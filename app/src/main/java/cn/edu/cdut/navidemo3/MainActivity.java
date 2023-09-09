package cn.edu.cdut.navidemo3;

import static com.baidu.vi.VIContext.getContext;
import static cn.edu.cdut.navidemo3.ESApplication._alarmManager;
import static cn.edu.cdut.navidemo3.ESApplication._predeterminedLabels;

import static cn.edu.cdut.navidemo3.ESApplication._sensorManager;
import static cn.edu.cdut.navidemo3.ESApplication.getTheAppContext;
import static cn.edu.cdut.navidemo3.extra.data.ESIntentService.LOG_TAG;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.google.android.material.navigation.NavigationView;

import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.List;

import cn.edu.cdut.navidemo3.databinding.ActivityMainBinding;
import cn.edu.cdut.navidemo3.extra.data.BaseActivity;
import cn.edu.cdut.navidemo3.extra.data.ESLabelStruct;
import cn.edu.cdut.navidemo3.extra.data.ESTimestamp;
import cn.edu.cdut.navidemo3.extra.data.FeedbackActivity;
import cn.edu.cdut.navidemo3.extra.data.SettingsActivity;

public class MainActivity extends BaseActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    LottieAnimationView lottie;
    private static MainActivity instance;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;
    EditText editTextName;
    TextView nav_header_name_tv;

    SharedPreferences user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance =this;
        SDKInitializer.setAgreePrivacy(getApplicationContext(),true);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(getApplicationContext());

        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
/*      //这是控制小信封的
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        lottie = findViewById(R.id.lottie);

        user = getSharedPreferences("user",MODE_PRIVATE);
        nav_header_name_tv = findViewById(R.id.nav_header_name);
        //nav_header_name_tv.setText(user.getString("name",""));

        if (navigationView.getHeaderCount() > 0){
            //获取NavigationView中header布局中的view
            View header = navigationView.getHeaderView(0);
            //获取header中的控件，而不是直接findViewById
            TextView viewById = (TextView) header.findViewById(R.id.nav_header_name);
            viewById.setText(user.getString("name","请输入用户名："));

        }


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        //将导航图和AppBarConfiguration关联起来

        //获取navController,通过MainActivity空白的NavHostFragment获取
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,R.id.nav_stress,R.id.nav_summary
                //navController.getGraph()
        )
                .setOpenableLayout(drawer)
                .build();
        //将AppBarConfiguration和NavController绑定起来
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //保存运行
/*        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
        wakeLock.acquire();*/


        //testGet();


        List<String> permissionList = new ArrayList<String>();
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_PHONE_STATE )!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED){
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


    }



    public static MainActivity getInstance(){
        return instance;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent intent;
        //Log.i("TESTT", String.valueOf(item.getItemId()));
        switch (item.getItemId())
        {
            case R.id.action_settings:
                //Toast.makeText(this,"You clicked first item",Toast.LENGTH_SHORT).show();
                lottie.setVisibility(View.VISIBLE);
                lottie.animate().setDuration(5000).setStartDelay(0);
                //lottie.playAnimation();
                showToast(getApplicationContext(),"正在生成分析报表...");
                //Toast.makeText(getApplicationContext(),"正在生成分析报表...",Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                         Intent intent = new Intent(MainActivity.this, ScrollingActivity.class);
                        startActivity(intent);
                        lottie.setVisibility(View.INVISIBLE);
                    }
                },5000);

                break;

            case R.id.action_settingss:
                //打开设置页面
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_active_feedback:
                //绿色小加号
                // Check should we collect data now,检查我们现在是否应该收集数据:
/*                if (!getTheESApplication().shouldDataCollectionBeOn()) {
                    Log.i(LOG_TAG,"Active feedback pressed, but data-collection is off");
                    AlertDialog.Builder builder = new AlertDialog.Builder(this).
                            setIcon(R.drawable.ic_launcher_foreground).setMessage(R.string.alert_for_active_feedback_while_data_collection_off).
                            setTitle("ExtraSensory").setNegativeButton(R.string.ok_button_text,new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.create().show();
                    break;
                }*/
                intent = new Intent(MainActivity.this, FeedbackActivity.class);
                //开始反馈前设置反馈参数
                FeedbackActivity.setFeedbackParametersBeforeStartingFeedback(new FeedbackActivity.FeedbackParameters());
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
    private void showToast(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        toast.setGravity(Gravity.TOP, 0, size.y / 4);
        toast.show();
    }
    /**
     * Start user initiated recording (and schedule).
     * This function first stops any current recording (if there is one), and stops the recording schedule,
     * then starts a new recording schedule.
     * The given labels are used for the started recording from now until the end time specified by the user.
     *
     * @param labelsToAssign - The labels to assign to the following activities
     * @param validForHowManyMinutes - How many minutes should the given labels be automatically assigned?
     * @param initiatedByNotification - Was this active feedback initiated by a notification/reminder?
     * @param timestampOpenFeedbackForm The timestamp of the time the user opened the feedback form for this activity (and actually sent feedback), or null in case this feedback did not involve the feedback form (e.g. confirmation-notification)
     * @param timestampPressSendButton The timestamp of the time the user pressed the "send feedback" button for this activity, or null in case this feedback did not involve the feedback form (e.g. confirmation-notification)
     * @param timestampNotification The timestamp of the time the notification showed, which eventually yielded this activity's update, or null if this feedback was not initiated by notification
     * @param timestampUserRespondToNotification The timestamp of the time the user responded to the notification by pressing an answer button, which yielded this activity's update, or null if this feedback was not initiated by notification
     */
    public void startActiveFeedback(ESLabelStruct labelsToAssign, int validForHowManyMinutes, boolean initiatedByNotification,
                                    ESTimestamp timestampOpenFeedbackForm, ESTimestamp timestampPressSendButton,
                                    ESTimestamp timestampNotification, ESTimestamp timestampUserRespondToNotification) {
        _predeterminedLabels.setPredeterminedLabels(labelsToAssign, validForHowManyMinutes,initiatedByNotification,
                timestampOpenFeedbackForm,timestampPressSendButton,timestampNotification,timestampUserRespondToNotification);
        stopCurrentRecordingAndRecordingSchedule();
        startRecordingSchedule(0);
    }
    public void stopCurrentRecordingAndRecordingSchedule() {
        stopRecordingSchedule();
        stopCurrentRecording();
    }

    /**
     * Start a repeating schedule of recording sessions (every 1 minute).
     *
     * @param millisWaitBeforeStart - time to wait (milliseconds) before the first recording session in this schedule.
     */
    private void startRecordingSchedule(long millisWaitBeforeStart) {
        if (_alarmManager == null) {
            Log.e(LOG_TAG, "Alarm manager is null");
            return;
        }
    }

    private void stopRecordingSchedule() {
        if (_alarmManager == null) {
            Log.e(LOG_TAG, "Alarm manager is null");
            return;
        }

    }

    private void stopCurrentRecording() {
        Log.i(LOG_TAG,"Stopping current recording session.");
        _sensorManager.stopRecordingSensors();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
/*        wakeLock.release();*/
    }

    public void onNavClick(View view){
        nav_header_name_tv = findViewById(R.id.nav_header_name);
        user = getSharedPreferences("user",MODE_PRIVATE);
        SharedPreferences.Editor edit = user.edit();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("请输入");
        editTextName= new EditText(MainActivity.this);
        builder.setIcon(R.drawable.ic_baseline_directions_run_24);
        builder.setView(editTextName);

        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Toast.makeText(getTheAppContext(),editTextName.getText(),Toast.LENGTH_SHORT).show();
                nav_header_name_tv.setText(editTextName.getText());
                edit.putString("name", String.valueOf(editTextName.getText()));
                edit.commit();
            }
        });
        builder.setNegativeButton("否",null);
        builder.show();



    }


}