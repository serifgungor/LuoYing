/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.object.game.impl;

import com.jme3.util.TempVars;
import name.huliqing.core.Factory;
import name.huliqing.core.object.actor.Actor;
import name.huliqing.core.constants.IdConstants;
import name.huliqing.core.constants.ResConstants;
import name.huliqing.core.data.GameLogicData;
import name.huliqing.core.enums.MessageType;
import name.huliqing.core.enums.SkillType;
import name.huliqing.core.mvc.network.ActorNetwork;
import name.huliqing.core.mvc.network.PlayNetwork;
import name.huliqing.core.mvc.network.SkillNetwork;
import name.huliqing.core.mvc.network.StateNetwork;
import name.huliqing.core.mvc.service.ActorService;
import name.huliqing.core.mvc.service.LogicService;
import name.huliqing.core.mvc.service.PlayService;
import name.huliqing.core.mvc.service.SkillService;
import name.huliqing.core.mvc.service.StateService;
import name.huliqing.core.mvc.service.ViewService;
import name.huliqing.core.loader.Loader;
import name.huliqing.core.object.actorlogic.PositionActorLogic;
import name.huliqing.core.logic.scene.ActorBuildLogic;
import name.huliqing.core.logic.scene.ActorBuildLogic.Callback;
import name.huliqing.core.manager.ResourceManager;
import name.huliqing.core.object.gamelogic.AbstractGameLogic;
import name.huliqing.core.object.view.TextView;

/**
 * 宝箱任务第二阶段：守护宝箱
 * @author huliqing
 * @param <T>
 */
public class SurvivalLogic<T extends GameLogicData> extends AbstractGameLogic<T> {
    private final boolean debug = true;
    private final ActorNetwork actorNetwork = Factory.get(ActorNetwork.class);
    private final SkillNetwork skillNetwork = Factory.get(SkillNetwork.class);
    private final StateNetwork stateNetwork = Factory.get(StateNetwork.class);
    private final PlayNetwork playNetwork = Factory.get(PlayNetwork.class);
    private final PlayService playService = Factory.get(PlayService.class);
    private final StateService stateService = Factory.get(StateService.class);
    private final LogicService logicService = Factory.get(LogicService.class);
    private final ActorService actorService = Factory.get(ActorService.class);
    private final SkillService skillService = Factory.get(SkillService.class);
    private final ViewService viewService = Factory.get(ViewService.class);
    
    // 任务位置
    private final SurvivalGame game;
    
    // 怪物刷新器及刷新位置
    private ActorBuildLogic builderLogic;
    private SurvivalLevelLogic levelLogic;
    private SurvivalBoss bossLogic;
    
    // 宝箱角色
    private Actor treasure;
    
    // 当前任务阶段
    private int stage;
    
    /**
     * @param game
     */
    public SurvivalLogic(SurvivalGame game) {
        this.game = game;
    }

    @Override
    protected void doLogic(float tpf) {
        if (stage == 0) {
            Actor player = playService.getPlayer();
            if (player != null) {
                doInit();
                stage = 1;
            }
        }
        
        // 任务逻辑
        if (stage == 1) {
            if (treasure != null && actorService.isDead(treasure)) {
                playNetwork.addMessage(get(ResConstants.TASK_FAILURE), MessageType.notice);
                TextView textView = (TextView) viewService.loadView(IdConstants.VIEW_TEXT_FAILURE);
                textView.setUseTime(-1);
                playNetwork.addView(textView);
                game.removeLogic(builderLogic);
                game.removeLogic(levelLogic);
                game.removeLogic(bossLogic);
                stage = 999;
            }
            return;
        } 
        
        if (stage == 999) {
            // end
        }
    }
    
    private void doInit() {
        treasure = actorService.loadActor(IdConstants.ACTOR_TREASURE);
        actorService.setLocation(treasure, game.treasurePos);
        actorService.setGroup(treasure, game.SELF_GROUP);
        actorService.setTeam(treasure, actorService.getTeam(playService.getPlayer()));
        playNetwork.addActor(treasure);
        
        builderLogic = new ActorBuildLogic();
        builderLogic.setCallback(new Callback() {
            @Override
            public Actor onAddBefore(Actor actor) {
                actorService.setGroup(actor, game.GROUP_ENEMY);
                skillService.playSkill(actor, skillService.getSkill(actor, SkillType.wait).getId(), false);

                TempVars tv = TempVars.get();
                tv.vect1.set(game.treasurePos);
                tv.vect1.setY(playService.getTerrainHeight(tv.vect1.x, tv.vect1.z));
                PositionActorLogic runLogic = (PositionActorLogic) Loader.loadLogic(IdConstants.LOGIC_POSITION);
                runLogic.setInterval(3);
                runLogic.setPosition(tv.vect1);
                runLogic.setNearestDistance(game.nearestDistance);
                logicService.addLogic(actor, runLogic);
                tv.release();
                
                // 等级达到最高之后不再刷新敌人
                int level = levelLogic.getLevel();
                actorService.setLevel(actor, level < 1 ? 1 : level);
                return actor;
            }
        });
        
        builderLogic.setRadius(game.nearestDistance);
        builderLogic.setTotal(game.buildTotal);
        builderLogic.addPosition(game.enemyPositions);
        builderLogic.addId(
                  IdConstants.ACTOR_NINJA,IdConstants.ACTOR_NINJA
                , IdConstants.ACTOR_SPIDER, IdConstants.ACTOR_SPIDER
                , IdConstants.ACTOR_WOLF, IdConstants.ACTOR_WOLF
                , IdConstants.ACTOR_BEAR, IdConstants.ACTOR_BEAR
                , IdConstants.ACTOR_SCORPION, IdConstants.ACTOR_SCORPION
                );
        
        levelLogic = new SurvivalLevelLogic(game.levelUpBySec, game.maxLevel);
        bossLogic = new SurvivalBoss(game, builderLogic, levelLogic);

        // 刷新普通角色,等级更新器,BOSS逻辑
        game.addLogic(builderLogic);
        game.addLogic(levelLogic);
        game.addLogic(bossLogic);
    }
        
    private String get(String rid, Object... params) {
        if (params == null) {
            return ResourceManager.get(rid);
        } else {
            return ResourceManager.get(rid, params);
        }
    }

}
