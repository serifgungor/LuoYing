/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.ly.object.env;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import name.huliqing.ly.Ly;
import name.huliqing.ly.data.env.EnvData;
import name.huliqing.ly.object.scene.Scene;

/**
 * 天空盒模型
 * @author huliqing
 * @param <T>
 */
public class SkyEnv <T extends EnvData> extends AbstractEnv<T> {

    // 基本文件夹路径，如：“Textures/tex/sky/default_min/"
    // 如果指定了这个路径，则把它加在west,east,north,south,up,down前面
    private String baseDir;
    
    private String west;
    private String east;
    private String north;
    private String south;
    private String up;
    private String down;
    
    // ---- inner
    private Spatial sky;

    @Override
    public void setData(T data) {
        super.setData(data);
        baseDir = data.getAsString("baseDir");
        west = data.getAsString("west");
        east = data.getAsString("east");
        north = data.getAsString("north");
        south = data.getAsString("south");
        up = data.getAsString("up");
        down = data.getAsString("down");
    }

    @Override
    public void updateDatas() {
        // ignore
    }

    @Override
    public void initialize(Scene scene) {
        super.initialize(scene);
        AssetManager am = Ly.getApp().getAssetManager();
        Texture w = am.loadTexture(baseDir != null ? baseDir + west : west);
        Texture e = am.loadTexture(baseDir != null ? baseDir + east : east);
        Texture n = am.loadTexture(baseDir != null ? baseDir + north : north);
        Texture s = am.loadTexture(baseDir != null ? baseDir + south : south);
        Texture u = am.loadTexture(baseDir != null ? baseDir + up : up);
        Texture d = am.loadTexture(baseDir != null ? baseDir + down : down);
        sky = SkyFactory.createSky(am, w, e, n, s, u, d);
        sky.setCullHint(Spatial.CullHint.Never);
        sky.setQueueBucket(RenderQueue.Bucket.Sky);
        scene.addSpatial(sky);
    }

    @Override
    public void cleanup() {
        scene.removeSpatial(sky);
        sky = null;
        super.cleanup(); 
    }

    @Override
    public Vector3f getLocation() {
        return data.getLocation();
    }

    @Override
    public void setLocation(Vector3f location) {
        data.setLocation(location);
    }

    @Override
    public Quaternion getRotation() {
        return data.getRotation();
    }

    @Override
    public void setRotation(Quaternion rotation) {
        data.setRotation(rotation);
    }

    @Override
    public Vector3f getScale() {
        return data.getScale();
    }

    @Override
    public void setScale(Vector3f scale) {
        data.setScale(scale);
    }
    
    
}
