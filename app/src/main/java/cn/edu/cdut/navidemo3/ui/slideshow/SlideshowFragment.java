package cn.edu.cdut.navidemo3.ui.slideshow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.edu.cdut.navidemo3.R;
import cn.edu.cdut.navidemo3.databinding.FragmentSlideshowBinding;

import cn.edu.cdut.navidemo3.record.AudioRecorder;
import cn.edu.cdut.navidemo3.record.FileUtil;



public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;

    private int REQUEST_CODE = 1000;
    public AudioRecorder audioRecorder;
    private MediaPlayer mMediaPlayer;
    private Handler handler = new Handler();
    private int isRecord = 0 ;
    long recordtime = 60000;//ms

    //创建倒计时对象，用来向用户展示录音所剩倒计时
    CountDownTimer countDownTimer= new CountDownTimer(60000, 1000) {
        public void onTick(long millisUntilFinished) {
            binding.textCount.setText("seconds remaining: " + millisUntilFinished / 1000);
            Log.i("KEEPRUN","KEEPRUN");
        }
        public void onFinish() {
            binding.textCount.setText("done!");
            cancel();

        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSlideshow;
        slideshowViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        //获取权限
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
        }


        //得到audioRecorder对象
        initRecord();



        //点击  控制开始停止录音
        binding.button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.start();
                if (isRecord==1 ) {
                    //如果上一段录音正在播放
                    Toast.makeText(getContext(), "已经在录音啦！", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    if (audioRecorder.getStatus() == AudioRecorder.Status.STATUS_NO_READY) {
                        //初始化录音
                        String fileName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
                        audioRecorder.createDefaultAudio(fileName);
                        audioRecorder.startRecord(null);
                        binding.icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_0328_run));
                        isRecord = 1;
                        //点击按钮后录音一分钟
                        Runnable runnable_record = new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {

                                //Log.i("looklook","looklook");
                                handler.postDelayed(this, recordtime);//1分钟
                                //每隔一段时间要重复执行的代码
                                if (isRecord == 1) {

                                    //停止并保存
                                    audioRecorder.stopRecord(getContext());
                                    binding.icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_0328_stop));
                                    handler.removeCallbacks(this);
                                }else {
                                    handler.removeCallbacks(this);
                                    isRecord = 0;
                                    binding.icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_0328_stop));
                                }
                            }

                        };
                        handler.postDelayed(runnable_record, recordtime);	//启动计时器

                    } else {
                        //停止录音
                        //audioRecorder.stopRecord();
                        //binding.button1.setText("开始录音");
                        //binding.icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_0328_stop));
                    }

                } catch (IllegalStateException e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });




        //停止录音
        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecorder.canel();
                binding.icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_0328_stop));
                binding.textCount.setText("倒计时");
                countDownTimer.cancel();
                isRecord = 0;

                }

        });







        return root;
    }

    private void initRecord() {
        audioRecorder = AudioRecorder.getInstance();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        countDownTimer.cancel();
        binding = null;
    }
}