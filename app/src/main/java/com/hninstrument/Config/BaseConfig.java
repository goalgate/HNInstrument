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

    public abstract String getDev_prefix();

    public abstract int getCheckOnlineTime();

    public abstract String getName();

    public abstract String getModel();

    public abstract String getProject();

    public abstract String getPower();

    public abstract boolean isCheckTime();

    public abstract boolean isGetOneShot();

}
