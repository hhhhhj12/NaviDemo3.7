package cn.edu.cdut.navidemo3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    public static final String COLUMN_ID = "id";
    private static final String COLUMN_STARTTIME ="startTime" ;
    private static final String COLUMN_ENDTIME = "endTime";
    private static final String COLUMN_ACTION = "move";
    private static final String COLUMN_POSITION = "position";
    private static final String COLUMN_AUDIO = "audio";
    public String TABLE_NAME="lyr22222";
    private Context mContext;


    public MyDatabaseHelper(@Nullable Context context) {
        super(context, "/storage/emulated/0/upload/label1.db", null, 1);//name数据库名字
        mContext = context;
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) { //只调用一次（没有数据库的时候调用）
        String sql = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
                + COLUMN_STARTTIME + " TEXT,"
                + COLUMN_ENDTIME + " TEXT,"
                + COLUMN_ACTION + " TEXT,"
                + COLUMN_POSITION + " TEXT,"
                + COLUMN_AUDIO + " TEXT);";
        if (HaveData(db,TABLE_NAME)){
            return;
        }
        db.execSQL(sql);//建立数据表
        Toast.makeText(mContext,TABLE_NAME+"Creat succeeded", Toast.LENGTH_LONG).show();

    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     *
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String addOne(LabelModel labelModel,String tableName){
        //添加数据传入参数：数据和表名
        TABLE_NAME = tableName;

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STARTTIME,labelModel.getStartTime());
        cv.put(COLUMN_ENDTIME,labelModel.getEndTime());
        cv.put(COLUMN_ACTION,labelModel.getAction());
        cv.put(COLUMN_POSITION,labelModel.getPosition());
        cv.put(COLUMN_AUDIO,labelModel.getAudio());
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        if (HaveData(sqLiteDatabase,tableName)){ //判断表是否存在，如果不存在就调用oncreat新建一个表
            Log.d("存在", tableName);
        }else{
            Log.d("不存在，创建新表：", tableName);
            onCreate(sqLiteDatabase);
        }
        long insert = sqLiteDatabase.insert(TABLE_NAME,COLUMN_STARTTIME,cv);//把数据插入表中。第二个参数随意选择一个列名
        if (insert == -1){
            return "fail";
        }
        sqLiteDatabase.close();
        return "success";
    }

    public String deleteOne(LabelModel labelModel){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        int delete = sqLiteDatabase.delete(TABLE_NAME,COLUMN_ID+"=?",new String[]{String.valueOf(labelModel.getId())});
        if (delete == 0){
            return "fail";
        }
        sqLiteDatabase.close();
        return "success";
    }

    public String updateOne(LabelModel labelModel,String tableName){

        //添加数据传入参数：数据和表名
        TABLE_NAME = tableName;

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STARTTIME,labelModel.getStartTime());
        cv.put(COLUMN_ENDTIME,labelModel.getEndTime());
        cv.put(COLUMN_ACTION,labelModel.getAction());
        cv.put(COLUMN_POSITION,labelModel.getPosition());
        cv.put(COLUMN_AUDIO,labelModel.getAudio());

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        if (HaveData(sqLiteDatabase,tableName)){ //判断表是否存在，如果不存在就调用oncreat新建一个表  ||tableName.equals("lyr20230909")
            Log.d("存在", tableName);
        }else{
            Log.d("不存在，创建新表：", tableName);
            onCreate(sqLiteDatabase);
        }
        int update = sqLiteDatabase.update(TABLE_NAME, cv, COLUMN_ID + "=?", new String[]{String.valueOf(labelModel.getId())});
        if (update == 0){
            return "fail";
        }
        sqLiteDatabase.close();
        return "success";

    }

    public List<LabelModel> getAll(String tableName){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        List<LabelModel> list = new ArrayList<>();
        if (HaveData(sqLiteDatabase,tableName)){ //判断表是否存在，如果不存在就调用oncreat新建一个表  ||tableName.equals("lyr20230909")
            Log.d("存在", tableName);
        }else{
            Log.d("不存在：", tableName);
            return list;
        }

        TABLE_NAME = tableName;
        String sql = "SELECT * FROM " + TABLE_NAME;

        Cursor cursor = sqLiteDatabase.rawQuery(sql,null);
        int idIndex = cursor.getColumnIndex(COLUMN_ID);
        int startTimeIndex = cursor.getColumnIndex(COLUMN_STARTTIME);
        int endTimeIndex = cursor.getColumnIndex(COLUMN_ENDTIME);
        int actionIndex = cursor.getColumnIndex(COLUMN_ACTION);
        int positionIndex = cursor.getColumnIndex(COLUMN_POSITION);
        int audioIndex = cursor.getColumnIndex(COLUMN_AUDIO);

        if (cursor.moveToFirst()){
            do {
                LabelModel labelModel = new LabelModel(cursor.getInt(idIndex)
                        ,cursor.getString(startTimeIndex)
                        ,cursor.getString(endTimeIndex)
                        ,cursor.getString(actionIndex)
                        ,cursor.getString(positionIndex)
                        ,cursor.getString(audioIndex));
                list.add(labelModel);
            }while (cursor.moveToNext());
        }
        sqLiteDatabase.close();
        return list;
    }

    public static boolean HaveData(SQLiteDatabase db,String tablename){
        Cursor cursor;
        boolean a = false;
        cursor = db.rawQuery("select name from sqlite_master where type='table' ",null);
        while (cursor.moveToNext()){
            String name = cursor.getString(0);
            if (name.equals(tablename)){
                a = true;
            }
            Log.d("tablename:",name);
        }
        if (a){
            cursor = db.query(tablename,null,null,null,null,null,null);
            return cursor.getCount()>0;
        }else {
            return false;
        }
    }

    public void dropTable(String tablename){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("drop table "+tablename);
    }

}
