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

import com.jilk.ros.message.Message;
import com.jilk.ros.rosbridge.indication.Indication;
import com.jilk.ros.rosbridge.operation.Wrapper;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;


// The slightly crazy abstractions here are designed to isolate knowledge of
//    the JSON library and data types from the Operation details of rosbridge.
//    Why is this important?  A few reasons I can see.  First, we might want
//    to change JSON libraries and this encapsulates all use of JSON-simple.
//    Second, as much as possible I would like the semantics of the rosbridge
//    protocol to be encapsulated in the Operation and its subclasses rather
//    than in a module that is essentially about serialization.
//
//    Unfortunately the hierarchical Message abstraction is a bit broken 
//    at the top level. Beginning at the actual operation (e.g., Publish), the
//    types of the fields are determined either by the fields themselves or by
//    an indicator.  However, the type of the operation itself is not determined
//    this way, because the indicator is in the object itself, which means it
//    would have to be created before its type is known. Rather than build in
//    exceptions, I elected to create a "Wrapper" operation type that simply
//    wraps the concrete operation and copies its "op" field.
//    

public class JSON {

    /**
     * Translates a Message recursively into JSON. Normally the Message is also an
     * Operation, but it does not have to be. The caller constructs a complete
     * message using @Operation and @Message types. This includes situations
     * where one or more fields are marked to be turned into arrays, using @AsArray. 
     * @param m  the @Message object to be recursively translated.
     * @return   the complete JSON string.
     */
    public static String toJSON(Message m) {
        JSONObject jo = convertObjectToJSONObject(m); // Object to JSON-Simple
        return jo.toJSONString();                     // JSON-Simple to string
    }
    
    /**
     * Translates JSON into a hierarchical Operation/Message structure.
     * This includes handling fields that are @Indicated and @AsArray. If the
     * @Class parameter is a @Wrapper, this is a special case whereby the
     * object is wrapped to create a consistent hierarchy. 
     * @param json  the source JSON string
     * @param c     the top level class of the JSON. Normally @Wrapper
     * @param r     the @Registry containing topic registrations
     * @return      the fully instantiated message hierarchy represented
     *              by the JSON string.
     */
    public static Message toMessage(String json, Class c, Registry<Class> r) {
        JSONObject joUnwrapped = convertStringToJSONObject(json); // String to JSON-Simple
        JSONObject jo = joUnwrapped;
        if (Wrapper.class.isAssignableFrom(c))
            jo = wrap(joUnwrapped, c);                            // wrap: a hack to make the hierarchy homogeneous
        return convertJSONObjectToMessage(jo, c, r);              // JSON-Simple to Message
    }
    
    // *** Create JSON from Messages *** //
    
    // Translate the object into a JSON-Simple object, field-by-field,
    //   recursively via convertElementToJSON.
    //   except for the case where AsArray is indicated
    private static JSONObject convertObjectToJSONObject(Object o) {
        JSONObject result = new JSONObject();
        for (Field f : o.getClass().getFields()) {
            Object fieldObject = getFieldObject(f, o);
            if (fieldObject != null) {
                Object resultObject;
                if (Indication.isBase64Encoded(f))
                    resultObject = convertByteArrayToBase64JSONString(fieldObject);
                else if (Indication.asArray(f))
                    resultObject = convertObjectToJSONArray(fieldObject);
                else resultObject = convertElementToJSON(fieldObject);
                result.put(f.getName(), resultObject);
            }
        }
        return result;
    }
    
    // Convert an array type to a JSON-Simple array, element-by-element,
    //    recursively via convertElementToJSON.
    private static JSONArray convertArrayToJSONArray(Object array) {
        JSONArray result = new JSONArray();
        for (int i = 0; i < Array.getLength(array); i++) {
            Object elementObject = Array.get(array, i);
            if (elementObject != null) {
                Object resultObject = convertElementToJSON(elementObject);
                result.add(resultObject);
            }
        }        
        return result;
    }
    
    // For AsArray objects, convert the object to a JSON-Simple array
    //     NOTE: This relies on later versions of the JDK providing 
    //           the fields in order.
    private static JSONArray convertObjectToJSONArray(Object o) {
        JSONArray result = new JSONArray();
        for (Field f : o.getClass().getFields()) {
            Object fieldObject = getFieldObject(f, o);
            if (fieldObject != null) {
                Object resultObject = convertElementToJSON(fieldObject);
                result.add(resultObject);
            }
        }
        return result;
    }
            
    // Convert the individual field or array element items recursively
    private static Object convertElementToJSON(Object elementObject) {
        Class elementClass = elementObject.getClass();
        Object resultObject;
        if (Message.isPrimitive(elementClass))
            resultObject = elementObject;
        else if (elementClass.isArray())  
            resultObject = convertArrayToJSONArray(elementObject);
        else
            resultObject = convertObjectToJSONObject(elementObject);
        return resultObject;
    }

    // Special case for Base 64-encoded fields
    private static Object convertByteArrayToBase64JSONString(Object fieldObject) {
        return Base64.encodeToString((byte[]) fieldObject, false);    
    }
    
    // This is just to buffer the code from the exception. Better error
    //    handling needed here.
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
    
    // *** Create Messages from JSON *** //
    
    // Use the JSON-simple parser to create the JSON-Simple object
    private static JSONObject convertStringToJSONObject(String json) {
        JSONObject result = null;
        StringReader r = new StringReader(json);
        JSONParser jp = new JSONParser();
        try {
            result = (JSONObject) jp.parse(r);
        }
        catch (Throwable t) {
            System.out.println(t.getMessage());
        }
        r.close();        
        return result;
    }
    
    // A bit of a hack to create a consistent hierarchy with jsonbridge operations
    // At least it does not depend on any specific field names, it just copies the 
    // Indicator and Indicated fields.
    private static JSONObject wrap(JSONObject jo, Class c) {
        JSONObject result = new JSONObject();
        String indicatorName = Indication.getIndicatorName(c);
        String indicatedName = Indication.getIndicatedName(c);
        result.put(indicatorName, jo.get(indicatorName));
        result.put(indicatedName, jo);
        return result;
    }
            
    // Convert the JSON-Simple object to the indicated message, field-by-field
    //    recursively via convertElementToField.
    private static Message convertJSONObjectToMessage(JSONObject jo, Class c, Registry<Class> r) {
        //System.out.println("JSON.convertJSONObjectToMessage: " + jo.toJSONString());
        try {
            Message result = (Message) c.newInstance();
            for (Field f : c.getFields()) {
                Class fc = getFieldClass(result, jo, f, r);
                Object lookup = jo.get(f.getName());
                if (lookup != null) {
                    Object value = convertElementToField(lookup, fc, f, r);
                    f.set(result, value);
                }
            }      
            return result;
        }
        catch (Exception ex) {
            //ex.printStackTrace();
            return null;
        }        
    }

    // Convert the JSON-Simple array to the indicated message, element-by-element
    //    recursively via convertElementToField.
    private static Object convertJSONArrayToArray(JSONArray ja, Class c, Registry<Class> r) {
        Object result = Array.newInstance(c, ja.size());
        for (int i = 0; i < ja.size(); i++) {
            Object lookup = ja.get(i);
            Object value = null;
            if (lookup != null) {
                if (lookup.getClass().equals(JSONObject.class))
                    value = convertJSONObjectToMessage((JSONObject) lookup, c, r);
                else if (lookup.getClass().equals(JSONArray.class))  // this is not actually allowed in ROS
                    value = convertJSONArrayToArray((JSONArray) lookup, c.getComponentType(), r);
                else 
                    value = convertJSONPrimitiveToPrimitive(lookup, c);
                Array.set(result, i, value);
            }
        }
        
        return result;
    }
    
    // Convert a JSON-Simple array to a Message, field-by-field of the Message,
    //     element-by-element of the array, recursively via convertElementToField.
    //     NOTE: This relies on later versions of the JDK providing 
    //           the fields in order.
    private static Message convertJSONArrayToMessage(JSONArray ja, Class c, Registry<Class> r) {
        try {
            Message result = (Message) c.newInstance();
            int arrayIndex = 0;
            for (Field f : c.getFields()) {
                Class fc = getFieldClass(result, null, f, r);
                Object lookup = ja.get(arrayIndex++);         // yes we are assuming that the fields are delivered in order
                if (lookup != null) {
                    Object value = convertElementToField(lookup, fc, f, r);
                    f.set(result, value);
                }
            }
            
            return result;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }        
    }

    // Convert an individual array or object element to a field in the Message,
    //    recursively, and applying AsArray if needed.
    private static Object convertElementToField(Object element, Class fc, Field f, Registry<Class> r) {
        //System.out.println("JSON.convertElementToField: " + f.getName() + " " + fc.getName());
        Object value;
        if (element.getClass().equals(JSONObject.class)) {
            //System.out.println("JSON.convertElementToField: JSON Object " + ((JSONObject) element).toJSONString());
            value = convertJSONObjectToMessage((JSONObject) element, fc, r);
        }
        else if (element.getClass().equals(JSONArray.class)) {
            //System.out.println("JSON.convertElementToField: JSON Array " + ((JSONArray) element).toJSONString());
            if (Indication.asArray(f))
                value = convertJSONArrayToMessage((JSONArray) element, fc, r);
            else value = convertJSONArrayToArray((JSONArray) element, fc, r);
        }
        else {
            //System.out.println("JSON.convertElementToField: Primitive " + element);
            if (Indication.isBase64Encoded(f))
                value = convertBase64JSONStringToByteArray(element);
            else value = convertJSONPrimitiveToPrimitive(element, fc);
        }
         
        return value;        
    }
    
    // Note that this is not checking ranges
    public static Object convertJSONPrimitiveToPrimitive(Object o, Class c) {
        Object result = o;
        if (c.isPrimitive() || Number.class.isAssignableFrom(c)) {
            if (c.equals(double.class) || c.equals(Double.class))
                result = new Double(((Number) o).doubleValue());
            else if (c.equals(float.class) || c.equals(Float.class))
                result = new Float(((Number) o).floatValue());
            else if (c.equals(long.class) || c.equals(Long.class))
                result = new Long(((Number) o).longValue());
            else if (int.class.equals(c) || c.equals(Integer.class))
                result = new Integer(((Number) o).intValue());
            else if (c.equals(short.class) || c.equals(Short.class))
                result = new Short(((Number) o).shortValue());
            else if (c.equals(byte.class) || c.equals(Byte.class))
                result = new Byte(((Number) o).byteValue());
        }
        return result;
    }
    
    public static byte[] convertBase64JSONStringToByteArray(Object element) {
        return Base64.decode((String) element);
    }    

    // Determine the target class of a field in the object or array, based
    //    directly on the field's type, or using the Indicator if applicable,    
    //    The Indicator field only provides the topic/service, so we have to look
    //    up the Class in the registry.
    public static Class getFieldClass(Message parent, JSONObject jo, Field f, Registry<Class> r) {
        Class fc;
        fc = f.getType();
        if (fc.isArray())
            fc = f.getType().getComponentType();
        if (Indication.isIndicated(f) && (jo != null)) {
            //fc = Indication.getIndication(parent,
            //        (String) jo.get(Indication.getIndicatorName(parent.getClass())));
            fc = r.lookup(parent.getClass(),
                    (String) jo.get(Indication.getIndicatorName(parent.getClass())));
            //System.out.println("JSON.getFieldClass: parent class " + parent.getClass().getName() +
            //        " Indicator: " + Indication.getIndicatorName(parent.getClass()) + 
            //        " result: " + fc.getName());
        }
        return fc;
    }            
}
