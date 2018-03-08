package com.hninstrument.Tools;

/**
 * Created by zbsz on 2018/3/8.
 */

public class Checked {
    boolean flag;

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public boolean isFlag() {
        return flag;
    }

    private static Checked instance=null;
    private Checked(){}
    public static Checked getInstance() {
        if(instance==null)
            instance=new Checked();
        return instance;
    }



}
