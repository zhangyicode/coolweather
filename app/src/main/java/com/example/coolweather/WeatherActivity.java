package com.example.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.service.AutoUpdateService;
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

    private ImageView bingPicImg,now_image;

    public SwipeRefreshLayout swipeRefresh;//刷新控件

    private String mWeatherId;

    public DrawerLayout drawerLayout;

    private Button navButton;

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

        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        navButton=(Button)findViewById(R.id.nav_button);

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);//读取缓存文件
        String weatherString=prefs.getString("weather",null);
        if (weatherString!=null){//缓存文件不为空，将缓存文件的内容解析之后显示
            Weather weather=Utility.handlerWeatherResponse(weatherString);
            mWeatherId=weather.basic.weatherId;
            showWeatherInfo(weather);//显示
        }else{//缓存文件为空，访问服务器获取数据
            mWeatherId=getIntent().getStringExtra("weather_id");//从intent中获取天气ID
            weatherLayout.setVisibility(View.INVISIBLE);//将布局隐藏，为了美观
            requestWeather(mWeatherId);//访问服务器
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            //下拉刷新事件监听
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
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
    public void requestWeather(final String weatherId){
        String weatherUrl="http://guolin.tech/api/weather?cityid="+ weatherId +"&key=2981f295a42f43ea9f0c9d5ab6f657fa";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {//根据天气ID访问服务器数据
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather= Utility.handlerWeatherResponse(responseText);//将服务器数据通过GSON类实例成对象
                runOnUiThread(new Runnable() {//切换到主线程
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor= PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();//获取Editor对象
                            editor.putString("weather",responseText);//将服务器返回的数据以键值对的方式存储
                            editor.apply();
                            mWeatherId=weather.basic.weatherId;//将最后一期请求的天气ID设置为当前ID，就能解决修改城市之后的刷新问题了
                            showWeatherInfo(weather);//显示界面
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_LONG).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_LONG).show();
                        swipeRefresh.setRefreshing(false);
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
        titleUpdateTime.setText("Updated："+updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        now_image=(ImageView)findViewById(R.id.now_image);
        showImage(now_image,weatherInfo);
        forecastLayout.removeAllViews();//移除已有的控件，动态加载控件时要先调用该方法。
        for(Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);//动态加载布局文件
            TextView dateText=(TextView)view.findViewById(R.id.date_text);
            ImageView icon=(ImageView)view.findViewById(R.id.forecast_icon);
            //TextView infoText=(TextView)view.findViewById(R.id.info_text);//天气详情
            TextView maxText=(TextView)view.findViewById(R.id.max_text);
            TextView minText=(TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            showImage(icon,forecast.more.info);
            //infoText.setText(forecast.more.info);
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
        //开启后台刷新服务
        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void showImage(ImageView imageView,String weatherInfo){//根据天气详情显示相应图标
        switch(weatherInfo){
            case "多云":
                imageView.setImageResource(R.drawable.duoyun);
                break;
            case "阴":
                imageView.setImageResource(R.drawable.yin);
                break;
            case "晴":
                imageView.setImageResource(R.drawable.qing);
                break;
            case "大雨":
                imageView.setImageResource(R.drawable.dayu);
                break;
            case "小雨":
                imageView.setImageResource(R.drawable.xiaoyu);
                break;
            case "中雨":
                imageView.setImageResource(R.drawable.zhongyu);
                break;
            case "阵雨":
                imageView.setImageResource(R.drawable.zhenyu);
                break;
            case "晴间多云":
                imageView.setImageResource(R.drawable.qingjianduoyun);
                break;
            default:
                break;
        }
    }
}
