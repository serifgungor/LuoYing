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
package name.huliqing.luoying.mess.network;

import com.jme3.network.serializing.Serializable;
import name.huliqing.luoying.mess.BaseMess;

/**
 * 向服务端发送Ping消息测试Ping值,这个消息不需要使用TCP方式，只要udp就可以.
 * @author huliqing
 */
 @Serializable
public class PingMess extends BaseMess {

     public long sendTime;

     public PingMess() {
         super(false);
     }

    
}
