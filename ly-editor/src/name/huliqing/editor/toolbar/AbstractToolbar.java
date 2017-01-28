/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.editor.toolbar;

import com.jme3.util.SafeArrayList;
import java.util.List;
import name.huliqing.editor.tools.Tool;
import name.huliqing.editor.edit.JmeEdit;

/**
 *
 * @author huliqing
 * @param <E> Form类型
 */
public abstract class AbstractToolbar<E extends JmeEdit> implements Toolbar<E> {
//    private static final Logger LOG = Logger.getLogger(AbstractToolbar.class.getName());

    protected final SafeArrayList<ToolbarListener> listeners = new SafeArrayList<ToolbarListener>(ToolbarListener.class);
    
    /**
     * 所有添加到工具栏中的工具
     */
    protected final SafeArrayList<Tool> tools = new SafeArrayList<Tool>(Tool.class);
    
    /**
     * 所有可用的工具,是tools的子集
     */
    protected final SafeArrayList<Tool> toolsEnabled = new SafeArrayList<Tool>(Tool.class);
    
    /**
     * 当前处于激活状态的工具栏, 是toolsValid的子集
     */
    protected final SafeArrayList<Tool> toolsActivated = new SafeArrayList<Tool>(Tool.class);
    
    protected E edit;
    protected boolean initialized;
    protected boolean enabled = true;
    
    public AbstractToolbar(E edit) {
        this.edit = edit;
    }

    @Override
    public void initialize() {
        if (initialized) {
            throw new IllegalStateException();
        }
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    @Override
    public void update(float tpf) {
        for (Tool t : toolsActivated.getArray()) {
            t.update(tpf);
        }
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        boolean changed = this.enabled != enabled;
        this.enabled = enabled;
        if (changed) {
            for (ToolbarListener tl : listeners.getArray()) {
                tl.onStateChanged(enabled);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void cleanup() {
        toolsActivated.forEach(t -> {t.cleanup();});
        initialized = false;
    }
    
    @Override
    public <T extends Toolbar> T add(Tool tool) {
        if (!tools.contains(tool)) {
            tools.add(tool);
            for (ToolbarListener tl : listeners.getArray()) {
                tl.onToolAdded(tool);
            }
        }
        return (T) this;
    }
    
    @Override
    public boolean remove(Tool tool) {
        boolean result = tools.remove(tool);
        toolsActivated.remove(tool);
        toolsEnabled.remove(tool);
        if (tool.isInitialized()) {
            tool.cleanup();
        }
        if (result) {
            for (ToolbarListener tl : listeners.getArray()) {
                tl.onToolRemoved(tool);
            }
        }
        return result;
    }
    
    /**
     * 清理掉工具栏中的所有工具,这个方法由子类调用，方便在cleanup的时候一次性清理和移除，而不需要一个一个清理的移除.
     * 这个方法不会触发侦听器，只用于在清理，销毁工具栏的时候方便调用。
     */
    protected void removeAll() {
        for (Tool t : tools.getArray()) {
            if (t.isInitialized()) {
                t.cleanup();
            }
        }
        tools.clear();
        toolsEnabled.clear();
        toolsActivated.clear();
    }

    @Override
    public Tool getTool(String tool) {
        for (Tool t : tools.getArray()) {
            if (tool.equals(t.getName())) {
               return t;
            }
        }
        return null;
    }

    @Override
    public <T extends Tool> T getTool(Class<? extends Tool> clazz) {
        for (Tool t : tools.getArray()) {
            if (clazz.isAssignableFrom(t.getClass())) {
               return (T) t;
            }
        }
        return null;
    }
    
    @Override
    public <T extends Toolbar> T setActivated(Tool tool, boolean activated) {
        if (activated) {
            toolEnabled(tool);
            toolActivated(tool);
        } else {
            toolDeactivated(tool);
        }
        return (T) this;
    }
    
    @Override
    public <T extends Toolbar> T setEnabled(Tool tool, boolean enabled) {
        if (enabled) {
            toolEnabled(tool);
        } else {
            toolDeactivated(tool);
            toolDisabled(tool);
        }
        return (T) this;
    }
    
    private void toolActivated(Tool t) {
        if (!toolsActivated.contains(t)) {
            toolsActivated.add(t);
            if (!t.isInitialized()) {
                t.initialize(edit, this);
            }
            for (ToolbarListener tl : listeners.getArray()) {
                tl.onToolActivated(t);
            }
//            LOG.log(Level.INFO, "toolActivated, tool={0}", t.getName());
        }
    }
    
    private void toolDeactivated(Tool t) {
        if (toolsActivated.remove(t)) {
            if (t.isInitialized()) {
                t.cleanup();
            }
            for (ToolbarListener tl : listeners.getArray()) {
                tl.onToolDeactivated(t);
            }
//            LOG.log(Level.INFO, "toolDeactivated, tool={0}", t.getName());
        }
    }
    
    private void toolEnabled(Tool t) {
        if (toolsEnabled.contains(t)) {
            return;
        }
        toolsEnabled.add(t);
        for (ToolbarListener l : listeners.getArray()) {
            l.onToolEnabled(t);
        }
//        LOG.log(Level.INFO, "toolEnabled, tool={0}", t.getName());
    }
    
    private void toolDisabled(Tool t) {
        if (toolsEnabled.remove(t)) {
            for (ToolbarListener l : listeners.getArray()) {
                l.onToolDisabled(t);
            }
//            LOG.log(Level.INFO, "toolDisabled, tool={0}", t.getName());
        }
    }

    @Override
    public void addListener(ToolbarListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public boolean removeListener(ToolbarListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public List<Tool> getTools() {
        return tools;
    }

    /**
     * 获取所有可用的工具
     * @return 
     */
    @Override
    public SafeArrayList<Tool> getToolsEnabled() {
        return toolsEnabled;
    }

    /**
     * 获取所有激活中的工具
     * @return 
     */
    @Override
    public SafeArrayList<Tool> getToolsActivated() {
        return toolsActivated;
    }

    @Override
    public E getEdit() {
        return edit;
    }

    
}
