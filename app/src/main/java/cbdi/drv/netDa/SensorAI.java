package cbdi.drv.netDa;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Created by Administrator on 2018-07-07.
 */

public class SensorAI {
    //传感器类型 0ma20 输出0到20ma  4ma20输出4到20ma  0v10 输出0到10V  DI为开关量
    public final int sensorType_4ma20=1; //传感类型为4-20ma输入
    public final int sensorType_0ma20=2; //传感类型为4-20ma输入
    public final int sensorType_0v10=3; //传感类型为0-10V输入


    private boolean enable=false;
    private float minVal=4; //输出最小值
    private float maxVal=20;//输出最大值
    private float scaleVal=1000; //采集值比例
    private float alarmMinVal=-1;  //最小报警值(-1时不报警)
    private float alarmMaxVal=-1;  //最大报警值(-1时不报警)
    private float changeVal=-1; //变化值

    public float getChangeVal() {
        return changeVal;
    }

    public void setChangeVal(float changeVal) {
        this.changeVal = changeVal;
    }

    public float getAlarmMinVal() {
        return alarmMinVal;
    }

    public void setAlarmMinVal(float alarmMinVal) {
        this.alarmMinVal = alarmMinVal;
    }

    public float getAlarmMaxVal() {
        return alarmMaxVal;
    }

    public void setAlarmMaxVal(float alarmMaxVal) {
        this.alarmMaxVal = alarmMaxVal;
    }

    public int getCollectionCycle() {
        return collectionCycle;
    }

    public void setCollectionCycle(int collectionCycle) {
        this.collectionCycle = collectionCycle;
    }

    private int collectionCycle=60;//采集周期
    private int sensorType=sensorType_4ma20;  //传感器类型
    private String sensorModel=""; //传感器类型号


    public float getScaleVal() {
        return scaleVal;
    }

    public void setScaleVal(float scaleVal) {
        this.scaleVal = scaleVal;
    }

    private String name="液位";  //传感器名称
    private String english_name = "Liquidlevel";



    private String unit="米";  //值单位
    private int precision=2;  //精度
    private float collectionVal=0; //采集值
    private float val=0;  //值

    private float minRange=0;//量程最小值
    private float maxRange=5;//量程最大值
    private Calendar time=null;  //采集时间

    public Calendar getTime() {
        return time;
    }

    public void setTime(Calendar time) {
        this.time = time;
    }

    public float getMinVal() {
        return minVal;
    }

    public void setMinVal(float minVal) {
        this.minVal = minVal;
    }

    public float getMaxVal() {
        return maxVal;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setMaxVal(float maxVal) {
        this.maxVal = maxVal;
    }

    public int getSensorType() {
        return sensorType;
    }

    public void setSensorType(int sensorType) {
        this.sensorType = sensorType;
    }

    public String getSensorModel() {
        return sensorModel;
    }

    public void setSensorModel(String sensorModel) {
        this.sensorModel = sensorModel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEnglish_name(String english_name) {
        this.english_name = english_name;
    }

    public String getEnglish_name() {

        return english_name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public float getCollectionVal() {
        return collectionVal;
    }

    //设备是否有故障，true有故障
    public boolean isAIError()
    {
        Calendar cd = Calendar.getInstance();
        if(time==null){return true;}
        if((cd.getTimeInMillis()-time.getTimeInMillis())>collectionCycle*2000)
        {
            return true;
        }else
        {
            if(collectionVal<minVal*scaleVal*0.8||collectionVal>maxVal*scaleVal*1.2)
            {
                return true;
            }else
            {
                return false;
            }
        }


    }

    public void setCollectionVal(float collectionVal) {
        this.collectionVal = collectionVal;
        if(enable)
        {
            try {
                time=Calendar.getInstance();
                if(collectionVal<minVal*(scaleVal-scaleVal*0.05))
                {
                    val=0;
                }else {
                    val = ((Math.abs((collectionVal) - minVal*scaleVal)) * ((maxRange - minRange) / (maxVal - minVal))) / scaleVal;
                }
            }catch(Exception ex){
                val=-1;
                ex.printStackTrace();
            }
        }
    }

    public BigDecimal getVal() {
        BigDecimal b  =   new BigDecimal(val);
        int ix=precision;
        if(ix<0&&ix>10){
            ix=2;
        }
        return b.setScale(ix, BigDecimal.ROUND_HALF_UP);//.floatValue();
    }

    public void setVal(float val) {
        this.val = val;
    }


    public float getMinRange() {
        return minRange;
    }

    public void setMinRange(float minRange) {
        this.minRange = minRange;
    }

    public float getMaxRange() {
        return maxRange;
    }

    public void setMaxRange(float maxRange) {
        this.maxRange = maxRange;
    }
}
