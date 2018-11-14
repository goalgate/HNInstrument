package com.hninstrument.Config;

public class SHGJ_Config extends BaseConfig{
    @Override
    public boolean isFace() {
        return true;
    }

    @Override
    public boolean isTemHum() {
        return false;
    }

    @Override
    public String getPersonInfoPrefix() {
        return "da_gzmb_persionInfo?";
    }

    @Override
    public String getServerId() {
        return "http://shjd.snaq.cn:47002/";
    }

    @Override
    public String getUpDataPrefix() {
        return "da_gzmb_updata?";
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
        return "GDMB";
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
        return false;
    }

    @Override
    public boolean collectBox() {
        return false;
    }

    @Override
    public boolean noise() {
        return false;
    }
}
