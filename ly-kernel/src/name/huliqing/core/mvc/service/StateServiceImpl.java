/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.mvc.service;

import com.jme3.math.FastMath;
import com.jme3.util.SafeArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import name.huliqing.core.Factory;
import name.huliqing.core.object.actor.Actor;
import name.huliqing.core.data.StateData;
import name.huliqing.core.loader.Loader;
import name.huliqing.core.xml.DataFactory;
import name.huliqing.core.object.actor.StateListener;
import name.huliqing.core.object.control.ActorStateControl;
import name.huliqing.core.object.state.State;

/**
 *
 * @author huliqing
 */
public class StateServiceImpl implements StateService{
    private static final Logger LOG = Logger.getLogger(StateServiceImpl.class.getName());

    private ResistService resistService;
    
    @Override
    public void inject() {
        resistService = Factory.get(ResistService.class);
    }
    
    @Override
    public float checkAddState(Actor actor, String stateId) {
        // mark20160521,以后不要管是否是死亡都允许添加状态，能否添加状态由各自状态处理器去判断。
        // 角色死亡时不能再添加状态，
        // 1.因为角色在死亡时可能会全部清理身上的状态，这时候如果再允许添状态时可能会发生冲突.may NPE
        // 2.另外一些状态也不能在死亡的时候使用，比如晕眩状态可能让死亡后的角色重新站立(被reset)
        // 3.因此死亡的角色返回抵抗值为1，即完全抵抗
        // remove20160123 test
//        if (actor.isDead()) {
//            return 1;
//        }
   
        // remove20160803
//        // 如果状态不存在，则返回一个绝对抵抗值，以阻止继续添加状态
//        if (!existsState(stateId)) {
//            if (Config.debug) {
//                Logger.getLogger(StateServiceImpl.class.getName()).log(Level.WARNING
//                        , "State not found!!! stateId={0}", stateId);
//            }
//            return 1.0f;
//        }
        
        // 获得角色抗性值
        float resist = resistService.getResist(actor, stateId);
        
        // 1.毫无抗性，直接添加
        if (resist <= 0) {
            return 0;
        }
        
        // 2.完全抗性
        if (resist >= 1.0f) {
            return 1;
        }
        
        // 3.给一个完全抵抗的机会
        if (resist >= FastMath.nextRandomFloat()) {
            return 1;
        }
        
        // 4.抵抗不成功仍有机会根据角色的最高抵抗值随机计算一个最终抵抗值，该
        // 值最高不会超过角色的最高抵抗值．该最终抵抗值可削弱一部分状态的作用．
        float resultResist = resist * FastMath.nextRandomFloat();
        return resultResist;
    }
    
    @Override
    public boolean addState(Actor actor, String stateId, Actor sourceActor) {
        // 如果resist >= 1.0则说明完全抵抗，则不添加
        float resist = checkAddState(actor, stateId);
        if (resist < 1.0f) {
            addStateForce(actor, stateId, resist, sourceActor);
            return true;
        }
        return false;
    }
    
    @Override
    public void addStateForce(Actor actor, String stateId, float resist, Actor sourceActor) {
        // 创建Data
        StateData newStateData = DataFactory.createData(stateId);
        newStateData.setResist(resist);
        if (sourceActor != null) {
            newStateData.setSourceActor(sourceActor.getData().getUniqueId());
        }
        
        // 1.先尝试移去旧的，因为状态在设计上是不允许重复存在的。
        removeState(actor, stateId);
        
        // 2.添加状态
        actor.getData().getStates().add(newStateData);
        State state = Loader.loadState(newStateData);
        
        ActorStateControl control = actor.getModel().getControl(ActorStateControl.class);
        control.addState(state);
        
        // 3.侦听器
        List<StateListener> sls = control.getStateListeners();
        if (sls != null && !sls.isEmpty()) {
            for (StateListener sl : sls) {
                sl.onStateAdded(actor, state);
            }
        }
    }

    @Override
    public final boolean removeState(Actor actor, String removeStateId) {
        // 1.从Data中移除
        List<StateData> datas = actor.getData().getStates();
        Iterator<StateData> it = datas.iterator();
        while (it.hasNext()) {
            if (it.next().getId().equals(removeStateId)) {
                it.remove();
            }
        }
        
        // 2.从Processor中移除
        ActorStateControl control = actor.getModel().getControl(ActorStateControl.class);
        State stateRemoved = control.findState(removeStateId);
        if (stateRemoved != null) {
            control.removeState(stateRemoved);
        }
        
        // 3.通知侦听器
        List<StateListener> sls = control.getStateListeners();
        if (sls != null && !sls.isEmpty()) {
            for (StateListener sl : sls) {
                sl.onStateRemoved(actor, stateRemoved);
            }
        }
        
        return stateRemoved != null;
    }

    @Override
    public State findState(Actor actor, String stateId) {
        ActorStateControl control = actor.getModel().getControl(ActorStateControl.class);
        return control.findState(stateId);
    }

    @Override
    public void clearStates(Actor actor) {
        List<StateData> datas = actor.getData().getStates();
        for (StateData sd : datas) {
            removeState(actor, sd.getId());
        }
    }

    @Override
    public boolean existsState(Actor actor, String stateId) {
        List<StateData> datas = actor.getData().getStates();
        for (StateData data : datas) {
            if (data.getId().equals(stateId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<StateData> getStates(Actor actor) {
        return actor.getData().getStates();
    }

    @Override
    public void addListener(Actor actor, StateListener listener) {
        ActorStateControl control = actor.getModel().getControl(ActorStateControl.class);
        control.addStateListener(listener);
    }

    @Override
    public boolean removeListener(Actor actor, StateListener listener) {
        ActorStateControl control = actor.getModel().getControl(ActorStateControl.class);
        return control != null && control.removeStateListener(listener);
    }
    
    
}
