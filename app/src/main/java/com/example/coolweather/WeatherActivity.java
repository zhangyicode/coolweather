package com.example.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCity,titleUpdateTime,degreeText,weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText,pm25Text,comfortText,carwashText,sportText;

    private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        //初始化各种控件
        bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);
        weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city);
        titleUpdateTime=(TextView)findViewById(R.id.title_update_time);
        degreeText=(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        forecastLayout=(LinearLayout)findViewById(R.id.forecast_layout);
        aqiText=(TextView)findViewById(R.id.aqi_text);
        pm25Text=(TextView)findViewById(R.id.pm25_text);
        comfortText=(TextView)findViewById(R.id.comfort_text);
        carwashText=(TextView)findViewById(R.id.car_wash_text);
        sportText=(TextView)findViewById(R.id.sport_text);
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);//读取缓存文件
        String weatherString=prefs.getString("weather",null);
        if (weatherString!=null){//缓存文件不为空，将缓存文件的内容解析之后显示
            Weather weather=Utility.handlerWeatherResponse(weatherString);
            showWeatherInfo(weather);//显示
        }else{//缓存文件为空，访问服务器获取数据
            String wertherId=getIntent().getStringExtra("weather_id");//从intent中获取天气ID
            weatherLayout.setVisibility(View.INVISIBLE);//将布局隐藏，为了美观
            requestWeather(wertherId);//访问服务器
        }
        String bingPic=prefs.getString("bing_pic",null);
        if (bingPic!=null){
            Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }
    }

    /*
    通过Glide加载必应每日一图
     */
    private void loadBingPic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";
        //获取图片链接，将连接存储到Preferences文件中
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(WeatherActivity.this, "背景图加载失败...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);//通过Glide加载网络图片
                    }
                });
            }
        });
    }


    /*
    根据天气ID请求城市天气信息
     */
    public void requestWeather(String weatherId){
       //    String weatherUrl=   "http://guolin.tech/api/weather?cityid=CN101190401&key=2981f295a42f43ea9f0c9d5ab6f657fa";
        String weatherUrl="http://guolin.tech/api/weather?cityid="+ weatherId +"&key=2981f295a42f43ea9f0c9d5ab6f657fa";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {//根据天气ID访问服务器数据
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                Log.d("zhangyi",responseText);
                final Weather weather= Utility.handlerWeatherResponse(responseText);//将服务器数据通过GSON类实例成对象
                runOnUiThread(new Runnable() {//切换到主线程
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor= PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();//获取Editor对象
                            editor.putString("weather",responseText);//将服务器返回的数据以键值对的方式存储
                            editor.apply();
                            showWeatherInfo(weather);//显示界面
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_LONG).show();
                    }
                });
            }

        });
        loadBingPic();//请求天气也会刷新图片
    }

    /*
    处理并展示Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather){
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();//移除已有的控件，动态加载控件时要先调用该方法。
        for(Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);//动态加载布局文件
            TextView dateText=(TextView)view.findViewById(R.id.date_text);
            TextView infoText=(TextView)view.findViewById(R.id.info_text);
            TextView maxText=(TextView)view.findViewById(R.id.max_text);
            TextView minText=(TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);//
        }
        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度"+weather.suggestion.comfort.info;
        String carWash="洗车指数"+weather.suggestion.carWash.info;
        String sport="运动建议"+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carwashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }
}
