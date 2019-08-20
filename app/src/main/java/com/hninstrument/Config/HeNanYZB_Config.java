package com.hninstrument.Config;

/**
 * Created by zbsz on 2017/12/19.
 */

public class HeNanYZB_Config extends BaseConfig {

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
        return  "cjy/s/fbcjy_updata?";
    }

    @Override
    public String getServerId() {
        return "http://hnyzb.wxhxp.cn:1093/";
    }

    @Override
    public String getUpDataPrefix() {
        return  "cjy/s/fbcjy_updata?";
    }

    @Override
    public String getDev_prefix() {
        return "800200";
    }

    @Override
    public int getCheckOnlineTime() {
        return 60;
    }

    @Override
    public String getModel() {
        return "CBDI-ID";
    }

    @Override
    public String getName() {
        return "数据采集器";
    }

    @Override
    public String getProject() {
        return "HNYZB";
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
        return false;
    }
}
