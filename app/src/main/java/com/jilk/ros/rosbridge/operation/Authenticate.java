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

import com.jilk.ros.message.MessageType;

@MessageType(string = "auth")
public class Authenticate extends Operation {
    public String mac;
    public String client;
    public String dest;
    public String rand;
    public int t;
    public String level;
    public int end;
    
    public Authenticate() {}
    
    public Authenticate(
            String mac,
            String client,
            String dest,
            String rand,
            int t,
            String level,
            int end)
    {
        this.mac = mac;
        this.client = client;
        this.dest = dest;
        this.rand = rand;
        this.t = t;
        this.level = level;
        this.end = end;

        this.id = null; // even though id is on EVERY OTHER operation type
    }
    
}
