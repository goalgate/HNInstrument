package cbdi.drv.netDa;

/**
 * Created by Administrator on 2017-06-13.
 */

public interface INetDaSocketEvent {
    public abstract void onOpen(int num, int state);  //打开状态 num 连接编号  state状态  1为连接 0为断开
    public abstract void onCmd(int num, int cmdType, byte value);  //命令回应  num 连接编号 cmdType命令类型   value返回值
    public abstract void onAI(int num, int cmdType, int[] value);  //命令回应  num 连接编号 cmdType命令类型   value返回值



    /*********************************************************************
     public  void onOpen(int num,int state)  //打开状态 num 连接编号  state状态  1为连接 0为断开
     {

     }

     public  void onCmd(int num,int cmdType, value)  //命令回应  num 连接编号 cmdType命令类型   value返回值
     {

     }

     public  void onAI(int num, int cmdType, int[] value)  //命令回应  num 连接编号 cmdType命令类型   value返回值
     {

     }


     *********************************************************************/

}
