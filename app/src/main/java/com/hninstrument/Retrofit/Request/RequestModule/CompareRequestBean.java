package com.hninstrument.Retrofit.Request.RequestModule;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zbsz on 2017/10/26.
 */

public class CompareRequestBean {


    public CompareRequestBean(String _$Faceimage1283, String _$Faceimage239) {
        this._$Faceimage1283 = _$Faceimage1283;
        this._$Faceimage239 = _$Faceimage239;
    }

    @SerializedName("faceimage1")
    private String _$Faceimage1283; // FIXME check this code
    @SerializedName("faceimage2")
    private String _$Faceimage239; // FIXME check this code

    public String get_$Faceimage1283() {
        return _$Faceimage1283;
    }

    public void set_$Faceimage1283(String _$Faceimage1283) {
        this._$Faceimage1283 = _$Faceimage1283;
    }

    public String get_$Faceimage239() {
        return _$Faceimage239;
    }

    public void set_$Faceimage239(String _$Faceimage239) {
        this._$Faceimage239 = _$Faceimage239;
    }
}
