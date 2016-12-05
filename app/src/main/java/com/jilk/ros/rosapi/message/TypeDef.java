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
package com.jilk.ros.rosapi.message;

import com.jilk.ros.message.Message;
import com.jilk.ros.message.MessageType;

@MessageType(string = "rosapi/TypeDef")
public class TypeDef extends Message {
    public String type;
    public String[] fieldnames;
    public String[] fieldtypes;
    public int[] fieldarraylen;
    public String[] examples;
    
    public static boolean match(String type, Class c) {
        boolean result = false;
        if (
            (type.equals("bool")    && ((boolean.class.equals(c)) || (Boolean.class.equals(c)))) ||
            (type.equals("int8")    && ((byte.class.equals(c))    || (Byte.class.equals(c)))) ||
            (type.equals("byte")    && ((byte.class.equals(c))    || (Byte.class.equals(c)))) ||            // deprecated
            (type.equals("uint8")   && ((short.class.equals(c))   || (Short.class.equals(c)))) ||
            (type.equals("char")    && ((short.class.equals(c))   || (Short.class.equals(c)))) ||           // deprecated
            (type.equals("int16")   && ((short.class.equals(c))   || (Short.class.equals(c)))) ||
            (type.equals("uint16")  && ((int.class.equals(c))     || (Integer.class.equals(c)))) ||
            (type.equals("int32")   && ((int.class.equals(c))     || (Integer.class.equals(c)))) ||
            (type.equals("uint32")  && ((long.class.equals(c))    || (Long.class.equals(c)))) ||
            (type.equals("int64")   && ((long.class.equals(c))    || (Long.class.equals(c)))) ||
            (type.equals("float32") && ((float.class.equals(c))   || (Float.class.equals(c)))) ||
            (type.equals("float64") && ((double.class.equals(c))  || (Double.class.equals(c)))) ||
            (type.equals("uint64")  &&                               (java.math.BigInteger.class.equals(c))) ||
            (type.equals("string")  &&                               (String.class.equals(c)))
        )
                result = true;
            
        return result;
    }
}
