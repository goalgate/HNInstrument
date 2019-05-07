package com.hninstrument.Config;

/**
 * Created by zbsz on 2017/12/19.
 */

public class HuBeiFB_Config extends BaseConfig {
    @Override
    public boolean isTemHum() {
        return true;
    }

    @Override
    public boolean isFace() {
        return false;
    }

    @Override
    public String getDev_prefix() {
        return "800100";
    }

    @Override
    public String getUpDataPrefix() {
        return "da_gzmb_updata?";
    }

    @Override
    public String getServerId() {
        return "http://116.211.86.147:25018/";
    }

    @Override
    public String getPersonInfoPrefix() {
        return "da_gzmb_persionInfo?";
    }

    @Override
    public int getCheckOnlineTime() {
        return 10;
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
        return "HuBeiFB";
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
        return false;
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
