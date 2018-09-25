package com.hninstrument.Builder;


import cbdi.drv.netDa.INetDaSocketEvent;
import cbdi.drv.netDa.NetDAM0888Socket;

public class SocketBuilder extends NetDAM0888Socket {

    public SocketBuilder setBuilderDATime(int time){
        setDATime(time);
        return SocketBuilder.this;
    }


    public SocketBuilder setBuilderEvent(INetDaSocketEvent event){
        setEvent(event);
        return SocketBuilder.this;
    }



    public SocketBuilder setBuilderNumber(int i){
       setNumber(i);
       return SocketBuilder.this;
    }

    public SocketBuilder builder_open(String ip, int port){
        open( ip, port);
        return SocketBuilder.this;
    }
}
