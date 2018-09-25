package cbdi.drv.netDa;

/**
 * Created by Administrator on 2017-06-13
 * 网络采集卡接口
 * 注：在AndroidManifest.xml加入网络访问权限
 * <uses-permission android:name="android.permission.INTERNET"></uses-permission>
 */

public interface  INetDaSocket {
    public abstract void open(String ip, int port);  //连接设备
    public abstract int openState();  //设备连接状态  0为连接设备断开，1为连接设备
    public abstract void close(); //关闭设备
    public abstract void setNumber(int i); //设备采集器编号
    public abstract int getNumber(); //取设备采集器编号
    public abstract void enable(boolean tr); //是否启用 用户不同页面切换禁用开启
    public abstract byte inStates(int i); //开关量状态   一个字节为8位   i为第几个8位 默认为0，也就是第一个8位开关量
    public abstract boolean inState(int i); //开关量状态  true为1，false为0  i为第几路开关量
    public abstract void setInStateType(int i, int itype);  //设置开关量类型 i为第几路   itype 开关量类型值
    /*##############################################
     itype开关量类型值
     1门开关
     2为入侵报警
     ############################################*/
    public abstract int getInStateType(int i);  //取开关量类型
    public abstract String getInStateTypeName(int i);  //取开关量类型名称
    public abstract boolean ctrlOut(int i, boolean state); //开关量输出控制    i为第几路输出  state true为闭合，false为断开
    public abstract float inValue(int i); //模拟量输入  i为第几路
    public abstract void setInValueType(int i, int itype);  //设置模拟量类型 i为第几路   itype 模拟量类型值
    /*###########################################
    itype模拟量类型值
     1温度
     2湿度
     ############################################*/
    public abstract int getInValueType(int i);  //取模拟量类型 i为第几路
    public abstract String getInValueTypeName(int i);  //取模拟量类型名称 i为第几路
    public abstract String getInValueUnit(int i);  //取模拟量单位 i为第几路
    public abstract void setEvent(INetDaSocketEvent event); //设置事件

    //public abstract void inStateSetAlarm(int i,int itype);//开关量,采集器设防 i
    //public abstract void inStateCancelAlarm(int i);//开关量,采集器撤防 ;


    /**********************************************************

     public void open(String ip,int port)  //连接设备
    {

    }

     public  int openState() //取设备状态  0为连接设备断开，1为连接设备
     {
         return -1;
     }

     public  void close() //关闭设备
     {

     }

     public  void setNumber(int i) //设备采集器编号
     {

     }


     public  int getNumber() //取设备采集器编号
     {
       return 0;
     }

     public  void enable(boolean tr) //是否启用 用户不同页面切换禁用开启
     {

     }

     public  byte inStates(int i)  //开关量状态   一个字节为8位   i为第几个8位 默认为0，也就是第一个8位开关量
     {
     return 0;
     }

     public  boolean inState(int i)  //开关量状态  true为1，false为0  i为第几路开关量
     {
        return false;
     }

     public  void setInStateType(int i,int itype)  //设置开关量类型 i为第几路   itype 开关量值
     {

     }
     public  int  getInStateType(int i)  //取开关量类型
     {
        return 0;
     }

     public  String getInStateTypeName(int i)  //取开关量类型名称
     {
        return "";
     }


     public  boolean ctrlOut(int i,boolean state) //开关量输出控制    i为第几路输出  state true为闭合，false为断开
     {
       return false;
     }

     public  float inValue(int i) //模拟量输入  i为第几路
     {
        return 0;
     }

     public  void setInValueType(int i,int itype)  //设置模拟量类型 i为第几路   itype 模拟量类型值
     {

     }

    public  int getInValueType(int i) //取模拟量类型 i为第几路
    {
        return 0;
    }

     public  String getInValueTypeName(int i) //取模拟量类型名称 i为第几路
     {
        return "";
     }

     public  String getInValueUnit(int i)  //取模拟量单位 i为第几路
     {
        return "";
     }

     public  void setEvent(INetDaSocketEvent event) //设置事件
     {

     }

     ****************************************************************/

}
