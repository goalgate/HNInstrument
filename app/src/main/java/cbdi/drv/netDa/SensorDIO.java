package cbdi.drv.netDa;

import java.util.Calendar;

/**
 * Created by Administrator on 2018-07-07.
 */

public class SensorDIO {
    private String sensorModel=""; //传感器类型号
    private String name="";  //传感器名称
    private int val=-1;  //f0关  1开
    private int id=1; //采集卡ID
    private int channel=-1; //通道号
    private boolean enable=false;
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
    private Calendar time=null; //采集时间

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
          }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public Calendar getTime() {
        return time;
    }

    public void setTime(Calendar time) {
        this.time = time;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }
}
