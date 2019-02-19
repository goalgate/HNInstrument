package cbdi.drv.netDa;

import android.os.Handler;
import android.os.Message;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import cbdi.log.Lg;


/**
 * 北京神州太讯科技有限公司
 */

public class NetDACVCIP608Socket {

    private  final ThreadLocal<Socket> threadConnect = new ThreadLocal<Socket>();
    private   int port_ = 5000;
    private DatagramSocket server = null;
    private byte[] container = new byte[1024];
    private NetDaSocketInfo netDaInfo=new NetDaSocketInfo();
    private SensorDIO di_=new SensorDIO();

    private INetDaSocketEvent event_=null;
    private boolean enable_=true;  //功能是否启用
    private int openState_=0;  //设备打开状态
    private int number_=1;   //设备编号

    //接收数据最后时间
    private long lastRevTime_=System.currentTimeMillis();;
    private Timer terCheck = new Timer(); //检测是否连接
    private final int checkTime_=600;//检测时间 x+5秒
    private boolean isRev_=true;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    onDIO();
                    break;
                case 12:
                    onOpen();
                    break;
                case 100:
                    checkConnect();
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

    private void clearData()
    {
        di_.setEnable(false);
        di_.setChannel(-1);
        di_.setVal(-1);

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

                    if(openState_!=1||server==null)
                    {
                        try {
                            Thread.sleep(100);
                        }catch (Exception ex){
                            Lg.e("NetDACVCIP608Socket.RecvThread.sleep",ex.toString());
                        }
                        continue;
                    }

                    try {
                        DatagramPacket packet = new DatagramPacket(container, container.length);
                        //4.接受数据
                        server.receive(packet);
                        //5.分析数据
                        byte[] data = packet.getData();
                        int len = packet.getLength();
                        if(len>-1){
                            lastRevTime_=System.currentTimeMillis();
                            //00 0A FF F5 45 20 30 30 31 30 20 31 20 33 0D

                            if(len>=15)
                            {
                                if((data[2]&0xff)==0xff&&data[4]==0x45)
                                {
                                      int ix=(data[11]&0xff)-0x30;
                                      if(ix>0&&ix<9) {
                                          clearData();
                                          di_.setEnable(true);
                                          di_.setChannel(ix);
                                          di_.setTime(Calendar.getInstance());
                                          ix=data[1]&0xff;
                                          di_.setId(ix);
                                          if(data[13]==0x33)
                                          {
                                              di_.setVal(0);
                                          }else
                                          {
                                              di_.setVal(1);
                                          }
                                          Message message = new Message();
                                          message.what = 2;
                                          handler.sendMessage(message);
                                      }
                                }
                            }
                        }else
                        {
                            try {
                                Thread.sleep(100);
                            }catch (Exception exx){
                                Lg.e("NetDACVCIP608Socket.RecvThread.sleep",exx.toString());
                            }
                        }

                    }catch (Exception ex){
                        try {
                            Thread.sleep(100);
                        }catch (Exception exx){
                            Lg.e("NetDACVCIP608Socket.RecvThread.sleep",exx.toString());
                        }
                        Lg.e("NetDACVCIP608Socket.RecvThread.inData",ex.toString());
                        continue;
                    }

                    }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    public SensorDIO getDI()
    {
        return di_;
    }


    public void checkConnect()
    {
        long l = (System.currentTimeMillis() - lastRevTime_) / 1000;
        if (l >checkTime_)
        {
            lastRevTime_=System.currentTimeMillis();
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

    private int connect()
    {
        try {
            isRev_=false;
            try {
                Thread.sleep(200);
            }catch (Exception ex1){ex1.printStackTrace();}
            if(server!=null){
                try
                {
                    server.close();
                }catch (Exception ex){};
            };
            server = new DatagramSocket(port_);
            isRev_=true;
            new Thread(new NetDACVCIP608Socket.RecvThread()).start();

            openState_=1;

        }catch(Exception ex){
            openState_=0;
            Lg.e("NetDACVCIP608Socket.connect",ex.toString());
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
                    Lg.e("NetDACVCIP608Socket.onOpen",ex1.toString());
                }
            }
        }
    }



    private void onDIO()
        {
        if(enable_)
        {
            if(event_!=null) {
                try {
                    //event_.onCmd(1,1,(byte)1);
                    if(di_.isEnable()&&di_.getChannel()>=0&&di_.getVal()>=0) {
                        event_.onCmd(di_.getId() * 1000 + di_.getChannel(), netDaInfo.cmdType_di, (byte) di_.getVal());
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                };
            }
        }
    }

    //
   public void setDATime(int t)
   {

   }


    public void open(int port)  //连接设备
    {
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
    }

    public  int openState() //取设备状态  0为连接设备断开，1为连接设备
    {
        return openState_;
    }

    public void close()
    {
        try {
            isRev_=false;
            server.close();
        } catch (Exception e) {
            Lg.e("NetDACVCIP608Socket.close",e.toString());
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

    public  void setEvent(INetDaSocketEvent event) //设置事件
    {
        event_=event;
    }
}
