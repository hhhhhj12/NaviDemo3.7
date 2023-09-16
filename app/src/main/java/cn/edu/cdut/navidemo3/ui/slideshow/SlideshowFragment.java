package cn.edu.cdut.navidemo3.ui.slideshow;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.edu.cdut.navidemo3.AddDataActivity;
import cn.edu.cdut.navidemo3.LabelActivity;
import cn.edu.cdut.navidemo3.LabelModel;
import cn.edu.cdut.navidemo3.MyDatabaseHelper;
import cn.edu.cdut.navidemo3.R;
import cn.edu.cdut.navidemo3.UpdateActivity;
import cn.edu.cdut.navidemo3.databinding.FragmentSlideshowBinding;

import cn.edu.cdut.navidemo3.record.AudioRecorder;
import cn.edu.cdut.navidemo3.record.FileUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class SlideshowFragment extends Fragment {
    private FragmentSlideshowBinding binding;
    private MyDatabaseHelper dbHelper; //数据库对象
    private Button btn_addlabel,btn_selectDate,btn_dropTable;
    private TextView tv_date;
    private ListView view_all;
    private CalendarView calendarView;
    private String Date;
    private static  String user = "lyr";

    private AlertDialog alertDialog = null;
    private AlertDialog.Builder builder = null;
    //读取用户名
    SharedPreferences preference;
    private String path;

    private String url = "http://192.168.32.188:5000/upload"; //http://192.168.32.188:5000/upload
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        preference = getActivity().getSharedPreferences("user",MODE_PRIVATE);
        user = preference.getString("name","请输入用户名：");


        btn_selectDate = binding.btnSelectDate;
        calendarView = binding.calendarViewId;
        tv_date = binding.tvDate;
        dbHelper = new MyDatabaseHelper(getContext());

//      long numRows = DatabaseUtils.longForQuery(db,"SELECT COUNT(*) FROM book",null);//查表里有多少数据
        view_all = binding.viewAll;
        btn_addlabel = binding.addLabelF;
        btn_addlabel.setEnabled(false);
        btn_addlabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddDataActivity.class);
                intent.putExtra("tableName",user+Date);
                startActivity(intent);
            }
        });

        btn_dropTable =binding.btnDropTable;
        btn_dropTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.dropTable(user+Date);
                view_all(user+Date);
            }
        });

        view_all.setOnItemClickListener(new AdapterView.OnItemClickListener() { //点击标签进行修改或删除
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LabelModel labelModel = (LabelModel) parent.getItemAtPosition(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle("请选择操作");
                dialog.setNegativeButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String s = dbHelper.deleteOne(labelModel);
                        Toast.makeText(getContext(),"DELETE:"+s,Toast.LENGTH_SHORT).show();
                        view_all(user+Date);
                    }
                });
                dialog.setPositiveButton("修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(getContext(), UpdateActivity.class);
                        intent.putExtra("tableName",user+Date);
                        intent.putExtra("id",labelModel.getId());
                        intent.putExtra("startTime",labelModel.getStartTime());
                        intent.putExtra("endTime",labelModel.getEndTime());
                        intent.putExtra("action",labelModel.getAction());
                        intent.putExtra("position",labelModel.getPosition());
                        intent.putExtra("audio",labelModel.getAudio());
                        startActivity(intent);
                    }
                });
                dialog.create();
                dialog.show();

            }
        });

        btn_selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.setVisibility(View.VISIBLE);
            }
        });

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                SharedPreferences preference = getActivity().getSharedPreferences("user",MODE_PRIVATE);

                if (preference.getString("name","请输入用户名：").contains("：")){
                    Toast.makeText(getActivity(),"请到导航栏中修改用户名！",Toast.LENGTH_SHORT).show();
                    return;
                }
                String m = new String();
                String d = new String();
                if (month+1<10){
                    m = "0"+String.valueOf(month+1);
                }else{
                    m = String.valueOf(month+1);
                }
                if (dayOfMonth<10){
                    d = "0"+String.valueOf(dayOfMonth);
                }else{
                    d =String.valueOf(dayOfMonth);
                }
                Date = String.valueOf(year)+m+d;
                tv_date.setText(Date);
                view_all(user+Date);
                calendarView.setVisibility(View.GONE);
                btn_addlabel.setEnabled(true);
            }
        });

        Button btn_uploadLables = binding.btnUploadlables;
        btn_uploadLables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    path = "/storage/emulated/0/upload/label1.db";
                    uploadFile(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return root;
    }
    public void view_all(String tableName){
        ArrayAdapter<LabelModel> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,dbHelper.getAll(tableName));
        view_all.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void uploadFile(String path) throws IOException {  //用uploadFile函数上传文件
        OkHttpClient client = new OkHttpClient();//避免多次生成实例
        File file1 = new File(path);
        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("label","label1.db", RequestBody.create(MediaType.parse("multipart/form-data"),file1))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30,TimeUnit.SECONDS)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //Log.d(TAG, "onFailure: "+e.getLocalizedMessage());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"上传失败",Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String res = response.body().string();
                //Log.d(TAG, "onResponse: "+res);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //result.setText(res);
                        Toast.makeText(getContext(),"上传成功,服务器返回：" +res ,Toast.LENGTH_LONG).show();

                    }
                });
            }
        });
        client =null;
    }
}