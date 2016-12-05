package com.jilk.ros.message;

/**
 * Created by xxhong on 16-11-17.
 */
@MessageType(string = "std_msgs/Int16MultiArray")
public class AudioMsg extends Message {
    public short[] data;
}
