package com.hninstrument.Function.Func_Switch.mvp.module;

/**
 * Created by zbsz on 2017/8/23.
 */

public interface ISwitching {

    void onOpen(ISwitchingListener listener);

    void onReadHum();

    void onOutD8(boolean status);

    void onOutD9(boolean status);

    void onBuzz(SwitchImpl.Hex hex);

    void onBuzzOff();

    void onDoorOpen();

    void onRedLightBlink();

    void onGreenLightBlink();
    interface ISwitchingListener{

        void onSwitchingText(String value);

        void onTemHum(int temperature, int humidity);

    }

}
