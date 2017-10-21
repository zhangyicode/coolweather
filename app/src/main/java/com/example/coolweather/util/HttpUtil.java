package com.example.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Administrator on 2017/10/19 0019.
 */
/*
调用该工具类会返回到Callback接口，在回调方法中处理返回值response
 */
public class HttpUtil {//进行网络请求的工具类

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
            //OkHttp3出色的封装，在enqueue()方法中开启了子线程进行网络请求，并将请求结果回调到okhttp3.Callback中
    }
}
