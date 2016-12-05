/**
 * Copyright (c) 2014 Jilk Systems, Inc.
 * 
 * This file is part of the Java ROSBridge Client.
 *
 * The Java ROSBridge Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Java ROSBridge Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Java ROSBridge Client.  If not, see http://www.gnu.org/licenses/.
 * 
 */
package com.jilk.ros.rosbridge.operation;

import com.jilk.ros.message.Message;
import com.jilk.ros.message.MessageType;
import com.jilk.ros.rosbridge.implementation.JSON;
import com.jilk.ros.rosbridge.implementation.Registry;

@MessageType(string = "operation")
public class Operation extends Message {
    private static Long uid = 0L;

    public String op;
    public String id;
    
    public Operation() {
        this.op = getMessageType(getClass());
        this.id = nextId();
    }
    
    private static synchronized String nextId() {
        String result = uid.toString();
        uid++;
        return result;
    }
    
    public String toJSON() {
        return JSON.toJSON(this);
    }
    
    public static Operation toOperation(String json, Registry<Class> registry) {
        return ((Wrapper) JSON.toMessage(json, Wrapper.class, registry)).msg;
    }
    
    public static void initialize(Registry<Class> registry) {
        initClass(registry, Advertise.class);
        initClass(registry, Authenticate.class);
        initClass(registry, CallService.class);
        initClass(registry, Fragment.class);
        initClass(registry, Operation.class);
        initClass(registry, PNG.class);
        initClass(registry, Publish.class);
        initClass(registry, ServiceResponse.class);
        initClass(registry, SetStatusLevel.class);
        initClass(registry, Status.class);
        initClass(registry, Subscribe.class);
        initClass(registry, Unadvertise.class);
        initClass(registry, Unsubscribe.class);
        initClass(registry, Wrapper.class);

        registry.register(Wrapper.class, Message.getMessageType(Publish.class), Publish.class);
        registry.register(Wrapper.class, Message.getMessageType(CallService.class), CallService.class);
        registry.register(Wrapper.class, Message.getMessageType(ServiceResponse.class), ServiceResponse.class);
    }    
    
    private static void initClass(Registry<Class> registry, Class<? extends Message> c) {
        registry.register(Message.class, Message.getMessageType(c), c);
    }
}
