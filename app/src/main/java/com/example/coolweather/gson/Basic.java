package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/10/21 0021.
 */

public class Basic {
    @SerializedName("city")//@SerializedName注解的方式将JSON字段和Java字段建立映射关系
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
