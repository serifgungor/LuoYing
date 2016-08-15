/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.mvc.service;

import java.util.List;
import name.huliqing.core.data.TaskData;
import name.huliqing.core.loader.Loader;
import name.huliqing.core.object.actor.Actor;
import name.huliqing.core.object.actor.TaskListener;
import name.huliqing.core.object.control.ActorTaskControl;
import name.huliqing.core.object.task.Task;

/**
 *
 * @author huliqing
 */
public class TaskServiceImpl implements TaskService {

    @Override
    public void inject() {
    }

    @Override
    public Task loadTask(String taskId) {
        return Loader.loadTask(taskId);
    }

    @Override
    public Task loadTask(TaskData taskData) {
        return Loader.loadTask(taskData);
    }
    
    @Override
    public void addTask(Actor actor, Task task) {
        // 任务不能重复，如果已经存在，则不再添加
        List<TaskData> tasks = actor.getData().getTasks();
        for (TaskData td : tasks) {
            if (td.getId().equals(task.getId())) {
                return;
            }
        }
        
        actor.getData().getTasks().add(task.getData());
        
        ActorTaskControl control = actor.getModel().getControl(ActorTaskControl.class);
        control.addTask(task);
        
        // 侦听器
        List<TaskListener> tls = control.getTaskListeners();
        if (tls != null && !tls.isEmpty()) {
            for (TaskListener tl : tls) {
                tl.onTaskAdded(actor, task);
            }
        }
    }

    @Override
    public Task getTask(Actor actor, String taskId) {
        ActorTaskControl control = actor.getModel().getControl(ActorTaskControl.class);
        return control.getTask(taskId);
    }

    @Override
    public List<Task> getTasks(Actor actor) {
        ActorTaskControl control = actor.getModel().getControl(ActorTaskControl.class);
        return control.getTasks();
    }
    
    @Override
    public List<TaskData> getTaskDatas(Actor actor) {
        return actor.getData().getTasks();
    }
    
    @Override
    public boolean checkCompletion(Actor actor, Task task) {
        checkValidState(actor, task);
        return task.checkCompletion();
    }
    
    @Override
    public void completeTask(Actor actor, Task task) {
        checkValidState(actor, task);
        // 执行“任务完成”如奖励经验、金钱、物品等。。。
        task.doCompletion();
        // 清理任务处理器，释放资源，当任务完成之后就不需要这些东西了，如各种侦听器等
        task.cleanup();
        // 把任务标记为“完成”
        task.getData().setCompletion(true);
        
        // 侦听器
        ActorTaskControl control = actor.getModel().getControl(ActorTaskControl.class);
        List<TaskListener> tls = control.getTaskListeners();
        if (tls != null && !tls.isEmpty()) {
            for (TaskListener tl : tls) {
                tl.onTaskCompleted(actor, task);
            }
        }
    }
    
    @Override
    public void applyItem(Actor actor, Task task, String itemId, int amount) {
        checkValidState(actor, task);
        TaskData data = task.getData();
        data.applyTaskItem(itemId, amount);
    }

    @Override
    public int getItemTotal(Actor actor, Task task, String itemId) {
        checkValidState(actor, task);
        return task.getData().getTaskItemTotal(itemId);
    }

    @Override
    public void addTaskListener(Actor actor, TaskListener taskListener) {
        ActorTaskControl control = actor.getModel().getControl(ActorTaskControl.class);
        if (control != null) {
            control.addTaskListener(taskListener);
        }
    }

    @Override
    public boolean removeTaskListener(Actor actor, TaskListener taskListener) {
        ActorTaskControl control = actor.getModel().getControl(ActorTaskControl.class);
        return control != null && control.removeTaskListener(taskListener);
    }
    
    // 检查任务状态是否正常
    private void checkValidState(Actor actor, Task task) {
        if (task.getActor() != actor) 
            throw new IllegalStateException("Task state error by execute actor, task=" + task.getId()
                    + ", actor=" + actor.getData().getId());
    }
}
