package com.hninstrument.EventBus;

/**
 * Created by zbsz on 2017/9/11.
 */

public class NetworkEvent {

    boolean network_state;

    String msg;

    public boolean getNetwork_state(){
        return network_state;
    }

    public String getMsg(){
        return msg;
    }

    public NetworkEvent(boolean network_state ,String msg){
        this.network_state = network_state;
        this.msg = msg;
    }



}
