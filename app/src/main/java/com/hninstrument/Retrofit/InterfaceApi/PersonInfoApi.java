package com.hninstrument.Retrofit.InterfaceApi;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Created by zbsz on 2017/12/6.
 */

public interface PersonInfoApi {

    @Headers({"Content-Type: application/json","Accept: application/json"})//需要添加头
    @POST("/daServer/da_gzmb_persionInfo")
    Observable<String> CommonRequest(@QueryMap Map<String, Object> params);
}
