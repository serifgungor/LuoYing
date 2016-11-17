/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.message;

import com.jme3.util.SafeArrayList;

/**
 * MessageFactory用于统一处理系统的消息。
 * @author huliqing
 */
public class MessageFactory {
    
    private final static SafeArrayList<MessageHandler> HANDLERS = new SafeArrayList<MessageHandler>(MessageHandler.class);
    
    /**
     * 添加消息，这些消息将由当前已经注册的MessageHandler进行处理，如果没有添加过任何消息处理器, 
     * 则该方法将什么也不会做。
     * @param code 消息状态码. 参考：StateCode
     * @param message 消息内容
     * @param params 可用的参数值，不同的消息可能会有不同的参数类型或参数数量,这个参数是临时性的。
     * @see #addHandler(name.huliqing.luoying.message.MessageHandler) 
     * @see name.huliqing.luoying.constants.StateCode
     */
    public static void post(int code, String message, Object... params) {
        if (HANDLERS.isEmpty()) 
            return;
        for (MessageHandler handler : HANDLERS.getArray()) {
            handler.handle(code, message, params);
        }
    }
    
    /**
     * 添加一个消息处理器.
     * @param handler 
     */
    public static void addHandler(MessageHandler handler) {
        if (!HANDLERS.contains(handler)) {
            HANDLERS.add(handler);
        }
    }
    
    /**
     * 移除一个消息处理器
     * @param handler
     * @return 
     */
    public static boolean removeHandler(MessageHandler handler) {
        return HANDLERS.remove(handler);
    }
}
