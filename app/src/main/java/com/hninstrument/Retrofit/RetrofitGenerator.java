package com.hninstrument.Retrofit;



import com.hninstrument.Bean.DataFlow.PersonBean;
import com.hninstrument.Retrofit.InterfaceApi.CommonApi;
import com.hninstrument.Retrofit.InterfaceApi.FacetofaceApi;
import com.hninstrument.Retrofit.InterfaceApi.PersonInfoApi;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class RetrofitGenerator {

    private static final String webservicrUri = "http://113.140.1.133:10011";
    private static final String Uri = "http://113.140.1.133:10010";

    private static final String ServerUri = "http://192.168.12.165:7001";
    private static OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();

    private static FacetofaceApi facetofaceApi;

    private static CommonApi commonApi;
    private static PersonInfoApi personInfoApi;

    private static Strategy strategy = new AnnotationStrategy();
    private static Serializer serializer = new Persister(strategy);

    private static <S> S createWebService(Class<S> serviceClass) {
        okHttpClient.interceptors().add(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Content-Type", "text/xml;charset=UTF-8")   // 对于SOAP 1.1， 如果是soap1.2 应是Content-Type: application/soap+xml; charset=utf-8
                        .method(original.method(), original.body());
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        OkHttpClient client = okHttpClient.connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
               // .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(SimpleXmlConverterFactory.create(serializer))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(webservicrUri).client(client).build();
        return retrofit.create(serviceClass);
    }

    private static <S> S createService(Class<S> serviceClass) {
        okHttpClient.interceptors().add(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Content-Type", "text/xml;charset=UTF-8")// 对于SOAP 1.1， 如果是soap1.2 应是Content-Type: application/soap+xml; charset=utf-8
                        .method(original.method(), original.body());
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        OkHttpClient client = okHttpClient.connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(ServerUri).client(client).build();
        return retrofit.create(serviceClass);
    }



    public static FacetofaceApi getFacetofaceApi() {
        if (facetofaceApi == null) {
            facetofaceApi = createService(FacetofaceApi.class);
        }
        return facetofaceApi;
    }

    public static CommonApi getCommonApi() {
        if (commonApi == null) {
            commonApi = createService(CommonApi.class);
        }
        return commonApi;
    }


    public static PersonInfoApi getPersonInfoApi() {
        if (personInfoApi == null) {
            personInfoApi = createService(PersonInfoApi.class);
        }
        return personInfoApi;
    }
}
