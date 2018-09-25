package cbdi.drv.netDa;

/**
 * Created by Administrator on 2018-07-05.
 */

public class NetDAM0888CMD {
    public final int cmdType_ai=4; //查询8路模拟量
    public final int cmdType_di=2; //查询8路开关量
    public final int cmdType_do=1; //查询8路断电器状态
    public final int cmdType_ctrl=5; //断电器输出控制



    public byte[] cmd_ai ={ (byte)0xFE,0x04,0x00,0x00,0x00,0x08,(byte)0xE5,(byte)0xC3};  //查询8路模拟量
    public byte[] cmd_di ={ (byte)0xFE,0x02,0x00,0x00,0x00,0x08,0x6D,(byte)0xC3};  //查询8路开关量状态
    public byte[] cmd_do ={ (byte)0xFE,0x01,0x00,0x00,0x00,0x08,0x29,(byte)0xC3};  //查询8路断电器状态


    public byte[] cmd_ctrl(byte i,boolean tr)  //true 为开
    {
        byte[] bs={(byte)0xFE,0x05,0x00,0x00,(byte)0xFF,0x00,(byte)0x98,0x35};
        if(i>=0&&i<8)
        {
           bs[3]=i;
        }else
        {
           return null;
        }
        if(!tr)
        {
            bs[4]=0x00;
        }
        byte[] bc=crc16(bs,6);
        bs[6]=bc[0];
        bs[7]=bc[1];
        return bs;
    }

    public  int calcCRC16(byte[] pArray,int length)
    {
        int wCRC = 0xFFFF;
        int CRC_Count = length;
        int i;
        int num = 0;
        while (CRC_Count > 0)
        {
            CRC_Count--;
            wCRC = wCRC ^ (0xFF & pArray[num++]);
            for (i = 0; i < 8; i++)
            {
                if ((wCRC & 0x0001) == 1)
                {
                    wCRC = wCRC >> 1 ^ 0xA001;
                }
                else
                {
                    wCRC = wCRC >> 1;
                }
            }
        }
        return wCRC;
    }

    public  byte[] crc16(byte[] data,int len)
    {
        int ix=calcCRC16(data,len);
        byte[] bs=new byte[2];
        bs[1]=(byte)(ix/256);
        bs[0]=(byte)(ix%256);
        return bs;
    }



}
