package com.example.a63509.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public LocationClient mLocationClient = null;
    private MyLocationListener myLocationListener = new MyLocationListener();
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;
    private TextView positionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());  //声明LocationClient类，获取全进程有效的Context
        mLocationClient.registerLocationListener(myLocationListener); //注册一个定位监听器，当获取到位置信息时，会回调
        SDKInitializer.initialize(getApplicationContext()); //初始化
        setContentView(R.layout.activity_main);
        mapView = (MapView)findViewById(R.id.mmap); //获取MapView的实例
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);    //开启定位图层
        baiduMap.setIndoorEnable(true);     //获取室内地图
        positionText = (TextView)findViewById(R.id.position_text_view);

        List<String>permissionList = new ArrayList<>(); //创建一个空List集合，依次判断后面三个权限有没有被授权，如果没有，则加入List集合中
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);   //通过GPS芯片接收卫星的定位信息，定位精度达10米以内
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);   //访问电话状态
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE); //允许程序写入外部存储，如SD卡上写文件
        }

        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);   //将List集合转化为数组
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1); //一次性申请LIst集合中的权限

        }else{
            requestLocation();  //地理位置定位
        }

    }

    /**
     * 对MapView进行管理，保证资源能够及时释放
     */
    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mLocationClient.stop(); //活动被销毁时，停止定位
        mapView.onDestroy();
        //当不需要定位图层是关闭定位图层
        baiduMap.setMyLocationEnabled(false);
    }

    private void requestLocation(){
        initLocation();
        mLocationClient.start();    //发起定位请求
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);   //高精度模式表示允许使用GPS、无线网络、蓝牙或移动网络来进行定位
        //option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);    //节电模式，只会使用网络进行定位
        //option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);   //仅使用设备
        option.setCoorType("bd09ll");   //正确显示当前位置必须使用bd09ll，默认为gcj02
        option.setScanSpan(1000);   //1秒更新当前位置
        option.setOpenGps(true);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);   //将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result : grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"同意所有权限才能正常使用本程序",Toast.LENGTH_SHORT).show();
                            finish();   //如果有权限被拒绝，就关闭程序
                            return;
                        }
                    }
                    requestLocation();  //重新请求定位
                }else{
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:

        }
    }

    private void navigateTo(BDLocation location){

        //开始的时候定位自己
        if(isFirstLocate){
            //将BDLocation对象中的地理位置信息取出并封装到LatLng对象中
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            //以动画的方式移动到地图中间
            baiduMap.animateMapStatus(update);
            //设置缩放级别
            update = MapStatusUpdateFactory.zoomTo(18f);    //地图缩放等级调整为4-21
            //将对象传入该方法中可完成缩放功能
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        //构造定位数据
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        //设置定位数据
        baiduMap.setMyLocationData(locationData);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location){
            if(location.getLocType()==BDLocation.TypeGpsLocation||location.getLocType()==BDLocation.TypeNetWorkLocation){
                if(location.getLocType()==BDLocation.TypeGpsLocation){
                    positionText.setText("正在使用GPS进行定位");
                    //传入自己的位置
                    navigateTo(location);
                }
                else if(location.getLocType()==BDLocation.TypeNetWorkLocation){
                    positionText.setText("正在使用网络进行定位");
                    /*
                    MyLocationConfiguration myLocationConfiguration = null;
                    myLocationConfiguration.accuracyCircleFillColor = 0xFFFFCC;
                    baiduMap.setMyLocationConfiguration(myLocationConfiguration);
                    */
                    navigateTo(location);
                }
            }
            else{
                positionText.setText("定位失败，请检查网络问题");
            }
        }

    }

}