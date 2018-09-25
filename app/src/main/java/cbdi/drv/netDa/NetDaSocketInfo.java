package cbdi.drv.netDa;

import java.util.HashMap;

/**
 * Created by Administrator on 2017-06-21.
 */

public class NetDaSocketInfo {
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
