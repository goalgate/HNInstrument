package com.hninstrument.Config;

/**
 * Created by zbsz on 2017/12/19.
 */

public class HB_Config extends BaseConfig {
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
        return "http://124.172.232.87:8802/";
    }

    @Override
    public String getPersonInfoPrefix() {
        return "da_gzmb_persionInfo?";
    }
}
