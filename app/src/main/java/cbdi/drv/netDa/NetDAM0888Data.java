package cbdi.drv.netDa;


import com.hninstrument.Builder.SensorAIBuilder;

/**
 * Created by Administrator on 2018-07-07.
 */

public class NetDAM0888Data {

    private SensorAIBuilder[] data_ai=new SensorAIBuilder[8];  //模拟量
    private SensorDIO[] data_di=new SensorDIO[8]; //开关量
    private SensorDIO[] data_do=new SensorDIO[8]; //继电器状态

    //采集原值
    private int[] aiVal=null;
    private byte diVal=0;
    private byte doVal=0;



    private byte[] inStateBit=new byte[]{1,2,4,8,16,32,64,(byte)128};//输入位对应的比值

    public  NetDAM0888Data()
    {
        for(int i=0;i<8;i++)
        {
            data_ai[i]=new SensorAIBuilder();
            data_di[i]=new SensorDIO();
            data_do[i]=new SensorDIO();
        }
    }

    public void setAI(int[] val)
    {
        aiVal=val;
        if(val!=null&&val.length>=8)
        {
            for(int i=0;i<8;i++)
            {
                data_ai[i].setCollectionVal(val[i]);
            }
        }
    }

    public void setDI(byte b)
    {
            diVal=b;
            byte bx=0;
            for(int i=0;i<8;i++)
            {
                bx=(byte)(b&inStateBit[i]);
                if(bx==inStateBit[i])
                {
                    data_di[i].setVal((byte)1);
                }else
                {
                    data_di[i].setVal((byte)0);
                }
            }

    }

    public void setDO(byte b)
    {
        diVal=b;
        byte bx=0;
        for(int i=0;i<8;i++)
        {
            bx=(byte)(b&inStateBit[i]);
            if(bx==inStateBit[i])
            {
                data_do[i].setVal((byte)1);
            }else
            {
                data_do[i].setVal((byte)0);
            }
        }

    }

    //取模拟量
    public SensorAIBuilder  getAI(int i)
    {
        if(i>=0&&i<8) {
            return data_ai[i];
        }else
        {
            return null;
        }
    }

    //取开关量
    public SensorDIO  getDI(int i)
    {
        if(i>=0&&i<8) {
            return data_di[i];
        }else
        {
            return null;
        }
    }

    //继电器状态
    public SensorDIO  getDO(int i)
    {
        if(i>=0&&i<8) {
            return data_do[i];
        }else
        {
            return null;
        }
    }

}
