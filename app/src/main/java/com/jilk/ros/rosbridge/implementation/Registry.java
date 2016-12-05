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
package com.jilk.ros.rosbridge.implementation;

import java.util.HashMap;
import java.util.Map;

public class Registry<T> extends HashMap<Class, Map<String, T>> {
    
    public void register(Class c, String s, T t) {
        Map<String, T> table = get(c);
        if (table == null) {
            table = new HashMap<String, T>();
            put(c, table);
        }
        table.put(s, t);
    }
    
    public void unregister(Class c, String s) {
        Map<String, T> table = get(c);
        if (table != null)
            table.remove(s);
    }
    
    public T lookup(Class c, String s) {
        T result = null;
        Map<String, T> table = get(c);
        if (table != null)
            result = table.get(s);
        return result;
    }
}
