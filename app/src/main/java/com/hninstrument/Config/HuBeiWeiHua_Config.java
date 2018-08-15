package com.hninstrument.Config;

/**
 * Created by zbsz on 2018/3/27.
 */

public class HuBeiWeiHua_Config extends BaseConfig{
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
        return "cjy_updata?";
    }

    @Override
    public String getServerId() {
        return "http://221.232.80.185/";
    }

    @Override
    public String getPersonInfoPrefix() {
        return "cjy_updata?";
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
        return "HBWH";
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
}
