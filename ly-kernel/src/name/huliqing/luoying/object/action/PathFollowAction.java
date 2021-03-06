/*
 * LuoYing is a program used to make 3D RPG game.
 * Copyright (c) 2014-2016 Huliqing <31703299@qq.com>
 * 
 * This file is part of LuoYing.
 *
 * LuoYing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LuoYing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LuoYing.  If not, see <http://www.gnu.org/licenses/>.
 */
package name.huliqing.luoying.object.action;

import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.Path;
import com.jme3.ai.navmesh.Path.Waypoint;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.data.ActionData;
import name.huliqing.luoying.layer.network.EntityNetwork;
import name.huliqing.luoying.layer.network.SkillNetwork;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.module.SkillModule;
import name.huliqing.luoying.object.skill.Skill;
import name.huliqing.luoying.utils.DebugDynamicUtils;
import name.huliqing.luoying.utils.ThreadHelper;

/**
 *
 * @author huliqing
 */
public class PathFollowAction extends AbstractAction implements FollowAction {
    private final static Logger LOG = Logger.getLogger(PathFollowAction.class.getName());
//    private final PlayService playService = Factory.get(PlayService.class);
//    private final ActorService actorService = Factory.get(ActorService.class);
//    private final SkillService skillService = Factory.get(SkillService.class);
    private final SkillNetwork skillNetwork = Factory.get(SkillNetwork.class);
    private final EntityNetwork entityNetwork = Factory.get(EntityNetwork.class);
    
    private SkillModule skillModule;
    
    // 被跟随的目标
    protected Spatial target;
    
    // 是否在跟随时自动朝向目标
    protected boolean autoFacing = true;
    
    // 允许的最近距离，不能太小，太小会产生两个现象,特别是在侦率较低时容易出现。
    // 1.当角色瞬间太快穿过目标路径点时，可能一直走下去。
    // 2.角色可能在路径起点时的ViewDirection方向不正确，表现为瞬间前后转向。
    // 主要是由于新路径的第一个点可能产生在了角色身后而导致方向错误。
    protected float nearest = 2f;
    private float nearestSquared = 4f; // 用于优化计算
    
    // ==== 走路
    // 角色最近走过的一个路径点
    private NavMeshPathfinder finder;
    private Path path;
    // 寻路线程返回值
    private Future<Boolean> future;
    // 角色当前正在走向的路径点
    private Waypoint current;
    // 寻路时间间隔,单位秒
    private final float pathFindInterval = 2f;
    private float pathFindTimeUsed;
    // 路径DEBUG信息。
    private boolean debug = false;
    
    // 避障
    private final Detour rayDetour = new RayDetour(this);
    private final Detour timeDetour = new TimeDetour(this);
    
    // 路径方向修正的时间间隔，单位秒,每经过一定时间修正一下路径。
    // 因为角色可能在行走过程中被打断或影响了方向。
    private final float fixInterval = 2f;
    private float fixTimeUsed;
    // 是否应该立即重设方式，有时候重置了跟随目标，则应该马上重设方式
    private boolean needResetDir;
    
    // 最近一次跟随的目标的位置，如果位置不变，则不需要重置
    private final Vector3f tempLastTargetPos = new Vector3f(-999, -999, -999);
    
    // 缓存技能id
    private Skill runSkill;
    private Skill waitSkill;
    
    public PathFollowAction() {
        super();
//        finder = playService.createPathfinder();

        LOG.log(Level.INFO, "寻路功能暂不支持");
    }
    
    @Override
    public void setData(ActionData ad) {
        super.setData(ad);
        this.autoFacing = ad.getAsBoolean("autoFacing", autoFacing);
        this.debug = ad.getAsBoolean("debug", debug);
//        finder = playService.createPathfinder();
    }

    @Override
    public void setActor(Entity actor) {
        super.setActor(actor);
        skillModule = actor.getModule(SkillModule.class);
    }
    
    @Override
    public void initialize() {
        super.initialize();
        rayDetour.setActor(actor);
        rayDetour.setAutoFacing(autoFacing);
        timeDetour.setActor(actor);
        timeDetour.setAutoFacing(autoFacing);
        
        if (skillModule != null) {
            if (waitSkill == null) {
                List<Skill> waitSkills = skillModule.getSkillWait(null);
                if (waitSkills != null && !waitSkills.isEmpty()) {
                    waitSkill = waitSkills.get(0);
                }                
            }

            if (runSkill == null) {
                List<Skill> runSkills = skillModule.getSkillRun(null);
                if (runSkills != null && !runSkills.isEmpty()) {
                    runSkill = runSkills.get(0);
                }                
            }
        }
    }
    
    @Override
    protected void doLogic(float tpf) {
        if (target == null) {
            return;
        }
        doFollow(target, tpf);
    }
    
    /**
     * 跟随目标
     * 否则返回false;
     * @param target 
     * @param tpf 
     */
    protected void doFollow(Spatial target, float tpf) {
        // 如果角色是不可移动的，则直接返回不处理逻辑
        if (runSkill == null) {
            end();
            return;
        }
        
        // target 不存在或已经脱场景
        if (target == null || target.getParent() == null) {
            if (waitSkill != null) {
                
//                skillNetwork.playSkill(actor, waitSkill, false);
                entityNetwork.useObjectData(actor, waitSkill.getData().getUniqueId());
                
            }
            end();
            return;
        }
        
        // 已经到达位置，则不需要再走
        if (actor.getSpatial().getWorldTranslation().distanceSquared(target.getWorldTranslation()) <= nearestSquared) {
            if (waitSkill != null) {
//                skillNetwork.playSkill(actor, waitSkill, false);
                entityNetwork.useObjectData(actor, waitSkill.getData().getUniqueId());
            }
            end();
            return;
        }
        
        // == 通过Ray偿试避开障碍，如果发现障碍，则让它偿试避开后再进行走动。
        if (rayDetour.detouring(tpf)) {
            return;
        }
        
        // 通过时间检测偿试避开障碍
        if (timeDetour.detouring(tpf)) {
            return;
        }
        
        // 位置没有变化，则不需要走动
        if (needResetDir || tempLastTargetPos.distanceSquared(target.getWorldTranslation()) > nearestSquared) {
            tempLastTargetPos.set(target.getWorldTranslation());
            needResetDir = true;
        }
        
        // 如果需要立即重置（如：重设了跟随目标，则应该马上重置寻路及方向）
        if (needResetDir) {
            needResetDir = false;
            future = null;
            path = null;
            TempVars tv = TempVars.get();
            skillNetwork.playWalk(actor
                    , runSkill
                    , target.getWorldTranslation().subtract(actor.getSpatial().getWorldTranslation(), tv.vect1), true, false);
            tv.release();
        }
        
        // == 修正路径,由于方向可能被Detour临时改变，所在这里必须注意修正。
        fixTimeUsed += tpf;
        if (fixTimeUsed > fixInterval) {
            fixTimeUsed = 0;
            TempVars tv = TempVars.get();
            if (path != null && current != null) {
                skillNetwork.playWalk(actor
                        , runSkill
                        , current.getPosition().subtract(actor.getSpatial().getWorldTranslation(), tv.vect1)
                        , true, false);
            } else {
                skillNetwork.playWalk(actor
                        , runSkill
                        , target.getWorldTranslation().subtract(actor.getSpatial().getWorldTranslation(), tv.vect1)
                        , true, false);
            }
            tv.release();
        }
        
        // == 每隔一定时间重新获取路径
        pathFindTimeUsed += tpf;
        if (pathFindTimeUsed > pathFindInterval && future == null && finder != null) {
            if (path != null 
                    && path.getWaypoints().size() > 0 
                    && path.getLast().getPosition().distanceSquared(target.getWorldTranslation()) <= nearestSquared) {
                // ignore,如果目标位置没有变化，则不需要重新查询路径
            } else {
                finder.clearPath();
                pathFindTimeUsed = 0;
                future = findPath(actor.getSpatial().getWorldTranslation(), target.getWorldTranslation(), false);
            }
        }
        
        // == 检测并获取新路径
        if (future != null) {
            checkPath();
        }
        
        // == 如果已经有路径点，则使用寻路，否则直接走向目标
        if (path != null) {
            runByPath();
        } else {
            runByStraight();
        }
    }
    
    private void runByPath() {
        Waypoint tempPoint = null;
        try {
            tempPoint = getNextPoint();
        } catch (Exception e) {
            // 多线程情况下总可能会有机率发生冲突。
            LOG.log(Level.WARNING, "Could not get next way point!", e);
        }
        if (tempPoint == null) {
            return;
        }
        
        // 转换到下一个路径点
        if (tempPoint != current || 
                !skillModule.isPlayingSkill(runSkill.getTypes())
                ) {
            current = tempPoint;
            skillNetwork.playWalk(actor
                    , runSkill
                    , current.getPosition().subtract(actor.getSpatial().getWorldTranslation()), true, false);
        }
    }
    
    private Path.Waypoint getNextPoint() {
        Waypoint waypoint = finder.getNextWaypoint();
        if (waypoint == null || finder.getPath().getWaypoints().size() <= 0 || finder.isAtGoalWaypoint()) 
            return null;
        
        // 将Y值移到与角色相同的Y值再比较距离，避免漂浮在空中的路径点无法比较
        TempVars temp = TempVars.get();
        temp.vect1.set(waypoint.getPosition());
        temp.vect1.setY(actor.getSpatial().getWorldTranslation().y);
        float distance = temp.vect1.distance(actor.getSpatial().getWorldTranslation());
        temp.release();
        
        if (distance < 1f) {
            finder.goToNextWaypoint();
            return getNextPoint();
        }
        return waypoint;
    }
    
    /**
     * 没有路径点的时候直线走路 
     */
    protected void runByStraight() {
        // 执行技能的过程很耗时，所以必须先作判断，以优化性能
        if (!skillModule.isRunning()) {
            skillNetwork.playWalk(actor, runSkill
                    , target.getWorldTranslation().subtract(actor.getSpatial().getWorldTranslation()), true, false);
        }
    }

    /**
     * 设置跟随时是否面向目标
     * @param autoFacing 
     */
    public void setAutoFacing(boolean autoFacing) {
        this.autoFacing = autoFacing;
        rayDetour.setAutoFacing(autoFacing);
        timeDetour.setAutoFacing(autoFacing);
    }

    @Override
    public void setFollow(Spatial target) {
        // 当跟随目标发生了变化时应该立即重置方向
        this.needResetDir = (this.target != target);
        this.target = target;
    }

    @Override
    public void setNearest(float nearest) {
        this.nearest = nearest;
        this.nearestSquared = nearest * nearest;
    }

    @Override
    public void cleanup() {
        if (finder != null) {
            finder.clearPath();
        }
        path = null;
        future = null;
        rayDetour.cleanup();
        timeDetour.cleanup();
        
        // add20160325,在退出跟随行为的时候要让角色停止下来，以避免突然中断跟
        // 随的时候角色还一直在向前冲
        if (actor != null) {
            if (waitSkill != null) {
//                skillNetwork.playSkill(actor, waitSkill, false);
                entityNetwork.useObjectData(actor, waitSkill.getData().getUniqueId());
            }
        }
        
        super.cleanup();
    }
    
    private void checkPath() {
        if (future.isDone()) {
            try {
                Boolean result = future.get();
                if (!result) {
                    finder.clearPath();
                    future = findPath(actor.getSpatial().getWorldTranslation(), target.getWorldTranslation(), true);
                } else {
                    path = finder.getPath();
                    future = null; // 获得线路后清空
                    if (debug) {
                        DebugDynamicUtils.debugPath(actor.getSpatial().getName() + toString(), path.getWaypoints());
                    }
                }
            } catch (Exception ex) {
                // 重要：这个异常必须处理。
                LOG.log(Level.WARNING, "actorName={}, error={1}", new Object[] {actor.getSpatial().getName(),ex.getMessage()});
                if (future != null) {
                    future.cancel(true);
                }
                future = null;
            }
        }
    }
    
    private Future findPath(final Vector3f start, final Vector3f end, final boolean warp) {
        Future temp = ThreadHelper.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (warp) {
                    finder.warp(start);
                } else {
                    finder.setPosition(start);
                }
                Boolean result = finder.computePath(end.clone());
                return result;
            }
        });
        return temp;
    }
}
