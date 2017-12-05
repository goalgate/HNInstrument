package com.hninstrument.Retrofit.InterfaceApi;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Created by zbsz on 2017/11/29.
 */

public interface CommonApi {

    @Headers({"Content-Type: application/json","Accept: application/json"})//需要添加头
    @POST("/daServer/da_gzmb_updata")
    //Observable<CommonResponseBean> CommonRequest(@QueryMap Map<String, Object> params);
    Observable<String> CommonRequest(@QueryMap Map<String, Object> params);
}
