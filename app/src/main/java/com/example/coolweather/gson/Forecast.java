package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/10/21 0021.
 */

public class Forecast {//JSON中包含的是一个数组，只需要定义出一个实体类，在声明实体类的时候利用集合类型来进行声明

    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;


    public class Temperature{
        public String max;
        public String min;
    }

    public class More{
        @SerializedName("txt_d")
        public String info;
    }

}
