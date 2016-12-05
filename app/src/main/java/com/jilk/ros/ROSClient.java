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
package com.jilk.ros;

import com.jilk.ros.message.Message;
import com.jilk.ros.rosapi.message.TypeDef;
import com.jilk.ros.rosbridge.FullMessageHandler;
import com.jilk.ros.rosbridge.ROSBridgeClient;
import com.jilk.ros.rosbridge.operation.Operation;

public abstract class ROSClient {

    public ROSClient() {}
    
    public static ROSClient create(String uriString) {
        // if we ever implement other ROSClient types, we'll key off the URI protocol (e.g., ws://)
        // we'd also have to abstract out Topic and Service since they depend on the ROSBridge operations
        return new ROSBridgeClient(uriString);
    }
    
    public abstract boolean connect();
    public abstract boolean connect(ConnectionStatusListener listener);
    public abstract void disconnect();
    public abstract void send(Operation operation);
    public abstract void send(String json);
    public abstract void register(Class<? extends Operation> c,
            String s,
            Class<? extends Message> m,
            FullMessageHandler h);
    public abstract void unregister(Class<? extends Operation> c, String s);
    public abstract void setDebug(boolean debug);
    public abstract String[] getNodes() throws InterruptedException;
    public abstract String[] getTopics() throws InterruptedException;
    public abstract String[] getServices() throws InterruptedException;
    public abstract TypeDef getTopicMessageDetails(String topic) throws InterruptedException;
    public abstract TypeDef[] getTopicMessageList(String topic) throws InterruptedException;
    public abstract TypeDef getServiceRequestDetails(String service) throws InterruptedException;
    public abstract TypeDef[] getServiceRequestList(String service) throws InterruptedException;
    public abstract TypeDef getServiceResponseDetails(String service) throws InterruptedException;
    public abstract TypeDef[] getServiceResponseList(String service) throws InterruptedException;
    public abstract TypeDef getTypeDetails(String type) throws InterruptedException;
    public abstract TypeDef[] getTypeList(String type) throws InterruptedException;
    public abstract void typeMatch(TypeDef t, Class<? extends Message> c) throws InterruptedException; 
    public abstract Object getUnderlyingClient(); // for debugging
    
    public interface ConnectionStatusListener {
        public void onConnect();
        public void onDisconnect(boolean normal, String reason, int code);
        public void onError(Exception ex);
    }
}
