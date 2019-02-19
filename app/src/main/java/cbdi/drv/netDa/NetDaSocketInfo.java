package cbdi.drv.netDa;

import java.util.HashMap;

/**
 * Created by Administrator on 2017-06-21.
 */

public class NetDaSocketInfo {
    public final int cmdType_ai=4; //查询8路模拟量
    public final int cmdType_di=2; //查询8路开关量
    public final int cmdType_do=1; //查询8路断电器状态
    public final int cmdType_ctrl=5; //断电器输出控制
    private HashMap<Integer,String> inStateType;
    public NetDaSocketInfo()
    {
        inStateType=new HashMap<Integer,String>();
        inStateType.put(0,"无");
        inStateType.put(1,"门开关");
        inStateType.put(2,"入侵报警");
    }

    public String getInStateTypeName(int i)
    {
        return inStateType.get(i);
    }
}
