package com.jilk.ros.message;

/**
 * Created by xxhong on 16-11-17.
 */
@MessageType(string = "std_msgs/String")
public class SemanticRequest extends Message {
    public  SemanticRequest(String args) {
        jsonStr = args;
    }

    public String jsonStr;
}
