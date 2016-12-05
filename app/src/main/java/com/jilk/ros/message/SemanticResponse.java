package com.jilk.ros.message;

/**
 * Created by xxhong on 16-11-17.
 */
@MessageType(string = "std_msgs/String")
public class SemanticResponse extends Message {
    public String result;
}
