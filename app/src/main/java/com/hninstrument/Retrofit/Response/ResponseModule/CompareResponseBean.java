package com.hninstrument.Retrofit.Response.ResponseModule;

/**
 * Created by zbsz on 2017/10/26.
 */

public class CompareResponseBean {


    /**
     * similar : 0.4487449
     * result : true
     * errorinfo :
     */

    private double similar;
    private boolean result;
    private String errorinfo;

    public double getSimilar() {
        return similar;
    }

    public void setSimilar(double similar) {
        this.similar = similar;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getErrorinfo() {
        return errorinfo;
    }

    public void setErrorinfo(String errorinfo) {
        this.errorinfo = errorinfo;
    }
}
