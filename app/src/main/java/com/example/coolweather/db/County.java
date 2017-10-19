package com.example.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/10/19 0019.
 */

public class County extends DataSupport {
    private int id;
    private String countyName;//县名字
    private String weatherId;//县所对应的天气id，获取服务器数据用
    private int cityId;//所属的城市id

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }


    public int getId() {
        return id;
    }

    public String getCountyName() {
        return countyName;
    }

}
