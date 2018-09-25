package cbdi.drv.netDa;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import cbdi.log.Lg;


/**
 * Created by Administrator on 2017-06-13.
 */

public class NetDAM0888Socket {

    private  final ThreadLocal<Socket> threadConnect = new ThreadLocal<Socket>();
    private String ip_ = "192.168.12.232";
    private   int port_ = 10000;
    private Socket client=null;
    private OutputStream outData = null; //发送数据流
    private InputStream inData = null;   //接收数据流
    private INetDaSocketEvent event_=null;
    private boolean enable_=true;  //功能是否启用
    private int openState_=0;  //设备打开状态
    private int number_=1;   //设备编号

    //发送命令类型
    private int cmdType_=0;
    private int cmdValue_=0;

    private boolean isAI=true; //

    //接收数据最后时间
    private long lastRevTime_= System.currentTimeMillis();;
    private Timer terCheck = new Timer(); //检测是否连接
    private final int checkTime_=30;//检测时间 x+5秒
    private Timer terDA = new Timer(); //采集时间
    private boolean isRev_=true;

    private NetDAM0888CMD cmd=new NetDAM0888CMD();
    private int[] data_ai=new int[8]; //8个模拟量采集值
    private byte data_di=0;
    private byte data_do=0;
    private NetDaSocketInfo netDaInfo=new NetDaSocketInfo();

    public NetDAM0888CMD getCmd() {
        return cmd;
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 4:
                    onAI();
                    break;
                case 1:
                    onDIO(cmd.cmdType_do);
                    break;
                case 2:
                    onDIO(cmd.cmdType_di);
                    break;
                case 12:
                    onOpen();
                    break;
                case 100:
                    checkConnect();
                    break;
                case 200:
                    getData();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    public String byteToHex(byte b) {
        String s = "";
        s = Integer.toHexString(0xFF&b).trim();
        if (s.length() < 2) {
            s = "0" + s;
        }

        return s.toUpperCase();
    }

    private void sendMessage(int ix)
    {
        Message message = new Message();
        message.what = ix;
        handler.sendMessage(message);
    }

    private  class RecvThread implements Runnable {
        public void run() {
            try {
                while (isRev_) {

                    if(openState_!=1||client==null||inData==null)
                    {
                        try {
                            Thread.sleep(100);
                        }catch (Exception ex){
                            Lg.e("NetDAM0888Socket.RecvThread.sleep",ex.toString());
                        }
                        continue;
                    }


                    byte[] b = new byte[128];
                    int r=0;
                    try {
                        r = inData.read(b);
                    }catch (Exception ex){
                        try {
                            Thread.sleep(100);
                        }catch (Exception exx){
                            Lg.e("NetDAM0888Socket.RecvThread.sleep",exx.toString());
                        }
                        Lg.e("NetDAM0888Socket.RecvThread.inData",ex.toString());
                        continue;
                    }

                    if(r>-1){
                        lastRevTime_= System.currentTimeMillis();
                        /*
                        String testStr="";
                        for(int i=0;i<r;i++)
                        {
                            testStr+=byteToHex(b[i]);
                        }
                        Lg.v("data:",testStr);
                        */
                        if(b[0]==(byte)0xfe)
                        {
                            if(b[1]==(byte)cmd.cmdType_ai)
                            {
                                for(int i=0;i<8;i++)
                                {
                                    data_ai[i]= b[4+i*2] & 0xFF |(b[3+i*2] & 0xFF) << 8;
                                            //(b[3+i*2]&0xff)*256+b[4+i*2]&0xff;
                                }
                                sendMessage(cmd.cmdType_ai);
                            }else if(b[1]==(byte)cmd.cmdType_di)
                            {
                                data_di=b[3];
                                sendMessage(cmd.cmdType_di);
                            }else if(b[1]==(byte)cmd.cmdType_do)
                            {
                                data_do=b[3];
                                sendMessage(cmd.cmdType_do);
                            }else if(b[1]==(byte)cmd.cmdType_ctrl)
                            {

                            }


                        }

                    }
                    /*
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }*/
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void getAI()
    {
        sendData(cmd.cmdType_ai,cmd.cmd_ai);
    }//读模拟量
    public void getDI()
    {
        sendData(cmd.cmdType_di,cmd.cmd_di);
    }//读开关量
    public void getDO()
    {
        sendData(cmd.cmdType_do,cmd.cmd_do);
    }//读继电器状态

    public void outCtrl(int i,boolean tr) //输出控制  0为第一路，true为开
    {
        byte[] bs=cmd.cmd_ctrl((byte)i,tr);
        if(bs!=null)
        {
            sendData(cmd.cmdType_ctrl,bs);
        }
    }


    public void getData()
    {
        if(isAI)
        {
            isAI=false;
            getAI();
        }else
        {
            isAI=true;
            getDI();

        }
    }

    public void checkConnect()
    {
        long l = (System.currentTimeMillis() - lastRevTime_) / 1000;
        if (l >checkTime_)
        {
            lastRevTime_= System.currentTimeMillis();
            new Thread(){
                @Override
                public void run()
                {
                    connect();
                    //把网络访问的代码放在这里
                }
            }.start();

        }
    }

    private TimerTask task = new TimerTask() {
        public void run() {
            Message message = new Message();
            message.what = 100;
            handler.sendMessage(message);
        }
    };

    private TimerTask daTask = new TimerTask() {
        public void run() {
            Message message = new Message();
            message.what = 200;
            handler.sendMessage(message);
        }
    };

    public void sendData(int iType,byte[] bs)
    {
        try
        {
            cmdType_=0;
            if(openState_==0){return;}
            if(outData!=null&&bs!=null)
            {
                cmdType_=iType;
                outData.write(bs);
                outData.flush();
            }
        }catch (Exception ex)
        {
            Lg.e("NetDAM0888Socket.sendData",ex.toString());
        }
    }




    private int connect()
    {
        try {
            /*
            if(client==null)
            {
                client = new Socket(ip_, port_);
                threadConnect.set(client);
            }else {
                client = threadConnect.get();
                if(client==null)
                {
                    client = new Socket(ip_, port_);
                    threadConnect.set(client);
                }
            }

            if((client!=null&&!client.isConnected())||(openState_==0))
            {
                InetSocketAddress socketAddress = new InetSocketAddress(ip_, port_);
                try {
                    client.close();
                }catch (Exception ex){ Lg.e("NetDAM0888Socket.connect_close",ex.toString());}
                client.connect(socketAddress,10000);
            }
            */
            client = new Socket(ip_, port_);
            threadConnect.set(client);

            isRev_=false;
            try {
                Thread.sleep(600);
            }catch (Exception ex1){ex1.printStackTrace();}
            isRev_=true;
            new Thread(new NetDAM0888Socket.RecvThread()).start();

            openState_=1;
            outData = client.getOutputStream();
            inData = client.getInputStream();

        }catch(Exception ex){
            openState_=0;
            Lg.e("NetDAM0888Socket.connect",ex.toString());
        }


        Message message = new Message();
        message.what = 12;
        handler.sendMessage(message);
        return openState_;

    }

    //打开状态信息
    private void onOpen()
    {
        if(enable_)
        {
            if(event_!=null) {
                try {
                    event_.onOpen(number_, openState_);
                }catch(Exception ex1){
                    Lg.e("NetDAM0888Socket.onOpen",ex1.toString());
                }
            }
        }
    }


    private void onAI()
    {
        if(enable_)
        {
            if(event_!=null) {
                try {
                    event_.onAI(number_,cmd.cmdType_ai,data_ai);
                }catch(Exception ex1){
                    Lg.e("NetDAM0888Socket.onInState",ex1.toString());
                }
            }
        }
    }


    private void onDIO(int ctype)
    {
        if(enable_)
        {
            if(event_!=null) {
                try {
                    if(ctype==cmd.cmdType_di)
                    {
                        event_.onCmd(number_,ctype,data_di);
                    }else if(ctype==cmd.cmdType_do)
                    {
                        event_.onCmd(number_,ctype,data_do);
                    }

                }catch(Exception ex1){
                    Lg.e("NetDAM0888Socket.onCmd",ex1.toString());
                }
            }
        }
    }

    //
   public void setDATime(int t)
   {

       if(t>500) {
           terDA.schedule(daTask, 1000, (int)(t / 2));
       }else
       {
           terDA.schedule(daTask, 1000,200);
       }
   }


    public void open(String ip, int port)  //连接设备
    {
        ip_=ip;
        port_=port;
        terCheck.schedule(task, 1000, 5000);
        new Thread(){
            @Override
            public void run()
            {
                connect();
                //把网络访问的代码放在这里
            }
        }.start();
        //return
    }

    public  int openState() //取设备状态  0为连接设备断开，1为连接设备
    {
        return openState_;
    }

    public void close()
    {
        try {
            isRev_=false;
            outData.close();
            inData.close();
            client.close();
        } catch (IOException e) {
            Lg.e("NetDAM0888Socket.close",e.toString());
            e.printStackTrace();
        }

    }

    public  void setNumber(int i) //设备采集器编号
    {
        number_=i;
    }

    public int getNumber()
    {
        return number_;
    }

    public  void enable(boolean tr) //是否启用 用户不同页面切换禁用开启
    {
        enable_=tr;
    }




    public String getInStateTypeName(int i)  //取开关量类型名称
    {
        return netDaInfo.getInStateTypeName(i);
    }

    public  void setEvent(INetDaSocketEvent event) //设置事件
    {
        event_=event;
    }
}
