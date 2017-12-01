package com.hninstrument.Retrofit.Request.RequestModule;

import retrofit2.http.Part;

/**
 * Created by zbsz on 2017/11/29.
 */

public class PersonRecordBean {


    public PersonRecordBean(String dataType, String daid, String pass) {
        this.dataType = dataType;
        this.daid = daid;
        this.pass = pass;
    }

    /**
     * dataType :
     * daid :
     * pass :

     */

    private String dataType;
    private String daid;
    private String pass;




    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDaid() {
        return daid;
    }

    public void setDaid(String daid) {
        this.daid = daid;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
