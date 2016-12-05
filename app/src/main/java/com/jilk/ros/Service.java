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
import com.jilk.ros.rosbridge.FullMessageHandler;
import com.jilk.ros.rosbridge.operation.CallService;
import com.jilk.ros.rosbridge.operation.ServiceResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Service<CallType extends Message, ResponseType extends Message> extends Message implements FullMessageHandler {
    private String service;
    private Class<? extends ResponseType> responseType;
    private Class<? extends CallType> callType;
    private ROSClient client;
    private Map<String, CallRecord> calls;
    
    public Service(String service, Class<? extends CallType> callType,
            Class<? extends ResponseType> responseType, ROSClient client) {
        this.service = service;
        this.client = client;
        this.responseType = responseType;
        this.callType = callType;
        calls = new HashMap<String, CallRecord>();
    }

    // A result can only be returned once; it is cleared from the hash before 
    //   being sent/returned. This is to ensure that results do not accumulate
    //   indefinitely.  If callers need to keep these around they can set up their 
    //   own hash.
    
    @Override
    public void onMessage(String id, Message response) {
        //System.out.print("Service.onMessage: ");
        //response.print();
        CallRecord call = calls.get(id);
        if(call == null) {
            System.out.print("No caller service response");
            return;
        }
        if (call.handler != null) {
            calls.remove(id);
            call.handler.onMessage((ResponseType) response);
        }
        else {
            call.result = (ResponseType) response;
            call.latch.countDown();
        }
    }
    
    public String call(CallType args) {
        return callImpl(args, null);
    }
    
    public void callWithHandler(CallType args, MessageHandler<ResponseType> responseHandler) {
        callImpl(args, responseHandler);
    }
    
    public ResponseType callBlocking(CallType args) throws InterruptedException {
        return take(call(args));
    }
    
    private String callImpl(CallType args, MessageHandler<ResponseType> responseHandler) {
        client.register(ServiceResponse.class, service, responseType, this);  // do this once on creation?
        CallService messageCallService = new CallService(service, args);
        String id = messageCallService.id;
        CallRecord callRecord = new CallRecord(responseHandler);
        calls.put(id, callRecord);
        client.send(messageCallService);
        return id;
    }
    
    public ResponseType poll(String id) {
        CallRecord call = calls.get(id);
        if (call.result != null)
            calls.remove(id);
        return call.result;
    }
    
    public ResponseType take(String id) throws InterruptedException {
        CallRecord call = calls.get(id);
        call.latch.await();
        calls.remove(id);
        return call.result;
    }
    
    public void verify() throws InterruptedException {

        boolean hasService = false;
        for (String s : client.getServices()) {
            if (s.equals(service)) {
                hasService = true;
                break;
            }
        }
        if (!hasService)
            throw new RuntimeException("Service \'" + service + "\' not available.");
        
        client.typeMatch(client.getServiceRequestDetails(service), callType);
        client.typeMatch(client.getServiceResponseDetails(service), responseType);
    }
    
    private class CallRecord {
        public ResponseType result;
        public CountDownLatch latch;
        public MessageHandler<ResponseType> handler;
        
        public CallRecord(MessageHandler<ResponseType> handler) {
            this.result = null;
            this.latch = new CountDownLatch(1);
            this.handler = handler;
        }
    }
}
