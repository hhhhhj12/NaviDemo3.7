package cn.edu.cdut.navidemo3;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cn.edu.cdut.navidemo3.record.AudioRecorder;
import cn.edu.cdut.navidemo3.ui.home.HomeFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyService extends Service implements SensorEventListener {
    PowerManager.WakeLock wakeLock;
    private Handler handler = new Handler();

    LocationManager mLocationManager;
    private String[] labels = new String[]{"static", "walk", "run", "dormitory", "classroom", "canteen", "lab", "quiet", "noisy"};
    StringBuilder currentAcc = new StringBuilder();
    StringBuilder currentgyroscope = new StringBuilder();
    StringBuilder currentPosition = new StringBuilder();
    private String url = "http://192.168.32.188:5000/upload"; //http://192.168.32.188:5000/upload
    OkHttpClient okHttpClient;
    private final int TimeForAcc = 40;    //时间间隔，   单位 ms
    private final int TimeForGPS = 5000;
    public static final String FILE_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
            + "AboutView" + File.separator + "data";
    DecimalFormat df = new DecimalFormat("0.0000");
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");
    private int isStartRecord = 1;
    private static final String FOLDER_ACC = File.separator + "acc_data";
    public static final String FOLDER_POSITION = File.separator + "position_data";

    private static final String FOLDER_G = File.separator + "gyroscope_data";
    public AudioRecorder audioRecorder;
    SharedPreferences label;
    //记录加速度数据
    double ax = 0;
    double ay = 0;
    double az = 0;
    SensorManager sensorManager;
    Sensor sensor_o;
    Sensor sensor_acc;
    SharedPreferences user;
    float angleX = 0;
    float angleY = 0;
    float angleZ = 0;
    Runnable runnable_acc_g, runnable_gps;
    Timer timer;
    LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        label = getSharedPreferences("label", Context.MODE_PRIVATE);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
/*        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // 获取当前纬度
                double latitude = location.getLatitude();
                // 获取当前经度
                double longitude = location.getLongitude();
//                currentPosition.append(latitude);
//                currentPosition.append(longitude);
            }
        });*/

        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, MyService.class.getName());
        wakeLock.acquire();

        startForeground();
        //        sensorManager = (SensorManager) getActivity().getApplicationContext().getSystemService(SENSOR_SERVICE);

        sensorManager = (SensorManager) getApplication().getSystemService(SENSOR_SERVICE);
        sensor_acc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensor_o = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        //SensorManager.SENSOR_DELAY_NORMAL
        sensorManager.registerListener(this, sensor_acc, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensor_o, SensorManager.SENSOR_DELAY_GAME);


        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
/*                Looper.prepare();
                Toast.makeText(getApplicationContext(),"service",Toast.LENGTH_SHORT).show();
                Looper.loop();*/
                Log.v("wang", "服务还活着....");

                //告诉home开始执行

                startAudioRecord();
                startACC_Groscope_GPS();

                //采集数据30秒并保存
                //每开始采集传感器数据30秒后停止
                long delayMillis = 30000;

                Runnable runnable_stop = new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {

                        //每隔一段时间要重复执行的代码
                        handler.postDelayed(this, delayMillis);

                        //告诉home结束执行

                        finishRecordAndSaveData();
                        audioRecorder.stopRecord(getApplication());

//                        isStartRecord=0;


                        handler.removeCallbacks(this);
                        handler.removeCallbacks(runnable_acc_g);
                    }
                };
                handler.postDelayed(runnable_stop, delayMillis);

            }
        }, 0, 5 * 60000);/* 5*60000 */
    }

    public  Location getLocation(Context context) {

        /*获取LocationManager对象*/
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        String provider = getProvider(locationManager);
        if (provider == null) {
            Toast.makeText(context, "定位失败", Toast.LENGTH_SHORT).show();
        }


        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        return locationManager.getLastKnownLocation(provider);
    }

    private Location getLastKnownLocation(Context context) {
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    /**
     * 根据LocationManager获取定位信息的提供者
     * @param locationManager
     * @return
     */
    private  String getProvider(LocationManager locationManager){

        //获取位置信息提供者列表
        List<String> providerList = locationManager.getProviders(true);

        if (providerList.contains(LocationManager.NETWORK_PROVIDER)){
            //获取NETWORK定位
            return LocationManager.NETWORK_PROVIDER;
        }else if (providerList.contains(LocationManager.GPS_PROVIDER)){
            //获取GPS定位
            return LocationManager.GPS_PROVIDER;
        }
        return null;
    }

    private void startACC_Groscope_GPS() {
        int act = label.getInt("act", 1);
        int loc = label.getInt("loc", 4);
        int sound = label.getInt("sound", 8);


        //显示并保存加速度数据，40ms一次 (25Hz)
        runnable_acc_g = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                handler.postDelayed(this, TimeForAcc);
                //每隔一段时间要重复执行的代码
                //开始记录数据时
                if (isStartRecord==1) {
                    currentAcc.append(System.currentTimeMillis()).append(",")
                            .append(df.format(ax)).append(",")
                            .append(df.format(ay)).append(",")
                            .append(df.format(az)).append("\n");
/*                            .append(labels[act-1]).append(",")
                            .append(labels[loc-1]).append(",")
                            .append(labels[sound-1]).append("\n");*/
                    currentgyroscope.append(System.currentTimeMillis()).append(",")
                            .append(df.format(angleX)).append(",")
                            .append(df.format(angleY)).append(",")
                            .append(df.format(angleZ)).append("\n");
/*                            .append(labels[act-1]).append(",")
                            .append(labels[loc-1]).append(",")
                            .append(labels[sound-1]).append("\n");*/


                    //添加纬度
                    //添加经度

                }
            }
        };
        runnable_gps = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this,TimeForGPS);
                if (isStartRecord==1) {
                    //Location location = getLocation(getApplicationContext());
                    Location location = getLastKnownLocation(getApplicationContext());

                    if (location!=null){
                        currentPosition.append(System.currentTimeMillis()).append(",")
                                .append(location.getLatitude()).append(",")
                                .append(location.getLongitude()).append("\n");
                    }

                }
            }
        };
        handler.postDelayed(runnable_acc_g, TimeForAcc);	//启动计时器
        handler.postDelayed(runnable_gps,TimeForGPS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 启动前台服务
     */
    private void startForeground() {
        String channelId = null;
        // 8.0 以上需要特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("kim.hsl", "ForegroundService");
        } else {
            channelId = "";
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        Notification notification = builder.setOngoing(true)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.checkmark_in_circle))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("采集数据中")
                .setContentText("采集数据中...")
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setWhen(System.currentTimeMillis())
                .build();
        startForeground(1, notification);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopForeground(true);

        if (wakeLock!=null){
            wakeLock.release();
            wakeLock=null;
        }

        timer.cancel();
        audioRecorder.stopRecord(getApplicationContext());

    }

    /**
     * 创建通知通道
     * @param channelId
     * @param channelName
     * @return
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName){
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }


    private void finishRecordAndSaveData() {
        Date date = new Date(System.currentTimeMillis());
/*                WriteData2CSVThread myThread_saveposision = new WriteData2CSVThread(currentPosition,FILE_FOLDER + FOLDER_POSITION,"position_data_" +formatter.format(date)+".csv");
                myThread_saveposision.run();*/

        WriteData2CSVThread myThread_saveposision = new WriteData2CSVThread(currentPosition,FILE_FOLDER + FOLDER_POSITION,"position_data_" +formatter.format(date)+".csv");
        myThread_saveposision.run();

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


        //Log.i("FILEE",FILE_FOLDER);

        currentAcc.setLength(0);
        currentgyroscope.setLength(0);

        uploadFile(FILE_FOLDER + FOLDER_ACC + File.separator+"acc_data_" +formatter.format(date)+".csv",
                FILE_FOLDER + FOLDER_G + File.separator+"gyroscope_data_" +formatter.format(date)+".csv",
                FILE_FOLDER + File.separator+"sound_data"+File.separator+"wav"+File.separator+"audio.wav",
                FILE_FOLDER + FOLDER_POSITION+File.separator+"position_data_" +formatter.format(date)+".csv",
                "acc_data_" +formatter.format(date)+".csv",
                "gyroscope_data_" +formatter.format(date)+".csv",
                "audio.wav",
                "position_data_" +formatter.format(date)+".csv"
                );
    }

    private void uploadFile(String path1, String path2,String path3,String path4,String filename1,String filename2,String filename3,String filename4) {
        File file1 = new File(path1);
        File file2 = new File(path2);
        File file3 = new File(path3);
        File file4 = new File(path4);

        user = getSharedPreferences("user",MODE_PRIVATE);
        String name = user.getString("name", "请输入用户名：");
        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file1",filename1, RequestBody.create(MediaType.parse("text/csv"),file1))
                .addFormDataPart("file2",filename2,RequestBody.create(MediaType.parse("text/csv"),file2))
                .addFormDataPart("file3",filename3,RequestBody.create(MediaType.parse("audio/x-wav"),file3)) //   contentType.put(".wav" , "audio/x-wav");
                .addFormDataPart("file4",filename4,RequestBody.create(MediaType.parse("text/csv"),file4))
                .build();
        Request request = new Request.Builder()
                .url(url)//+"/"+name
                .post(body)
                .build();
        //Toast.makeText(MainActivity.this,"开始上传"+file.getAbsolutePath(),Toast.LENGTH_LONG).show();
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
    private void startAudioRecord() {
        audioRecorder = AudioRecorder.getInstance();
        if (isStartRecord==1){
            if (audioRecorder.getStatus()==AudioRecorder.Status.STATUS_NO_READY) {
                String fileName = "audio";
                audioRecorder.createDefaultAudio(fileName);
                audioRecorder.startRecord(null);

            }
        }
    }


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

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
