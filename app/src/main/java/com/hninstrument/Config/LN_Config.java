package com.hninstrument.Config;

public class LN_Config extends BaseConfig {
    @Override
    public String hardware() {
        return "rk3128";
    }

    @Override
    public boolean isFace() {
        return false;
    }

    @Override
    public boolean isTemHum() {
        return false ;
    }

    @Override
    public String getPersonInfoPrefix() {
        return "ewln_persionInfo.do?";
    }

    @Override
    public String getServerId() {
        return "http://124.172.232.89:8050/daServer/";
    }

    @Override
    public String getUpDataPrefix() {
        return "ewln_updata.do?";
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
        return "LNFB";
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
