package com.hninstrument.Config;

public class HNFB_Config extends BaseConfig {

    @Override
    public String hardware() {
        return "rk3288";
    }

    @Override
    public boolean isFace() {
        return true;
    }

    @Override
    public boolean isTemHum() {
        return false;
    }


//    @Override
//    public String getPersonInfoPrefix() {
//        return "da_gzmb_persionInfo?";
//    }
//
//    @Override
//    public String getServerId() {
//        return "http://124.172.232.87:8802/";
//    }
//
//    @Override
//    public String getUpDataPrefix() {
//        return "da_gzmb_updata?";
//    }


    @Override
    public String getPersonInfoPrefix() {
        return "cjy/s/fbcjy_updata?";
    }

    @Override
    public String getServerId() {
        return "http://hnyzb.wxhxp.cn:1093/";
    }

    @Override
    public String getUpDataPrefix() {
        return "cjy/s/fbcjy_updata?";
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
        return "防爆数据采集器";
    }

    @Override
    public String getProject() {
        return "HeNanFB";
    }

    @Override
    public String getPower() {
        return "12V 2A";
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
