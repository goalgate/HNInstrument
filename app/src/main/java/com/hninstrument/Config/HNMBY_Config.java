package com.hninstrument.Config;

public class HNMBY_Config extends BaseConfig {
    @Override
    public boolean isFace() {
        return false;
    }

    @Override
    public boolean isTemHum() {
        return true;
    }

    @Override
    public String getPersonInfoPrefix() {
        return null;
    }

    @Override
    public String getServerId() {
        return "http://119.29.111.172:7001/";
    }

    @Override
    public String getUpDataPrefix() {
        return null;
    }

    @Override
    public String getDev_prefix() {
        return "800100";
    }

    @Override
    public int getCheckOnlineTime() {
        return 60;
    }

    @Override
    public String getModel() {
        return "CBDI-DA-01";
    }

    @Override
    public String getName() {
        return "防爆采集器";
    }

    @Override
    public String getProject() {
        return "HNMBY";
    }

    @Override
    public String getPower() {
        return "12-18V 2A";
    }

    @Override
    public boolean isCheckTime() {
        return false;
    }

    @Override
    public boolean isGetOneShot() {
        return true;
    }

    @Override
    public boolean disAlarm() {
        return true;
    }

    @Override
    public boolean collectBox() {
        return false;
    }

    @Override
    public boolean noise() {
        return false;
    }

    @Override
    public boolean doubleCheck() {
        return true;
    }
}
