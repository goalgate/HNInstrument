package com.hninstrument.Config;

/**
 * Created by zbsz on 2017/12/19.
 */

public class    HB_Config extends BaseConfig {
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
        return "http://61.183.83.186:9901/";
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
        return "CBDI-ID-C";
    }

    @Override
    public String getName() {
        return "防爆采集器";
    }

    @Override
    public String getProject() {
        return "HBFB";
    }
}
