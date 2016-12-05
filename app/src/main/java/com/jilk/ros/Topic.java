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
import com.jilk.ros.rosbridge.operation.Advertise;
import com.jilk.ros.rosbridge.operation.Operation;
import com.jilk.ros.rosbridge.operation.Publish;
import com.jilk.ros.rosbridge.operation.Subscribe;
import com.jilk.ros.rosbridge.operation.Unadvertise;
import com.jilk.ros.rosbridge.operation.Unsubscribe;

import java.util.concurrent.LinkedBlockingQueue;


public class Topic<T extends Message> extends LinkedBlockingQueue<T> implements FullMessageHandler {
    protected String topic;
    private Class<? extends T> type;
    private String messageType;
    private ROSClient client;
    private Thread handlerThread;
    
    public Topic(String topic, Class<? extends T> type, ROSClient client) {
        this.topic = topic;
        this.client = client;
        this.type = type;
        messageType = Message.getMessageType(type);
        handlerThread = null;
    }
    
    @Override
    public void onMessage(String id, Message message) {
        add((T) message);
    }
    
    
    // warning: there is a delay between the completion of this method and 
    //          the completion of the subscription; it takes longer than
    //          publishing multiple other messages, for example.    
    public void subscribe(MessageHandler<T> handler) {
        startRunner(handler);
        subscribe();
    }
    
    public void subscribe() {
        client.register(Publish.class, topic, type, this);
        send(new Subscribe(topic, messageType));
    }
    
    public void unsubscribe() {
        // need to handle race conditions in incoming message handler
        //    so that once unsubscribe has happened the handler gets no more
        //    messages
        send(new Unsubscribe(topic));        
        client.unregister(Publish.class, topic);
        stopRunner();
    }
    
    private void startRunner(MessageHandler<T> handler) {
        stopRunner();
        handlerThread = new Thread(new MessageRunner(handler));
        handlerThread.setName("Message handler for " + topic);
        handlerThread.start();
    }
    
    private void stopRunner() {
        if (handlerThread != null) {
            handlerThread.interrupt();
            clear();
            handlerThread = null;
        }
    }
    
    
    public void advertise() {
        send(new Advertise(topic, messageType));
    }
    
    public void publish(T message) {
        send(new Publish(topic, message));
    }
    
    public void unadvertise() {
        send(new Unadvertise(topic));
    }
    
    private void send(Operation operation) {
        client.send(operation);
    }
    
    public void verify() throws InterruptedException {

        boolean hasTopic = false;
        for (String s : client.getTopics()) {
            if (s.equals(topic)) {
                hasTopic = true;
                break;
            }
        }
        if (!hasTopic)
            throw new RuntimeException("Topic \'" + topic + "\' not available.");
        
        client.typeMatch(client.getTopicMessageDetails(topic), type);
    }
    
    private class MessageRunner implements Runnable {
        private MessageHandler<T> handler;

        public MessageRunner(MessageHandler<T> handler) {
            this.handler = handler;
        }             
        
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    handler.onMessage(take());
                }
                catch (InterruptedException ex) {
                    break;
                }
            }
        }
    }
    
}
