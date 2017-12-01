package com.hninstrument.Retrofit.InterfaceApi;

import com.hninstrument.Retrofit.Response.ResponseModule.CompareResponseBean;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by zbsz on 2017/11/29.
 */

public interface PersonRecordApi {

  /*  @Headers({"multipart/form-data"})//需要添加头*/
    @Multipart
    @POST("/daServer/da_gzmb_updata")
    Observable<ByteArrayOutputStream> personRecord(@QueryMap Map<String, Object> maps , @Part MultipartBody.Part stream);
  //Observable<ByteArrayOutputStream> personRecord(@PartMap Map<String, RequestBody> map);

}
