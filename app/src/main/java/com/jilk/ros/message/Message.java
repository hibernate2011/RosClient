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
package com.jilk.ros.message;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Map;


@MessageType(string = "message")
public abstract class Message {
    
    // Some requirements about message types:
    //   - It must have a MessageType declaration to be recognized on inbound messages
    //   - Every field must be explicitly designated as public
    //   - Every field that is not a primitive or near-primitive must be another Message class
    //   - If there is a non-empty constructor, you must also have an empty constructor
    //   - If it is set up as an inner class, it needs an explicit nullary constructor
    //     (note: I have seen an inner class otherwise fail, I have not tested it with the explicit constructor)
    
    public static void register(Class c, Map<String, Class> messageClasses) {
        try {
            typecheck(c);

            // Must register the class and not have duplicate
            // This is not recursive because only the top level message class
            //   needs to be determined from the string - others are top-down.
            String messageString = getMessageType(c);
            Class existingClass = messageClasses.get(messageString);
            if (existingClass != null && !existingClass.equals(c))
                throw new MessageException("Message String \'" + messageString +
                    "\' is assigned to two different classes (" +
                        c.getName() + " and " + existingClass.getName() + ")");
            messageClasses.put(messageString, c);        
        }
        catch (MessageException ex) {
            // should be changed to be a hooked method to give library user control
            System.out.println(ex.getMessage());
        }
    }
    
    public static String getMessageType(Class c) {
        return ((MessageType) c.getAnnotation(MessageType.class)).string();
    }    
    
    // this has never been used or tested but kind of belongs here
    // commented out because it uses ReflectiveOperationException which is not available on Android
    /*
    public static Message newInstance(String className) {
        try {
            Class messageClass = Class.forName(className);
            if (Message.class.isAssignableFrom(messageClass))
                return (Message) messageClass.newInstance();
            else throw new ClassCastException();
        }
        catch (ReflectiveOperationException ex) {
            throw new RuntimeException("Unable to create message of class \'" + className + "\'.", ex);
        }
    }
    */
    
    // Could probably do more checking here, but not sure what right now
    private static void typecheck(Class c) throws MessageException {
        
        // Must inherit from Message
        if (!Message.class.isAssignableFrom(c))
            throw new MessageException("Class \'" + c.getName() +
                    "\' does not extend Message");

        // Must have the MessageType annotation
        if (getMessageType(c) == null)
            throw new MessageException("Class \'" + c.getName() +
                    "\' is missing the MessageType annotation");
                
        // All fields must also be valid Message classes
        // Note that this also serves to force-load all the message classes
        //      so that they get registered
        for (Field f : c.getFields()) {
            Class fc = f.getType();
            if (fc.isArray()) {
                Class ac = fc.getComponentType(); 
                if (!isPrimitive(ac))
                    typecheck(ac);
            }
            else if (!isPrimitive(fc))
                typecheck(fc);
        }
    }
    
    public void print() {
        printMessage(this, "");
    }
    
    private static void printMessage(Object o, String indent) {
        for (Field f : o.getClass().getFields()) {
            Class c = f.getType();
            Object fieldObject = getFieldObject(f, o);
            if (fieldObject != null) {
                if (isPrimitive(c))
                    System.out.println(indent + f.getName() + ": " + fieldObject);
                else if (c.isArray()) {
                    System.out.println(indent + f.getName() + ": [");                    
                    printArray(fieldObject, indent + "  ");
                    System.out.println(indent + "]");
                }
                else {
                    System.out.println(indent + f.getName() + ":");
                    printMessage(fieldObject, indent + "  ");
                }
            }
        }
    }
    
    private static void printArray(Object array, String indent) {
        Class arrayClass = array.getClass().getComponentType();
        for (int i = 0; i < Array.getLength(array); i++) {
            Object elementObject = Array.get(array, i);
            if (elementObject != null) {
                if (isPrimitive(arrayClass))
                    System.out.println(indent + i + ": " + elementObject);
                else if (arrayClass.isArray()) { // this is not actually allowed in ROS
                    System.out.println(indent + i + ": [");                    
                    printArray(elementObject, indent + "  ");
                    System.out.println(indent + "]");
                }
                else {
                    System.out.println(indent + i + ":");
                    printMessage(elementObject, indent + "  ");
                }
            }
        }
        // remember to print array indices
    }
    
    public static boolean isPrimitive(Class c) {
        return (c.isPrimitive() ||
                c.equals(String.class) ||
                Number.class.isAssignableFrom(c) ||
                c.equals(Boolean.class));        
    }    
    

    // Copied from com.jilk.ros.rosbridge.JSON
    private static Object getFieldObject(Field f, Object o) {
        Object fo = null;
        try {
            fo = f.get(o);
        }
        catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return fo;
    }
    
    public void copyFrom(Message source) {
        try {
            if (source.getClass() != getClass())
                throw new RuntimeException("Attempt to copy non-matching classes");
            for (Field f : getClass().getFields()) {
                Class fc = f.getType();
                if (fc.isArray())
                    throw new RuntimeException("copyFrom - array types not implemented");
                else if (!isPrimitive(fc))
                    ((Message) f.get(this)).copyFrom((Message) f.get(source));
                else {
                    Object value = f.get(source);
                    f.set(this, value);
                }
            }
        }
        catch (IllegalAccessException ex) {
            throw new RuntimeException ("copyFrom error", ex);
        }
        catch (ClassCastException ex) {
            throw new RuntimeException ("copyFrom error", ex);
        }
        // ReflectiveOperationException is not available on Android (Java 1.6)
        /*
        catch (ReflectiveOperationException ex) {
            throw new RuntimeException ("copyFrom error", ex);
        }
        */
    }
    
}
