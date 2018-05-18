package com.hninstrument.Config;

public class HeiBei_Config extends BaseConfig {
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
        return "http://113.140.1.136:8601/";
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
        return "HeiBeiFB";
    }

    @Override
    public String getPower() {
        return "12-18V 2A";
    }

    @Override
    public boolean getCheckTime() {
        return false;
    }
}
