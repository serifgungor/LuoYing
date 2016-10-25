/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.object.magic;

import name.huliqing.luoying.data.MagicData;
import name.huliqing.luoying.object.entity.Entity;

/**
 * @author huliqing
 * @param <T>
 */
public interface Magic<T extends MagicData> extends Entity<T> {

    @Override
    public void setData(T data);

    @Override
    public T getData();
    
    /**
     * 魔法的施放源，如果没有特别的施放源，则为null.
     * @param source 
     */
    void setSource(Entity source);
    
    /**
     * 设置魔法所针对的特别的目标对象列表，如果没有设置，则根据魔法的范围自己决定,在魔法范围内的对象都为目标对象。
     * @param targets 
     */
    void setTargets(Entity... targets);
    
    /**
     * 判断魔法是否可以作用于一个目标
     * @param entity
     * @return 
     */
    boolean canHit(Entity entity);
    
//    private final PlayService playService = Factory.get(PlayService.class);
//    
//    protected T data;
//    
//    // ---- 由于一些Object要涉及到缓存，以下一些参数存放到MagicData中,这些参数包
//    // 括：sounds,effects,states, actorAnims
//    // 格式: soundId|timePoint,soundId|timePoint...
//    protected List<SoundWrap> sounds;
//    
//    // 格式:effectId|timePoint,effectId|timePoint,effectId|timePoint...
//    protected List<EffectWrap> effects;
//    
//    // ----------------inner
//    protected final Spatial localRoot = new Node();
//    
//    protected Spatial traceObject;
//    
//    // 魔法的施放者，有可能为null.
//    protected Entity source;
//    
//    // 魔法针对的主目标,如果魔法没有特定的目标，则有可能为null
//    protected Entity target;
//    
//    protected HitChecker hitChecker;
//    
//    // 标记是否已经开始运行
//    protected boolean started;
    
    
    
//    @Override
//    public T getData() {
//        return data;
//    }
//
//    @Override
//    public void setData(T data) {
//        this.data = data;
//        // Sounds
//        String[] tempSounds = data.getAsArray("sounds");
//        if (tempSounds != null) {
//            sounds = new ArrayList<SoundWrap>(tempSounds.length);
//            for (int i = 0; i < tempSounds.length; i++) {
//                String[] soundArr = tempSounds[i].split("\\|");
//                SoundWrap sw = new SoundWrap();
//                sw.soundId = soundArr[0];
//                if (soundArr.length >= 2) {
//                    sw.timePoint = MathUtils.clamp(ConvertUtils.toFloat(soundArr[1], 0f), 0, 1);
//                }
//                sounds.add(sw);
//            }
//        }
//        
//        // Effects, 格式:effectId|timePoint,effect|timePoint,effectId|timePoint...
//        String[] tempEffects = data.getAsArray("effects");
//        if (tempEffects != null) {
//            effects = new ArrayList<EffectWrap>(tempEffects.length);
//            for (int i = 0; i < tempEffects.length; i++) {
//                String[] effectArr = tempEffects[i].split("\\|");
//                EffectWrap ew = new EffectWrap();
//                ew.effectId = effectArr[0];
//                if (effectArr.length >= 2) {
//                    ew.timePoint = MathUtils.clamp(ConvertUtils.toFloat(effectArr[1], 0f), 0, 1);
//                }
//                effects.add(ew);
//            }
//        }
//    }
//
//    @Override
//    public void updateDatas() {
//        // ignore
//    }
//    
//    @Override
//    public void initialize(Application app) {
//        super.initialize(app);
//        
//        if (data.getLocation() != null) {
//            localRoot.setLocalTranslation(data.getLocation());
//        }
//        if (data.getRotation() != null) {
//            localRoot.setLocalRotation(data.getRotation());
//        }
//        
//        source = playService.findActor(data.getSourceActor());
//        target = playService.findActor(data.getTargetActor());
//        
//        // 确定要跟随的目标对象
//        if (data.getTraceActor() > 0) {
//            Entity traceActor = playService.findActor(data.getTraceActor());
//            if (traceActor != null) {
//                traceObject = traceActor.getSpatial();
//            }
//        }
//        // 首次时进行一次位置更新
//        if (traceObject != null && Ly.getPlayState().isInScene(traceObject)) {
//            if (data.getTracePosition() == TraceType.once 
//                    || data.getTracePosition() == TraceType.always) {
//                doUpdateTracePosition();
//            }
//            if (data.getTraceRotation() == TraceType.once 
//                    || data.getTraceRotation() == TraceType.always) {
//                doUpdateTraceRotation();
//            }
//        }
//        
//        playService.addObject(localRoot);
//        
//        if (data.getHitCheckerData() != null) {
//            hitChecker = Loader.loadHitChecker(data.getHitCheckerData());
//        }
//    }
//    
//    @Override
//    public void update(float tpf) {
//        // time
//        data.setTimeUsed(data.getTimeUsed() + tpf);
//        
//        float inter = getInterpolation();
//        
//        // update position & rotation
//        if (traceObject != null && Ly.getPlayState().isInScene(traceObject)) {
//            if (data.getTracePosition() == TraceType.always) {
//                doUpdateTracePosition();
//            }
//            if (data.getTraceRotation() == TraceType.always) {
//                doUpdateTraceRotation();
//            }
//        }
//        
//        // update sounds
//        doUpdateSounds(inter);
//        
//        // update effects
//        doUpdateEffects(inter);
//
//        // 判断是否结束魔法
//        if (data.getTimeUsed() >= data.getUseTime()) {
//            playService.removePlayObject(this);
//        }
//
//    }
//    
//    private void doUpdateTracePosition() {
//        Vector3f pos = localRoot.getLocalTranslation();
//        pos.set(traceObject.getWorldTranslation());
//        // 注：tracePositionOffset是以被跟随的目标对象的本地坐标为基准的,
//        // 所以必须mult上目标对象的旋转
//        if (data.getTracePositionOffset() != null) {
//            TempVars tv = TempVars.get();
//            traceObject.getWorldRotation().mult(data.getTracePositionOffset(), tv.vect2);
//            pos.addLocal(tv.vect2);
//            tv.release();
//        }
//        localRoot.setLocalTranslation(pos);
//    }
//    
//    private void doUpdateTraceRotation() {
//        Quaternion rot = localRoot.getLocalRotation();
//        rot.set(traceObject.getWorldRotation());
//        if (data.getTraceRotationOffset() != null) {
//            rot.multLocal(data.getTraceRotationOffset());
//        }
//        localRoot.setLocalRotation(rot);
//    } 
//
//    /**
//     * 播放声音事件 
//     * @param inter
//     */
//    protected void doUpdateSounds(float inter) {
//        if (sounds != null) {
//            for (SoundWrap sw : sounds) {
//                sw.update(inter);
//            }
//        }
//    }
//    
//    protected void doUpdateEffects(float inter) {
//        if (effects != null) {
//            for (EffectWrap ew : effects) {
//                ew.update(inter);
//            }
//        }
//    }
//    
//    @Override
//    public void cleanup() {
//        started = false;
//        // 清理声效播放标记,让声效可以重新播放
//        if (sounds != null) {
//            for (SoundWrap sw : sounds) {
//                sw.cleanup();
//            }
//        }
//        // 清理效果播放标记,让效果可以重新播放
//        if (effects != null) {
//            for (EffectWrap ew : effects) {
//                ew.cleanup();
//            }
//        }
//        // 解除被跟随对象。
//        source = null;
//        target = null;
//        traceObject = null;
//        // 自动脱离
//        localRoot.removeFromParent();
//        super.cleanup();
//    }
//    
//    /**
//     * 获取当前时间的插值
//     * @return 
//     */
//    protected float getInterpolation() {
//        float useTime = data.getUseTime();
//        if (useTime <= 0) {
//            return 0;
//        }
//        float inter = data.getTimeUsed() / useTime;
//        if (inter < 1) {
//            return inter;
//        }
//        return 1;
//    }
//    
//    protected void playSound(String soundId) {
//        SoundManager.getInstance().playSound(soundId, localRoot.getWorldTranslation());
//    }
//    
//    protected void playEffect(String effectId) {
//        Effect effect = Loader.load(effectId);
//        effect.setTraceObject(localRoot);
//        EffectManager.getInstance().addEffect(effect);
//    }
//    
//    // -------------------------------------------------------------------------
//    
//    // 效果更新控制
//    protected class EffectWrap {
//        // 效果ID
//        String effectId;
//        // 效果的开始播放时间点
//        float timePoint;
//        // 标记效果是否已经开始
//        boolean started;
//        
//        void update(float interpolation) {
//            if (started) return;
//            if (interpolation >= timePoint) {
//                playEffect(effectId);
//                started = true;
//            }
//        }
//        
//        void cleanup() {
//            // 不要去手动调用效果的cleanup,效果只要开始后,就让它自动结束.
//            started = false;
//        }
//    }
//    
//    // 声音控制
//    public class SoundWrap {
//        String soundId;
//        float timePoint;     // 未受cutTime影响的时间点,在xml中配置的
//        boolean started;
//        
//        void update(float interpolation) {
//            if (started) return;
//            if (interpolation >= timePoint) {
//                playSound(soundId);
//                started = true;
//            }
//        }
//        
//        void cleanup() {
//            started = false;
//        }
//    }
}