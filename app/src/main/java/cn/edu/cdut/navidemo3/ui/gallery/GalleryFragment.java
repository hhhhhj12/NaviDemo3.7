package cn.edu.cdut.navidemo3.ui.gallery;

import static android.util.Half.EPSILON;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.StrictMath.sqrt;
import static cn.edu.cdut.navidemo3.ui.home.HomeFragment.FILE_FOLDER;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.icu.text.SimpleDateFormat;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.map.track.TraceAnimationListener;
import com.baidu.mapapi.map.track.TraceOptions;
import com.baidu.mapapi.model.LatLng;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.edu.cdut.navidemo3.R;
import cn.edu.cdut.navidemo3.databinding.FragmentGalleryBinding;
import cn.edu.cdut.navidemo3.extra.data.ESContinuousActivity;
import cn.edu.cdut.navidemo3.extra.data.ESTimestamp;
import cn.edu.cdut.navidemo3.ui.home.HomeFragment;
import cn.edu.cdut.navidemo3.ui.stress.PressureScrollingFragment;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    TextView textView_filepath;
    Button btn_showTrace;
    LocationClient mLocationClient ;
    MapView mMapView;
    BaiduMap mBaiduMap = null;
    boolean isFirstLocate = true;
    UiSettings mUiSettings;
    List<LatLng> ppp;
    //List<Overlay> overlays;
    public static double[] locationLatLong = new double[2];
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        btn_showTrace = binding.btnShowTrace;
        View root = binding.getRoot();

        final TextView textView = binding.textGallery;
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        LocationClient.setAgreePrivacy(true);//此问题是缺这个权限


        try {
            //Log.d("TEST","1:"+ String.valueOf(mLocationClient));
            mLocationClient = new LocationClient(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mLocationClient.registerLocationListener(new MyLocationListener());

        //获取地图控件引用
        mMapView = getActivity().findViewById(R.id.bmapView_G);
        mBaiduMap = mMapView.getMap();
        //实例化UiSettings类对象,对地图进行设置
        mUiSettings = mBaiduMap.getUiSettings();
        //设置地图模式为卫星地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //开启地图的定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //关闭地图缩放控件
        mMapView.showZoomControls(false);
        //开启交通图
        //mBaiduMap.setTrafficEnabled(true);

        //支持设置maxZoomLevel和minZoomLevel
        mBaiduMap.setMaxAndMinZoomLevel(20f, 16f);


/*        addText(22.802828, 113.95267, "饭堂");
        addText(22.803573, 113.95205, "西一");
        addText(22.804265, 113.9513, "西三");
        addText(22.804943, 113.95774, "西园教学楼");
        addText(22.809174, 113.963319, "东园教学楼");
        addText(22.803169, 113.960031, "理学院");
        addText(22.80506, 113.952396, "西园操场");
        addText(22.811003, 113.967563, "东园操场");*/


        List<String> permissionList = new ArrayList<String>();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
                Environment.isExternalStorageManager()) {
            Toast.makeText(getContext(), "已获得访问所有文件的权限", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            Toast.makeText(getContext(), "请允许本软件获得访问所有文件的权限", Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }


        if (!permissionList.isEmpty()) {

            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(getActivity(), permissions, 1);
            //Log.i("FILEE", String.valueOf(permissions));
        } else {
            requestLocation();
        }

        btn_showTrace.setOnClickListener(new View.OnClickListener() {
            int isShow = 1;
            @Override
            public void onClick(View view) {
                if (isShow==1) {
                    List<LatLng> points = new ArrayList<LatLng>();
                    int ii = 1;//测试用的

                    for (ESContinuousActivity today_activity : PressureScrollingFragment.get_activityArray()){
                        if (today_activity.getLocationLatLongFromFirstInstance().length==0){
                            continue;
                        }

                        double _latitude = today_activity.getLocationLatLongFromFirstInstance()[0];
                        double _longitude = today_activity.getLocationLatLongFromFirstInstance()[1];
                        ESTimestamp _startTimestamp = today_activity.getStartTimestamp();
                        ESTimestamp _endTimestamp = today_activity.getStartTimestamp();

                        //setting time label
                        Date date = _startTimestamp.getDateOfTimestamp();
                        String start_timeLabel = new java.text.SimpleDateFormat("kk:mm").format(date);
                        date = _endTimestamp.getDateOfTimestamp();
                        String end_timeLabel = new java.text.SimpleDateFormat("kk:mm").format(date);

                        Toast.makeText(getContext(), ""+ ii++ +" "+ _startTimestamp +" "+_longitude, Toast.LENGTH_SHORT).show();
                        if (_latitude > 0){
                            //showLocTag(_latitude,  _longitude,
                            showLocTag(_latitude, _longitude,
                                ""+ start_timeLabel+"~" + end_timeLabel + "\n"
                                    + today_activity.getMainActivityUserCorrection());
                            points.add(new LatLng(_latitude,_longitude));
                        }
                    }
                    //showLocTag(22.803747, 113.951418, "宿舍\n0:00~7:26\n18:00~24:00");
                    //showLocTag(22.804943, 113.95774, "西园教学楼\n7:54~11:50");
                    //showLocTag(22.802828, 113.95267, "饭堂\n11:54~12:10");
                    //showLocTag(22.801639, 113.955214, "实验室\n12:30~17:50");

                    //构建折线点坐标
                    //LatLng p1_ss = new LatLng(22.803747, 113.951418);
                    //LatLng p2_jxl = new LatLng(22.804943, 113.95774);
                    //LatLng p3_ft = new LatLng(22.802828, 113.95267);
                    //LatLng p4_sys = new LatLng(22.801639,113.955214);


                    //points.add(p1_ss);points.add(new LatLng(22.804613,113.953201));points.add(new LatLng(22.805146,113.95471));
                    //points.add(p2_jxl);points.add(new LatLng(22.804238,113.956094));points.add(new LatLng(22.804988,113.954746));
                    //points.add(p3_ft);
                    //points.add(p4_sys);points.add(new LatLng(22.801344,113.954737));points.add(new LatLng(22.803201,113.95167));points.add(new LatLng(22.803572,113.951629));


                    //添加纹理图片
                    List<BitmapDescriptor> textureList = new ArrayList<BitmapDescriptor>();
                    BitmapDescriptor mRedTexture = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_road_red_arrow);//箭头图片
                    textureList.add(mRedTexture);

                    // 添加纹理图片对应的顺序
                    List<Integer> textureIndexs = new ArrayList<Integer>();
                    for (int i=0;i<points.size();i++){
                        textureIndexs.add(i);//或许把0变为i？
                    }
                    if(points.size()>=2){
                        OverlayOptions mOverlay=new PolylineOptions()
                                //折线宽度
                                .width(16)
                                //折线颜色
                                .color(0xAAFF0000)
                                //折线坐标点列表
                                .points(points)
                                .dottedLine(true)
                                .textureIndex(textureIndexs)//设置分段纹理index数组
                                .customTextureList(textureList);//设置线段的纹理，建议纹理资源长宽均为2的n次方;
                        Overlay mPolyline=(Polyline)mBaiduMap.addOverlay(mOverlay);
                        //mPolyline.setZIndex(3);
                    }
/*
                    //设置折线的属性
                    OverlayOptions mOverlayOptions = new PolylineOptions()
                            .width(10)
                            .color(0xAAFF0000)
                            .points(points);
                    //在地图上绘制折线
                    //mPloyline 折线对象
                    Overlay mPolyline = mBaiduMap.addOverlay(mOverlayOptions);*/


                    isShow = 0;
                }else {
                    mBaiduMap.hideInfoWindow();
                    mBaiduMap.clear();
                    isShow = 1;
                }


            }
        });

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result :grantResults){
                        if (result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(getContext(),"必须同意所有的权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            //finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(getContext(),"发生未知错误",Toast.LENGTH_SHORT).show();
                    //finish();

                }
                break;
        }
    }

    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        ppp=new ArrayList<LatLng>();
        //ppp.add(new LatLng(22.802401, 113.952876 ));
        //ppp.add(new LatLng(22.802401, 113.952876 ));
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选、设置定位模式,默认高精度
        //LocationNode.Hight_Accuracy:高精度;
        //LocationMode. Battery_Saving;低功耗;
        //LocationMode. Device_Sensors :仅使用设备;

        option.setCoorType("bd09ll");
        //可选，设置返回经纬度坐标类型，默认GCJ02
        //GCJ02:国测局坐标;
        //BD09ll :百度经纬度坐标;
        //BD09:百度墨卡托坐标;
        //海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标

        option.setScanSpan(1000);
        //可选，设置发起定位请求的间隔, int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为日
        //如果设置非o，需设置100ems 以上才有效

        option.setOpenGps(true);
        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true

        option. setLocationNotify(true );
        //可选，设置是否当GPS有效时按照1s/1次频率输出GPS结果，默认false

        option.setIgnoreKillProcess (false);
        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop 的时候杀死这个进程,默认(建议）不杀死，即setIgnoreKillLProcess(true)

        option.SetIgnoreCacheException(false) ;
        //可选，设置是否收集crash 信息，默认收集，即参数为false

        option.setWifiCacheTimeOut(5*60*1000);
        //可选，V7.2版本新增能力
        //如果设置了该接口，首次启动定位时,会先判断当前wi-Fi是否超出有效期，若超出有效期，会先重新扫描wi-Fi，然后定位

        option.setEnableSimulateGps(false);
        //可选，设置是否需要过滤GPs仿真结果，默认需要，即参数为false
        option.setIsNeedAddress(true);

        mLocationClient.setLocOption(option);



    }


    private class MyLocationListener extends BDAbstractLocationListener {

        @SuppressLint("SetTextI18n")
        @Override
        public void onReceiveLocation(BDLocation location) {

            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null){
                return;
            }
            navigateTo(location);

            printTrace(location);

            inDoorMode(location);
            //数据开始记录时

            //添加纬度
            if (location.getLatitude() > 0.1) {
                locationLatLong[0]=location.getLatitude();
            }
            //添加经度
            if (location.getLongitude() > 0.1) {
                locationLatLong[1]=location.getLongitude();
                //Log.i("ASDFGGG",""+ locationLatLong[0] + " ," + locationLatLong[1])
            }


        }
    }


    private void navigateTo(BDLocation location){
        if (isFirstLocate) {
            //拿到经纬度信息
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            //地图手势旋转等操作是以地图中心点为标准做旋转的，通过如下方法设置地图中心点。
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            //对地图状态做更新，否则可能不会触发渲染，造成样式定义无法立即生效。
            mBaiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            mBaiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        //生成小蓝点
        MyLocationData.Builder locatoinBuilder = new MyLocationData.Builder();
        locatoinBuilder.longitude(location.getLongitude());
        locatoinBuilder.latitude(location.getLatitude());
        MyLocationData myLocationData = locatoinBuilder.build();
        mBaiduMap.setMyLocationData(myLocationData);


    }

    private void printTrace(BDLocation location){
        LatLng pointsss=new LatLng(location.getLatitude(),location.getLongitude());

        if(pointsss.longitude>0.1 && pointsss.latitude>0.1 )
            ppp.add(pointsss);
        if (ppp.size()>=2) {//防止traceOptions.points(ppp);报错
            TraceOptions traceOptions = new TraceOptions();
            traceOptions.animationTime(5000);
            traceOptions.animate(true);
            traceOptions.animationType(TraceOptions.TraceAnimateType.TraceOverlayAnimationEasingCurveLinear);
            traceOptions.color(0xAAFF0000);
            traceOptions.width(10);
            traceOptions.points(ppp);
            //traceOptions.icon(mRedTexture);
            mBaiduMap.addTraceOverlay(traceOptions, new TraceAnimationListener() {
                @Override
                public void onTraceAnimationUpdate(int percent) {
                    // 轨迹动画更新进度回调
                }

                @Override
                public void onTraceUpdatePosition(LatLng position) {
                    // 轨迹动画更新的当前位置点回调
                }

                @Override
                public void onTraceAnimationFinish() {
                    // 轨迹动画结束回调
                }
            });
        }



        //在日志中打印出点的位置信息，方便观察
        //Log.i("MyMap",  " latitude:" + location.getLatitude()
        //        + " longitude:" + location.getLongitude() + ppp.size());

    }

    private void inDoorMode(BDLocation location){
        //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
        if (location.getFloor() != null) {
            // 开启室内定位模式（重复调用也没问题），开启后，定位SDK会融合各种定位信息（Gnss,WI-FI，蓝牙，传感器等）连续平滑的输出定位结果；
            mLocationClient.startIndoorMode();
        }
    }

    protected void addText(double latitude,double longitude,String text){
        //文字覆盖物位置坐标
        LatLng llText = new LatLng(latitude, longitude);

        //构建TextOptions对象
        OverlayOptions mTextOptions = new TextOptions()
                .text(text) //文字内容
                .bgColor(0xAA92D050) //背景色
                .fontSize(24) //字号
                .fontColor(0xFF085820) //文字颜色
                .rotate(-0) //旋转角度
                .position(llText);

        //在地图上显示文字覆盖物
        Overlay mText = mBaiduMap.addOverlay(mTextOptions);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.stopIndoorMode();
        mLocationClient.stop();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);
        //Toast.makeText(MainActivity2.this,"已取消注册传感器监听器",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        //simpleRate.get_SENSOR_RATE_SLOW() //SensorManager.SENSOR_DELAY_NORMAL
        mMapView.onResume();
        //重新定位到学校
        mBaiduMap.setMyLocationData(new MyLocationData.Builder().longitude(113.952876).latitude(22.802401).build());
        LatLng ll = new LatLng(22.802401, 113.952876);
        //地图手势旋转等操作是以地图中心点为标准做旋转的，通过如下方法设置地图中心点。
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
        //对地图状态做更新，否则可能不会触发渲染，造成样式定义无法立即生效。
        mBaiduMap.animateMapStatus(update);
    }

    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    // 把毫秒转为hh小时mm分钟ss秒的格式
    public String getTimeFromInt(long time){
        // time为毫秒，转为秒
        time /= 1000;

        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        hours = time / 3600 %24 +8;
        minutes = (time%3600) / 60;
        seconds = time % 60;

        String result = "";

        if(hours != 0){
            result += (String.valueOf(hours)+":");
        }
        if(hours != 0 || minutes != 0){
            result += (String.valueOf(minutes)+":");
        }
        result += (String.valueOf(seconds)+" ");
        return result;
    }

    public void showLocTag(double latitude,double longitude,CharSequence text){
        //定义Maker坐标点  113.952141,22.803726
        LatLng point = new LatLng(latitude, longitude);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.place);

        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
        //用来构造InfoWindow的Button
        Button button = new Button(getContext());
        button.setBackgroundResource(R.drawable.dialog);
        button.setText(text);
        button.setTextSize(8);
        //构造InfoWindow
        //point 描述的位置点
        //-10InfoWindow相对于point在y轴的偏移量
        InfoWindow mInfoWindow = new InfoWindow(button, point, -12);
        //使InfoWindow生效
        mBaiduMap.showInfoWindow(mInfoWindow,false);
    }

    public static String getFileCreateTime(String filePath) {

        SimpleDateFormat dateFormat = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss ");
        }
        FileTime t = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                t = Files.readAttributes(Paths.get(filePath), BasicFileAttributes.class).creationTime();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String createTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createTime = dateFormat.format(t.toMillis());
        }
        System.out.println("创建时间 ： " + createTime);
        return createTime;
    }

    public static double[] getCurrentPositionDoubleArray() {
        return locationLatLong;
    }
}