package cn.edu.cdut.navidemo3.ui.home;

import static android.content.Context.SENSOR_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import cn.edu.cdut.navidemo3.MyService;
import cn.edu.cdut.navidemo3.R;
import cn.edu.cdut.navidemo3.databinding.FragmentHomeBinding;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.edu.cdut.navidemo3.WriteData2CSVThread;
import cn.edu.cdut.navidemo3.record.AudioRecorder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment implements SensorEventListener{

    private FragmentHomeBinding binding;
    SharedPreferences label;
    SharedPreferences.Editor edit;
    private String url="http://192.168.32.188:5000/upload";
    OkHttpClient okHttpClient;
    LottieAnimationView animationView;
    TextView locationInfo;
    TextView accInfo;
    TextView screenInfo;
    TextView gyroscopeInfo;
    TextView gyroscope2Info;
    RadioButton rb1,rb2,rb3,rb4,rb5,rb6,rb7,rb8,rb9;
    public AudioRecorder audioRecorder;
    Button btn_startRecorder;
    Button btn_savedata;
    List<RadioButton> rbs;
    ImageView img_phone3;
    ImageView img_phone_roll;
    ImageView Img_loc;

    LocationClient mLocationClient ;
    MapView mMapView;
    BaiduMap mBaiduMap = null;

    CheckBox CB_isdeveloper;
    RadioGroup RBG_act,RBG_sound;
    RadioGroup RBG_loc;
    Button btn_savesettings;

    boolean isFirstLocate = true;

    UiSettings mUiSettings;
    List<LatLng> ppp;
    List<UsageStats> stats;
    StringBuilder currentPosition = new StringBuilder();
    StringBuilder currentAcc = new StringBuilder();
    StringBuilder currentSrc = new StringBuilder();
    StringBuilder currentSrc_HHMMSS = new StringBuilder();
    StringBuilder currentAngularspeed = new StringBuilder();
    StringBuilder currentgyroscope = new StringBuilder();
    public static String[] locationLatLong = new String[2];

    public static final String FILE_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
            + "AboutView" + File.separator + "data";

    private static final String FILE_POSITION_CSV = "position_data.csv";
    private static final String FILE_ACC_CSV = "acc_data.csv";
    private static final String FILE_SCR_CSV = "scr_data.csv";

    public static final String FOLDER_POSITION = File.separator + "position_data";
    private static final String FOLDER_ACC = File.separator + "acc_data";
    private static final String FOLDER_SCR = File.separator + "scr_data";
    private static final String FOLDER_AS = File.separator + "AS_data";
    private static final String FOLDER_G = File.separator + "gyroscope_data";
    public static final String FOLDER_DAYACTIVITYDATA = File.separator + "theDayActivityData_data";

    SensorManager sensorManager;
    WindowManager windowManager;
    Display display;
    Sensor sensor_acc;
    Sensor sensor_gyroscope;
    Sensor sensor_o;
    DecimalFormat df = new DecimalFormat("0.0000");
    //SimpleDateFormat sdFormatter = new SimpleDateFormat("HH:mm:ss");
    private final int Time =40;    //时间间隔，   单位 ms
    private Handler handler = new Handler();

    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    CharSequence wifi_BSSID;

    //记录加速度数据
    double ax = 0;
    double ay = 0;
    double az = 0;

    //记录陀螺仪数据
    double gx = 0;
    double gy = 0;
    double gz = 0;

    float angleX =0;
    float angleY=0;
    float angleZ=0;


    private float mTimestamp; // 记录上次的时间戳
    private float mAngle[] = new float[3]; // 记录xyz三个方向上的旋转角度
    private float deltaRotationVector[] = new float[4];
    private static final float NS2S = 1.0f / 1000000000.0f; // 将纳秒转化为秒

    //private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;
    private float angle[] =new  float[3];


    //1是屏幕熄灭状态
    private int screenState = 1 ;
    private int isStartRecord = 0;
    SimpleDateFormat formatter= new SimpleDateFormat("MM_dd_HH_mm");

//    ActivityMainBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        sensorManager = (SensorManager) getActivity().getApplicationContext().getSystemService(SENSOR_SERVICE);

        sensor_acc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensor_gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensor_o = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        //SensorManager.SENSOR_DELAY_NORMAL
        sensorManager.registerListener(this, sensor_acc, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensor_gyroscope, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this,sensor_o,SensorManager.SENSOR_DELAY_GAME);

        windowManager = (WindowManager) getActivity().getApplicationContext().getSystemService(WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();




        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        okHttpClient = new OkHttpClient.Builder().build();
        locationInfo = getActivity().findViewById(R.id.locationInfo);
        accInfo = getActivity().findViewById(R.id.accInfo);
        screenInfo = getActivity().findViewById(R.id.screenInfo);
        gyroscopeInfo = getActivity().findViewById(R.id.gyroscopeInfo);
        gyroscope2Info = getActivity().findViewById(R.id.gyroscope2Info);

        btn_startRecorder = getActivity().findViewById(R.id.btn_startRecorder);
        btn_savedata = getActivity().findViewById(R.id.btn_savedata);

        img_phone3 = getActivity().findViewById(R.id.img_phone3);
        img_phone_roll = getActivity().findViewById(R.id.img_phone_roll);
        Img_loc = getActivity().findViewById(R.id.Img_loc);
        animationView = getActivity().findViewById(R.id.voice_wave);
        CB_isdeveloper = getActivity().findViewById(R.id.CB_isdeveloper);
        RBG_act = getActivity().findViewById(R.id.RBG_act);
        RBG_loc = getActivity().findViewById(R.id.RBG_loc);
        RBG_sound = getActivity().findViewById(R.id.RBG_sound);

        setRBG3();

        btn_savesettings = getActivity().findViewById(R.id.btn_savesettings);
        LocationClient.setAgreePrivacy(true);//此问题是缺这个权限

        btn_startRecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Date date = new Date(System.currentTimeMillis());
                //Log.i("TESTT",formatter.format(date));
                Intent intent = new Intent(getContext(), MyService.class);
                getActivity().startService(intent);

                if (isStartRecord == 0){
                    //开始记录
                    isStartRecord = 1;
                    //startAudioRecord();
                    Toast.makeText(getContext(),"已开始记录数据",Toast.LENGTH_SHORT).show();

                    //每开始采集传感器数据30秒后停止
                    long delayMillis = 30000;
                    Runnable runnable_stop = new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {

                            //每隔一段时间要重复执行的代码
                            if (isStartRecord==1){
                                handler.postDelayed(this, delayMillis);
                                isStartRecord = 0;
//                                finishRecordAndSaveData();

                                //audioRecorder.stopRecord(getContext());
                                animationView.cancelAnimation();
                                handler.removeCallbacks(this);
                            }

                        }
                    };
                    handler.postDelayed(runnable_stop,delayMillis);


                } else if (isStartRecord == 1){
                    //结束记录
                    isStartRecord = 0;
                    Toast.makeText(getContext(),"已结束记录数据",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_savedata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //finishRecordAndSaveData();
                Intent intent = new Intent(getContext(), MyService.class);
                getActivity().stopService(intent);

            }
        });

        img_phone3.setOnClickListener(new View.OnClickListener(){
                                          @Override
                                          public void onClick(View v){
                                              //获取ImageView的Bitmap
                                              Bitmap bitmap0 = ((BitmapDrawable)img_phone3.getDrawable()).getBitmap();
                                              bigImageLoader(bitmap0);
                                          }
                                      });
        img_phone_roll.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //获取ImageView的Bitmap
                Bitmap bitmap0 = ((BitmapDrawable)img_phone_roll.getDrawable()).getBitmap();
                bigImageLoader(bitmap0);
            }
        });

        Img_loc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //获取ImageView的Bitmap
                Bitmap bitmap0 = ((BitmapDrawable)Img_loc.getDrawable()).getBitmap();
                bigImageLoader(bitmap0);
            }
        });


        CB_isdeveloper.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()){
                    //Toast.makeText(getContext(),compoundButton.getText()+"被选中",Toast.LENGTH_SHORT).show();
                    RBG_act.setVisibility(View.VISIBLE);
                    RBG_loc.setVisibility(View.VISIBLE);
                    RBG_sound.setVisibility(View.VISIBLE);
                    btn_savesettings.setVisibility(View.VISIBLE);
                    //btn_startRecorder.setVisibility(View.INVISIBLE);
                }else {
                    RBG_act.setVisibility(View.GONE);
                    RBG_loc.setVisibility(View.GONE);
                    RBG_sound.setVisibility(View.GONE);
                    btn_savesettings.setVisibility(View.GONE);
                    //btn_startRecorder.setVisibility(View.VISIBLE);
                }
            }
        });


        try {
            //Log.d("TEST","1:"+ String.valueOf(mLocationClient));
            mLocationClient = new LocationClient(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Log.d("TEST","2:"+ String.valueOf(mLocationClient));
        mLocationClient.registerLocationListener(new MyLocationListener());

        //获取地图控件引用
        mMapView = getActivity().findViewById(R.id.bmapView);
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
        mBaiduMap.setMaxAndMinZoomLevel(20f,16f);



        List<String> permissionList = new ArrayList<String>();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE )!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        //startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        //startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            ActivityCompat.requestPermissions(MainActivity2.this,new String[]{Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION},1);
//        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
                Environment.isExternalStorageManager()) {
            //Toast.makeText(getContext(), "已获得访问所有文件的权限", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            Toast.makeText(getContext(), "请允许本软件获得访问所有文件的权限", Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }




        if (!permissionList.isEmpty()){

            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(getActivity(),permissions,1);
            //Log.i("FILEE", String.valueOf(permissions));
        }else {
            requestLocation();
        }




        //显示并保存加速度数据，40ms一次 (25Hz)
        Runnable runnable_acc = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                handler.postDelayed(this, Time);
                //每隔一段时间要重复执行的代码
                //开始记录数据时
                if (isStartRecord==1) {
                    currentAcc.append(System.currentTimeMillis()).append(",")
                            .append(df.format(ax)).append(",")
                            .append(df.format(ay)).append(",")
                            .append(df.format(az)).append("\n");

                    accInfo.setText("三轴加速度：\t\t\t\t    \n"+"accX: " + df.format(ax) + "\n"
                            + "accY: " + df.format(ay) + "\n"
                            + "accZ: " + df.format(az));

                }
            }
        };
        handler.postDelayed(runnable_acc, Time);	//启动计时器

        //显示并保存陀螺仪数据，40ms一次 (25Hz)
        Runnable runnable_gyroscope = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                handler.postDelayed(this, 40);
                //每隔一段时间要重复执行的代码
                //开始记录数据时
                if (isStartRecord==1) {
                    currentAngularspeed.append(System.currentTimeMillis()).append(",")
                            .append(df.format(gx)).append(",")
                            .append(df.format(gy)).append(",")
                            .append(df.format(gz)).append("\n");

                    currentgyroscope.append(System.currentTimeMillis()).append(",")
                            .append(df.format(angleX)).append(",")
                            .append(df.format(angleY)).append(",")
                            .append(df.format(angleZ)).append("\n");

                    gyroscopeInfo.setText("三轴角速度：\t\t\t\t    \n"+"axisX: " + df.format(gx )+ "\n"
                            + "axisY: " + df.format(gy) + "\n"
                            + "axisZ: " + df.format(gz));

                    gyroscope2Info.setText("陀螺仪检测到当前\nx轴的转动角度为\n"+df.format(angleX)
                            +"\ny轴的转动角度为\n"+df.format(angleY)
                            +"\nz轴的转动角度为\n"+df.format(angleZ));
                    //Log.i("TESTT",""+gx +"*" +angleX );
                }

            }
        };
        handler.postDelayed(runnable_gyroscope, Time);	//启动计时器

        //检测屏幕开关状态，250ms一次 (4Hz)
        Runnable runnable_scr = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                handler.postDelayed(this, 250);
                //每隔一段时间要重复执行的代码

                if (isStartRecord==1) {
                    if (display.getState() == 2 && screenState == 1) {
                        //屏幕是由熄灭到开启
                        currentSrc.append(System.currentTimeMillis()).append(",");
                        currentSrc_HHMMSS.append(getTimeFromInt(System.currentTimeMillis())).append(", ");
                    } else if (display.getState() == 1 && screenState == 2) {
                        //屏幕是由开启到熄灭
                        currentSrc.append(System.currentTimeMillis()).append("\n");
                        currentSrc_HHMMSS.append(getTimeFromInt(System.currentTimeMillis())).append("\n");
                    }
                    screenState = display.getState();

                }

                screenInfo.setText("亮屏时间(保存后重置)"+"\n" + "\t\t\ton\t\t\t\t\t\t\t\toff\n"+currentSrc_HHMMSS );
            }
        };
        handler.postDelayed(runnable_scr,250);


    }

    private void setRBG3() {

        rb1 = getActivity().findViewById(R.id.RB_static);
        rb2 = getActivity().findViewById(R.id.RB_walk);
        rb3 = getActivity().findViewById(R.id.RB_run);
        rb4 = getActivity().findViewById(R.id.RB_sushe);
        rb5 = getActivity().findViewById(R.id.RB_classroom);
        rb6 = getActivity().findViewById(R.id.RB_fantang);
        rb7 = getActivity().findViewById(R.id.RB_shiyanshi);
        rb8 = getActivity().findViewById(R.id.RB_anjing);
        rb9 = getActivity().findViewById(R.id.RB_caoza);

        rbs = new ArrayList<>();
        rbs.add(rb1);
        rbs.add(rb2);
        rbs.add(rb3);
        rbs.add(rb4);
        rbs.add(rb5);
        rbs.add(rb6);
        rbs.add(rb7);
        rbs.add(rb8);
        rbs.add(rb9);


        label = getActivity().getSharedPreferences("label", getContext().MODE_PRIVATE);

        edit = label.edit();
        edit.putInt("act",1);
        edit.putInt("loc",4);
        edit.putInt("sound",8);
        edit.apply();
        rb1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getContext(),"已选中静止",Toast.LENGTH_SHORT).show();
                    edit.putInt("act",1);
                    edit.apply();
                }
            }
        });

        rb2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getContext(),"已选中走路",Toast.LENGTH_SHORT).show();
                    edit.putInt("act",2);
                    edit.apply();
                }
            }
        });

        rb3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getContext(),"已选中跑步",Toast.LENGTH_SHORT).show();
                    edit.putInt("act",3);
                    edit.apply();
                }
            }
        });

        rb4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getContext(),"已选中宿舍",Toast.LENGTH_SHORT).show();
                    edit.putInt("loc",4);
                    edit.apply();
                }
            }
        });
        rb5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getContext(),"已选中教室",Toast.LENGTH_SHORT).show();
                    edit.putInt("loc",5);
                    edit.apply();
                }
            }
        });
        rb6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getContext(),"已选中饭堂",Toast.LENGTH_SHORT).show();
                    edit.putInt("loc",6);
                    edit.apply();
                }
            }
        });

        rb7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getContext(),"已选中实验室",Toast.LENGTH_SHORT).show();
                    edit.putInt("loc",7);
                    edit.apply();
                }
            }
        });
        rb8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getContext(),"已选中安静",Toast.LENGTH_SHORT).show();
                    edit.putInt("sound",8);
                    edit.apply();
                }
            }
        });

        rb9.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Toast.makeText(getContext(),"已选中嘈杂",Toast.LENGTH_SHORT).show();
                    edit.putInt("sound",9);
                    edit.apply();
                }
            }
        });
    }

    private void startAudioRecord() {
        audioRecorder = AudioRecorder.getInstance();
        if (isStartRecord==1){
            if (audioRecorder.getStatus()==AudioRecorder.Status.STATUS_NO_READY) {
                String fileName = "audio";
                audioRecorder.createDefaultAudio(fileName);
                audioRecorder.startRecord(null);
                //显示正在录音
                animationView.playAnimation();
            }
        }
    }

    private void finishRecordAndSaveData() {
        Date date = new Date(System.currentTimeMillis());
/*                WriteData2CSVThread myThread_saveposision = new WriteData2CSVThread(currentPosition,FILE_FOLDER + FOLDER_POSITION,"position_data_" +formatter.format(date)+".csv");
                myThread_saveposision.run();*/

/*                Date date = new Date(System.currentTimeMillis());
                WriteData2CSVThread myThread_saveposision = new WriteData2CSVThread(currentPosition,FILE_FOLDER + FOLDER_POSITION,"position_data_" +formatter.format(date)+".csv");
                myThread_saveposision.run();*/

        WriteData2CSVThread myThread_saveacc = new WriteData2CSVThread(currentAcc,FILE_FOLDER + FOLDER_ACC,"acc_data_" +formatter.format(date)+".csv");
        myThread_saveacc.run();

 /*               WriteData2CSVThread myThread_savesrc = new WriteData2CSVThread(currentSrc,FILE_FOLDER + FOLDER_SCR,"scr_data_" +formatter.format(date)+".csv");
                myThread_savesrc.run();*/

/*                WriteData2CSVThread myThread_saveAngularspeed = new WriteData2CSVThread(currentAngularspeed,FILE_FOLDER + FOLDER_AS,"Angularspeed_data_" +formatter.format(date)+".csv");
                myThread_saveAngularspeed.run();*/

        WriteData2CSVThread myThread_savegyroscope = new WriteData2CSVThread(currentgyroscope,FILE_FOLDER + FOLDER_G,"gyroscope_data_" +formatter.format(date)+".csv");
        myThread_savegyroscope.run();

/*                        .append(df.format(angleX)).append(",")
                        .append(df.format(angleY)).append(",")
                        .append(df.format(angleZ)).append("\n");*/

        Toast.makeText(getContext(),"已保存信息",Toast.LENGTH_SHORT).show();
        //Log.i("FILEE",FILE_FOLDER);

        currentPosition.setLength(0);
        currentAcc.setLength(0);
        currentAngularspeed.setLength(0);
        currentgyroscope.setLength(0);
        currentSrc.setLength(0);
        currentSrc_HHMMSS.setLength(0);

        uploadFile(FILE_FOLDER + FOLDER_ACC + File.separator+"acc_data_" +formatter.format(date)+".csv",
                FILE_FOLDER + FOLDER_G + File.separator+"gyroscope_data_" +formatter.format(date)+".csv",
                FILE_FOLDER + File.separator+"sound_data"+File.separator+"wav"+File.separator+"audio.wav",
                "acc_data_" +formatter.format(date)+".csv",
                "gyroscope_data_" +formatter.format(date)+".csv",
                "audio.wav");
    }

    private void uploadFile(String path1, String path2,String path3,String filename1,String filename2,String filename3) {
        File file1 = new File(path1);
        File file2 = new File(path2);
        File file3 = new File(path3);

        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file1",filename1, RequestBody.create(MediaType.parse("text/csv"),file1))
                .addFormDataPart("file2",filename2,RequestBody.create(MediaType.parse("text/csv"),file2))
                .addFormDataPart("file3",filename3,RequestBody.create(MediaType.parse("audio/x-wav"),file3)) //   contentType.put(".wav" , "audio/x-wav");
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        //Toast.makeText(MainActivity.this,"开始上传"+file.getAbsolutePath(),Toast.LENGTH_LONG).show();
        //并没有创建，进入不到这里
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30,TimeUnit.SECONDS)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("HomeFragment", "onFailure: "+e.getLocalizedMessage());
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        //Toast.makeText(MainActivity.this,"上传失败"+file1.getAbsolutePath(),Toast.LENGTH_LONG).show();
//                    }
//                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String res = response.body().string();
                Log.d("HomeFragment", "onResponse: "+res);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        result.setText(res);
//                    }
//                });
            }
        });

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Log.i("onViewCreated","onViewCreated");

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
        option.setScanSpan(5000);
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
        //如果设置了该接口，首次启动定位时,会先判断当前wi-Fi是否超出有效期，若超出有效期，会先重新扫描wi-Fi，然后定位
        option.setEnableSimulateGps(false);
        //可选，设置是否需要过滤GPs仿真结果，默认需要，即参数为false
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            // 获取加速度传感器的三个参数
            ax = sensorEvent.values[0];
            ay = sensorEvent.values[1];
            az = sensorEvent.values[2];

        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION){
            angleZ = sensorEvent.values[0];
            angleX = sensorEvent.values[1];
            angleY = sensorEvent.values[2];
        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // 获取加速度传感器的三个参数
            gx = sensorEvent.values[0];
            gy = sensorEvent.values[1];
            gz = sensorEvent.values[2];
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

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

            getwifimac();
            //数据开始记录时
            if (isStartRecord==1) {

                currentPosition.append(System.currentTimeMillis()).append(",");

                //添加纬度
                if (location.getLatitude() > 0.1) {
                    currentPosition.append(location.getLatitude()).append(",");
                    locationLatLong[0]= String.valueOf(location.getLatitude());
//                    locationLatLong[0]=1;

                } else {
                    currentPosition.append((String) null).append(",");
                    locationLatLong[0]= "null";
                }

                //添加经度
                if (location.getLongitude() > 0.1) {
                    currentPosition.append(location.getLongitude()).append(",");
                    locationLatLong[1]= String.valueOf(location.getLongitude());
                    //Log.i("ASDFGGG",""+ locationLatLong[0] + " ," + locationLatLong[1]);

                } else {
                    currentPosition.append((String) null).append(",");
                    locationLatLong[1]="null";
                }

                currentPosition.append(wifi_BSSID).append("\n");

            }


            locationInfo.setText("经纬度：\n"+"" + location.getLatitude() + "\n"
                    + location.getLongitude( ) + "\n\n"
                    + "wifi mac地址:\n" + wifi_BSSID
            );

            Log.i("KEEPRUN","KEEPRUN");

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

        //添加纹理图片
        List<BitmapDescriptor> textureList = new ArrayList<BitmapDescriptor>();
        BitmapDescriptor mRedTexture = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_road_red_arrow);//箭头图片
        textureList.add(mRedTexture);


        if(pointsss.longitude>0.1 && pointsss.latitude>0.1 )
            ppp.add(pointsss);
        // 添加纹理图片对应的顺序
        List<Integer> textureIndexs = new ArrayList<Integer>();
        for (int i=0;i<ppp.size();i++){
            textureIndexs.add(0);
        }
        if(ppp.size()>=2){
            OverlayOptions mOverlay=new PolylineOptions()
                    //折线宽度
                    .width(16)
                    //折线颜色
                    .color(0xAAFF0000)
                    //折线坐标点列表
                    .points(ppp)
                    .dottedLine(true)
                    .textureIndex(textureIndexs)//设置分段纹理index数组
                    .customTextureList(textureList);//设置线段的纹理，建议纹理资源长宽均为2的n次方;
            Overlay mPolyline=(Polyline)mBaiduMap.addOverlay(mOverlay);
            mPolyline.setZIndex(3);
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
        sensorManager.unregisterListener(this);
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

    public void getwifimac(){ //获取wifi——mac
        String sdk = Build.VERSION.SDK;
        int anInt = Integer.parseInt(sdk);
        if (anInt >= 27) { //判断版本
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);

            } else {
                wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiInfo = wifiManager.getConnectionInfo();
                wifi_BSSID = wifiInfo.getBSSID();

            }
        }else{
            wifiManager = (WifiManager)getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiInfo = wifiManager.getConnectionInfo();
            //textView.setText(wifiInfo.getBSSID());
            wifi_BSSID = wifiInfo.getBSSID();
        }
    }

    private void bigImageLoader(Bitmap bitmap){
        final Dialog dialog = new Dialog(getActivity());
        ImageView image = new ImageView(getContext());
        image.setImageBitmap(bitmap);
        dialog.setContentView(image);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dialog.cancel();
            }
        });
    }

    public static String[] getCurrentPositionDoubleArray() {
        return locationLatLong;
    }
}