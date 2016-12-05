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
package com.jilk.ros.rosbridge.indication;

import java.lang.reflect.Field;

public class Indication {
    public static boolean isIndicated(Field f) {
        return (f.getAnnotation(Indicated.class) != null);
    }
    
    public static boolean asArray(Field f) {
        return (f.getAnnotation(AsArray.class) != null);
    }
    
    public static boolean isBase64Encoded(Field f) {
        return ((f.getAnnotation(Base64Encoded.class) != null) &&
                f.getType().isArray() &&
                f.getType().getComponentType().equals(byte.class)); 
    }
    
    public static String getIndicatorName(Class c) {
        return getName(c, Indicator.class);
    }
    
    public static String getIndicatedName(Class c) {
        return getName(c, Indicated.class);
    }
    
    private static String getName(Class c, Class annotation) {
        String result = null;
        for (Field f : c.getFields()) {
            if (f.getAnnotation(annotation) != null) {
                result = f.getName();
                break;
            }
        }
        return result;
    }

    /*
    public static Class getIndication(Object o, String s) {
        Class c = o.getClass();
        Class result = null;
        try {
            Method m = getIndicateMethod(c);
            result = (Class) (m.invoke(o, s));
        }
        catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
        return result;
    }
    
    private static Method getIndicateMethod(Class c) {
        Method result = null;
        for (Method m : c.getMethods()) {
            if (m.getAnnotation(Indicate.class) != null) {
                result = m;
                break;
            }
        }
        return result;
    }
    */
}
