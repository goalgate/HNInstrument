package com.hninstrument.Builder;

import cbdi.drv.netDa.SensorAI;

public class SensorAIBuilder extends SensorAI {


    public SensorAIBuilder setBuilderEnable(boolean enable) {
        setEnable(enable);
        return this;
    }

    public SensorAIBuilder setBuilderEnglishName(String englishName) {
        setEnglish_name(englishName);
        return this;
    }


    public SensorAIBuilder setBuilderName(String name) {
        setName(name);
        return this;
    }

    public SensorAIBuilder setBuilderUnit(String unit) {
        setUnit(unit);
        return this;
    }

    public SensorAIBuilder setBuilderMaxRange(float maxRange) {
        setMaxRange(maxRange);
        return this;
    }


    public SensorAIBuilder setBuilderMinRange(float minRange) {
        setMinRange(minRange);
        return this;
    }

    public SensorAIBuilder setBuilderMinVal(float minVal) {
        setMinVal(minVal);
        return this;
    }

    public SensorAIBuilder setBuilderMaxVal(float maxVal) {
        setMaxVal(maxVal);
        return this;
    }


    public SensorAIBuilder setSensorAIBuilderPrecision(int precision) {
       setPrecision(precision);
       return this;
    }

    public SensorAIBuilder setSensorAIBuilderAlarmMaxVal(float alarmMaxVal) {
        setAlarmMaxVal(alarmMaxVal);
        return this;
    }


    public SensorAIBuilder setSensorAIBuilderAlarmMinVal(float alarmMinVal) {
        setAlarmMinVal(alarmMinVal);
        return this;
    }






}
