package cn.edu.cdut.navidemo3;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LabelActivity extends AppCompatActivity {
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label);


        preference = getSharedPreferences("user",MODE_PRIVATE);
        user = preference.getString("name","请输入用户名：");


        btn_selectDate = findViewById(R.id.btn_selectDate);
        calendarView = findViewById(R.id.calendarViewId);
        tv_date = findViewById(R.id.tv_date);
        dbHelper = new MyDatabaseHelper(this);

//      long numRows = DatabaseUtils.longForQuery(db,"SELECT COUNT(*) FROM book",null);//查表里有多少数据
        view_all = findViewById(R.id.view_all);
        btn_addlabel = findViewById(R.id.add_label);
        btn_addlabel.setEnabled(false);
        btn_addlabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LabelActivity.this,AddDataActivity.class);
                intent.putExtra("tableName",user+Date);
                startActivity(intent);
            }
        });

        btn_dropTable = findViewById(R.id.btn_dropTable);
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(LabelActivity.this);
                dialog.setTitle("请选择操作");
                dialog.setNegativeButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String s = dbHelper.deleteOne(labelModel);
                        Toast.makeText(LabelActivity.this,"DELETE:"+s,Toast.LENGTH_SHORT).show();
                        view_all(user+Date);
                    }
                });
                dialog.setPositiveButton("修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(LabelActivity.this,UpdateActivity.class);
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
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        view_all(user+Date);
    }

    public void view_all(String tableName){
        ArrayAdapter<LabelModel> adapter = new ArrayAdapter<>(LabelActivity.this, android.R.layout.simple_list_item_1,dbHelper.getAll(tableName));
        view_all.setAdapter(adapter);
    }
}