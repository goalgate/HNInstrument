package com.hninstrument.Retrofit.InterfaceApi;


import com.hninstrument.Retrofit.Response.ResponseModule.CompareResponseBean;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by zbsz on 2017/10/26.
 */

public interface FacetofaceApi {
    @Headers({"Content-Type: application/json","Accept: application/json"})//需要添加头
    @POST("/api/face2face")
    Observable<CompareResponseBean> facetoface(@Body RequestBody param);
}
