/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.object.game;

import com.jme3.math.Vector3f;
import name.huliqing.fighter.Factory;
import name.huliqing.fighter.data.GameData;
import name.huliqing.fighter.game.network.ActorNetwork;
import name.huliqing.fighter.game.service.PlayService;

/**
 *
 * @author huliqing
 */
public class StoryGuardGame extends StoryGame {
    private final PlayService playService = Factory.get(PlayService.class);
    private final ActorNetwork actorNetwork = Factory.get(ActorNetwork.class);
    
//    public final static int GROUP_PLAYER = 1;
    /** 敌军分组 */
    public final static int GROUP_ENEMY = 2;    // 敌军
    /** 妖精分组 */
    public final static int GROUP_FAIRY = 3;    // 妖精
    
    // 古柏的位置
    private Vector3f selfPosition;
    // 盗贼据点
    private Vector3f enemyPosition;
    // 妖精位置
    private Vector3f fairyPosition;
    
    public int fairyLevel = 25;
    public int servantLevel = 20;
    
     @Override
    public void initData(GameData data) {
        super.initData(data);
        selfPosition = data.getAsVector3f("selfPosition");
        enemyPosition = data.getAsVector3f("enemyPosition");
        fairyPosition = data.getAsVector3f("fairyPosition");
        fairyLevel = data.getAsInteger("fairyLevel", fairyLevel);
        servantLevel = data.getAsInteger("servantLevel", servantLevel);
        
        if (selfPosition == null || enemyPosition == null || fairyPosition == null) 
            throw new NullPointerException();
    }

    @Override
    protected void doInit() {
        addTask(new StoryGuardTask1(this));
        addTask(new StoryGuardTask2(this));
    }

    public Vector3f getSelfPosition() {
        return selfPosition;
    }

    public Vector3f getEnemyPosition() {
        return enemyPosition;
    }
    
    public Vector3f getFairyPosition() {
        return fairyPosition;
    }

    public int getFairyLevel() {
        return fairyLevel;
    }

    public int getServantLevel() {
        return servantLevel;
    }

}
