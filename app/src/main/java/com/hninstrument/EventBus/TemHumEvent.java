package com.hninstrument.EventBus;

/**
 * Created by zbsz on 2017/12/19.
 */

public class TemHumEvent {
    private int Tem;

    private int Hum;

    public int getTem(){
        return Tem;
    }

    public int getHum(){
        return Hum;
    }

    public TemHumEvent(int tem, int hum){
        this.Tem = tem;
        this.Hum = hum;

    }
}
