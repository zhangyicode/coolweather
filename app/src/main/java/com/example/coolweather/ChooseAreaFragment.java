package com.example.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/10/19 0019.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;//ListView适配器

    private List<String> dataList=new ArrayList<>();//放入ListView的数据集合

    private List<Province> provinceList;//省列表

    private List<City> cityList;//市列表

    private List<County> countyList;//县列表

    private Province selectedProvince;//选中的省份

    private City selectedCity;//选中的城市

    private int currentLevel;//当前选中的级别


    /*
    给碎片动态加载布局，并将前台控件与后台关联
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText=(TextView)view.findViewById(R.id.title_text);
        backButton=(Button)view.findViewById(R.id.back_button);
        listView=(ListView)view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);//实例adapter适配器
        listView.setAdapter(adapter);//关联适配器
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//给ListView的子项注册点击事件
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){//如果当前显示的是省份进行下列操作
                    selectedProvince=provinceList.get(position);//将点击事件获取到的省份设置为选择的省份
                    queryCities();//显示当前省份下的所有城市
                }else if(currentLevel==LEVEL_CITY){//如果当前显示的是市进行下列操作
                    selectedCity=cityList.get(position);//将点击事件获取到的城市设置为选择的城市
                    queryCounties();//显示当前城市下的所有县
                }else if(currentLevel==LEVEL_COUNTY){//如果当前显示的是县进行下列操作
                    String weatherId=countyList.get(position).getWeatherId();//获取天气ID
                    Intent intent=new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);//将天气ID封装到Intent中
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_COUNTY){//根据当前级别判断进行返回操作
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();//在创建的时候就显示省份的ListView
    }


    /*
    查询全国所有的省，优先从数据库中去查找，如果没有查到再去服务器上去查询
     */
    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);//将返回按钮隐藏，因为省份不能再返回上层菜单
        provinceList= DataSupport.findAll(Province.class);//从省份数据库中查找所有数据，并赋值给省份集合
        if (provinceList.size()>0){//如果省份集合不为空，就遍历这个集合
            dataList.clear();
            for(Province province : provinceList){
                dataList.add(province.getProvinceName());//添加将要显示在ListView中的数据
            }
            adapter.notifyDataSetChanged();//通知适配器数据发生变化，进行更新
            listView.setSelection(0);//将ListView显示到第一条
            currentLevel = LEVEL_PROVINCE;//将现在的级别设置到省份级别
        }else{//数据库中查询不在数据，请求网络数据
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }


    /*
    查询选中的省份所有的市，优先从数据库中去查找，如果没有查到再去服务器上去查询
     */
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());//将Title显示成选择的省份的名字
        backButton.setVisibility(View.VISIBLE);//将返回按钮可见
        cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
                //在城市数据库中查找选择省份id的所有城市并赋值给城市集合
        if(cityList.size()>0){//遍历集合
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;//将当前级别设置为城市级别
        }else{
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;//根据当前的省份去访问服务器数据
            queryFromServer(address,"city");
        }
    }


    /*
    查询选中的市所有的县，优先从数据库中去查找，如果没有查到再去服务器上去查询
     */
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());//将Title显示为选择的城市的名字
        backButton.setVisibility(View.VISIBLE);//将返回按钮可见
        countyList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);
                //在县数据库中查找所有附属于当前城市的县，并赋值给县集合
        if (countyList.size()>0){//遍历县集合
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;//将当前级别设置到县级别
        }else{
            int provinceCode=selectedProvince.getProvinceCode();//选择省份id
            int cityCode=selectedCity.getCityCode();//选择城市id
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;//根据省份以及城市访问服务器数据
            queryFromServer(address,"county");
        }
    }


    /*
    根据传入地址和类型去服务器上获取省市县的数据
     */
    private void queryFromServer(final String address, final String type) {//访问网络数据的方法（1地址2访问类型{根据访问类型解析数据}）
        showProgressDialog();//显示进度条
        HttpUtil.sendOkHttpRequest(address, new Callback() {//调用访问网络工具类
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();//服务器返回的数据
                boolean result=false;
                if ("province".equals(type)){//根据访问类型进行数据解析
                   result= Utility.handleProvinceResponse(responseText);//调用解析省份数据的方法
                }else if ("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result){//解析成功会返回一个为true的boolean类型的返回值
                    getActivity().runOnUiThread(new Runnable() {//切换到主线程
                        @Override
                        public void run() {
                            closeProgressDialog();//关闭进度条
                            if ("province".equals(type)){//根据访问类型来显示什么类型的ListView
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                /*回调方法都是在子线程中运行的，通过getActivity().runOnUiThread()方法回到主线程处理UI逻辑*/
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {//显示进度条
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);//设置进度条不能返回或点击其他地方消失
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {//关闭进度条
        if(progressDialog!=null){
           progressDialog.dismiss();
        }
    }
}
