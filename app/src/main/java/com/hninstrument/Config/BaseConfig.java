package com.hninstrument.Config;

/**
 * Created by zbsz on 2017/12/19.
 */

public abstract class BaseConfig {

    public abstract boolean isTemHum();

    public abstract boolean isFace();

    public abstract String getServerId();

    public abstract String getUpDataPrefix();

    public abstract String getPersonInfoPrefix();
}