package com.hninstrument.Config;

/**
 * Created by zbsz on 2018/1/24.
 */

public class XA_Config extends BaseConfig {

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
        return false;
    }

    @Override
    public String getPersonInfoPrefix() {
        return "da_gzmb_persionInfo?";
    }

    @Override
    public String getServerId() {
        return "http://xajy.snaq.cn:8886/";
    }

    @Override
    public String getUpDataPrefix() {
        return "da_gzmb_updata?";
    }

    @Override
    public String getDev_prefix() {
        return "800100";
    }
//    @Override
//    public String getDev_prefix() {
//        return "800200";
//    }

    @Override
    public int getCheckOnlineTime() {
        return 60;
    }

//    @Override
//    public String getModel() {
//            return "CBDI-DA-01";
//    }

    @Override
    public String getModel() {
        return "CBDI-ID";
    }
    @Override
    public String getName() {
        return "库房采集器";
    }

    @Override
    public String getProject() {
        return "XAJD";
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
