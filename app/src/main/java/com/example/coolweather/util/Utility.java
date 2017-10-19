package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/10/19 0019.
 */

public class Utility {//解析服务器返回JSON格式数据的工具类

    /*
    *解析处理服务器返回的省份级别数据
     */
    public static boolean handleProvinceResponse(String response){//接受一个服务器返回的response参数
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces=new JSONArray(response);//将服务器返回的数据传入JSONArray中
                for(int i=0;i<allProvinces.length();i++){//遍历JSONArray
                    JSONObject provinceObject=allProvinces.getJSONObject(i);//从JSONArray取出一个JSONObject对象
                    Province province=new Province();
                    province.setProvinceName(provinceObject.getString("name"));//获取省份名字数据并给provinceName赋值
                    province.setProvinceCode(provinceObject.getInt("id"));//获取省份代号数据并给provinceCode赋值
                    province.save();//调用save()方法将实体类数据保存到LitePal数据库中
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    *解析处理服务器返回的市级别数据
     */
    public static boolean handleCityResponse(String response,int provinceId){//接收服务器返回的数据，省份id
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities=new JSONArray(response);
                for(int i=0;i<allCities.length();i++){
                    JSONObject cityObject=allCities.getJSONObject(i);
                    City city=new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
   *解析处理服务器返回的县级别数据
    */
    public static boolean handleCountyResponse(String response,int cityId){//接收服务器返回的数据，城市id
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties=new JSONArray(response);
                for(int i=0;i<allCounties.length();i++){
                    JSONObject countyObject=allCounties.getJSONObject(i);
                    County county=new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setCountyName(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
