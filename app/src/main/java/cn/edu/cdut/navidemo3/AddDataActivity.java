package cn.edu.cdut.navidemo3;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddDataActivity extends AppCompatActivity implements View.OnClickListener,TimePickerDialog.OnTimeSetListener {

    private TextView tv_startTime,tv_endTime,tv_action,tv_position,tv_audio;
    private AlertDialog alertDialog = null;
    private AlertDialog.Builder builder = null;
    final String[] position = new String[]{"宿舍","食堂","教室","自习室或图书馆","户外"};
    final String[] action = new String[]{"坐","站","走","跑"};
    final String[] audio = new String[]{"安静","嘈杂","交谈"};
    private String time,startTime,endTime,Action,Position,Audio,tableName;
    private Button btn_add;
    private MyDatabaseHelper dbHelper; //数据库对象


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data);

        Intent intent = getIntent();
        tableName = intent.getStringExtra("tableName");

        tv_startTime = findViewById(R.id.tv_startTime);
        tv_endTime = findViewById(R.id.tv_endTime);
        tv_action = findViewById(R.id.tv_action);
        tv_position = findViewById(R.id.tv_position);
        tv_audio = findViewById(R.id.tv_audio);
        btn_add = findViewById(R.id.btn_add);
        setListeners();

        tv_startTime.setOnClickListener(this);
        tv_endTime.setOnClickListener(this);
        dbHelper = new MyDatabaseHelper(this);

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
        case R.id.tv_startTime:
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog dialog = new TimePickerDialog(AddDataActivity.this,this,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
            dialog.show();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    startTime = time;
                    tv_startTime.setText("起始时间："+startTime);
                }
            });
            break;
        case R.id.tv_endTime:
            Calendar calendar1 = Calendar.getInstance();
            TimePickerDialog dialog1 = new TimePickerDialog(AddDataActivity.this,this,
                    calendar1.get(Calendar.HOUR_OF_DAY),
                    calendar1.get(Calendar.MINUTE),
                    true);
            dialog1.show();
            dialog1.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    endTime = time;
                    tv_endTime.setText("结束时间："+endTime);
                }
            });
            break;
        }
    }

    private void setListeners(){
        OnClick onClick = new OnClick();

        tv_action.setOnClickListener(onClick);
        tv_position.setOnClickListener(onClick);
        tv_audio.setOnClickListener(onClick);
        btn_add.setOnClickListener(onClick);

    }

    /**
     * Called when the user is done setting a new time and the dialog has
     * closed.
     *
     * @param view      the view associated with this listener
     * @param hourOfDay the hour that was set
     * @param minute    the minute that was set
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
         time = String.format("%d时%d分",hourOfDay,minute);
    }

    public class OnClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.tv_action:
                    setDialog(tv_action,action,"行为");
                    break;
                case R.id.tv_position:
                    setDialog(tv_position,position,"位置");
                    break;
                case R.id.tv_audio:
                    setDialog(tv_audio,audio,"环境音");
                    break;
                case R.id.btn_add:
                    if (tv_startTime.getText().equals("起始时间:")||
                        tv_endTime.getText().equals("结束时间：")||
                        tv_action.getText().equals("行为：")||
                        tv_position.getText().equals("地点：")||
                        tv_audio.getText().equals("环境音：")){
                        Toast.makeText(AddDataActivity.this,"请输入完整标签",Toast.LENGTH_SHORT).show();
                    }else {
                        LabelModel labelModel = new LabelModel(-1,startTime,endTime
                                ,tv_action.getText().toString(),tv_position.getText().toString()
                                ,tv_audio.getText().toString());
                        String s = dbHelper.addOne(labelModel, tableName);
                        Toast.makeText(AddDataActivity.this,"ADD:"+s,Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }


    private void setDialog(TextView textView,String[] strings,String s){
        alertDialog = null;
        builder = new AlertDialog.Builder(AddDataActivity.this);
        alertDialog = builder.setTitle("选择你的"+s)
                .setSingleChoiceItems(strings, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(AddDataActivity.this,"你选择了"+strings[which],Toast.LENGTH_SHORT).show();
                        String result = new String(strings[which]);
                        textView.setText(result);
                    }
                }).create();
        alertDialog.show();
    }
}