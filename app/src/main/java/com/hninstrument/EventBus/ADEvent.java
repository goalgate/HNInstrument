package com.hninstrument.EventBus;

import cbdi.drv.netDa.NetDAM0888Data;

public class ADEvent {

    String message;

    public NetDAM0888Data getData() {
        return data;
    }

    NetDAM0888Data data;

    public String getMessage() {
        return message;
    }

    public ADEvent(NetDAM0888Data data, String message) {
        this.data = data;
        this.message = message;
    }
}
