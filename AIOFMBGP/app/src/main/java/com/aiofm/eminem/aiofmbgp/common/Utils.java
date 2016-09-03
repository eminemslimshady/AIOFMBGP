/**
 * 工具类
 * Created by Bao guangpu  on 2016/1/21.
 */

package com.aiofm.eminem.aiofmbgp.common;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.message.BasicNameValuePair;

import com.aiofm.eminem.aiofmbgp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Bao guangpu on 2016/1/21.
 */
public class Utils {
    //设备连接信息数据库
    private static final String DB_NAME="ConnectInfo.db";
    public static final String TAG = "Utils";
    //数据库版本
    private static final int DB_VERSION=1;
    //打开数据库时返回的数据库对象
    private SQLiteDatabase mSQLiteDatabase=null;
    private DatabaseHelper mDatabaseHelper=null;
    private static Utils dbConn=null;
    // 查询游标对象
    private Cursor cursor;

    public static void showLongToast(Context context,String pMsg) {
        Toast.makeText(context, pMsg,Toast.LENGTH_LONG).show();
    }

    public static void showShortToast(Context context,String pMsg) {
        Toast.makeText(context,pMsg,Toast.LENGTH_SHORT).show();
    }

    /**
     * 打开Activity
     *
     * Added by Bao guangpu on 2016/1/21.
     * @param activity
     * @param cls
     */
    public static void start_Activity(Activity activity,Class<?> cls,Map<String,String> name) {
        Intent intent=new Intent();
        intent.setClass(activity, cls);
        if(name!=null) {
            //传值到Intent中
            for(Map.Entry<String,String> m:name.entrySet())
            {
                intent.putExtra(m.getKey(),m.getValue());
            }
            //打开新的Activity
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
        }
    }

    /**
     * Added by Bao guangpu on 2016/2/10.
     * @param activity
     * @param cls
     * @param intent
     */
    public static  void start_Activity(Activity activity,Class<?> cls,Intent intent){
        if(intent!=null){
            intent.setClass(activity, cls);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
        }
    }

    /**
     * 关闭Activity
     * Added by Bao guangpu on 2016/1/21.
     * @param activity
     */
    public static void finish(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    /**
     * 检测网络连接状态
     *
     * Added by Bao guangpu on 2016/1/21.
     * @param context
     */
    public static boolean checkNetworkStatus(Context context)
    {
        if(context.checkCallingOrSelfPermission(Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
            Log.w("Utility","haven't internet permission");
            return false;
        }
        //创建ConnectivityManager对象，用于管理网络连接
        ConnectivityManager connectivityManage=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManage==null) {
            Log.w("Utility", "couldn't get connectivity manager");
            return false;
        }else {
            //获取手机当前的的wifi状态
            NetworkInfo wifiState = connectivityManage.getActiveNetworkInfo();
            if (wifiState != null && wifiState.getType()==ConnectivityManager.TYPE_WIFI) {
                Log.w("Utility","wifi is available");
                return true;
            } else {
                Log.w("Utility","wifi is unavailable");
                return false;
            }
        }
    }

    /**
     * SQLiteOpenHelper内部类
     * Added by Bao guangpu on 2016/2/14
     */
    private static class DatabaseHelper extends SQLiteOpenHelper{
        DatabaseHelper(Context context){
            super(context,DB_NAME,null,DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE ConInfo(id INTEGER PRIMARY KEY NOT NULL," +
                    "device_name TEXT NOT NULL," +
                    "device_type TEXT NOT NULL," +
                    "IP TEXT NOT NULL," +
                    "Port INTEGER NOT NULL," +
                    "cjzjg REAL DEFAULT 0," +
                    "cjzjgunit INTEGER DEFAULT 0," +
                    "cjzs INTEGER DEFAULT 0," +
                    "ljzjg REAL DEFAULT 0," +
                    "ljzjgunit INTEGER DEFAULT 0," +
                    "ljzs INTEGER DEFAULT 0," +
                    "bgsj REAL DEFAULT 0," +
                    "bgsjunit INTEGER DEFAULT 0," +
                    "hdbhxs REAL DEFAULT 0," +
                    "qshzb INTEGER DEFAULT 0," +
                    "qszzb INTEGER DEFAULT 0," +
                    "txkd INTEGER DEFAULT 0," +
                    "txgd INTEGER DEFAULT 0," +
                    "bcbgsz INTEGER DEFAULT 0," +
                    "bctxsz INTEGER DEFAULT 0);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS ConInfo;");
            onCreate(db);
        }
    }

    /**
     * 工具类私有构造函数
     * Added by Bao guangpu on 2016/2/14
     */
    private Utils(){
        super();
    }

    /**
     * 实现单例构造工具类实例
     * Added by Bao guangpu on 2016/2/14
     * @return
     */
    public static Utils getInstance(){
        if(null==dbConn){
            dbConn=new Utils();
        }
        return dbConn;
    }

    /**
     * 打开数据库
     * Added by Bao guangpu on 2016/2/14
     * @param mContext
     */
    public void opendb(Context mContext){
        mDatabaseHelper=new DatabaseHelper(mContext);
        mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
    }

    /**
     * 关闭数据库
     * Added by Bao guangpu on 2016/2/14
     */
    public void closedb() {
        if (null != mDatabaseHelper) {
            mDatabaseHelper.close();
        }
        if (null != cursor) {
            cursor.close();
        }
    }

    /**
     * 插入
     * @param tableName 表名
     * @param nullColumn 当插入内容为空时的列名
     * @param contentValues 插入内容，为字段名与字段值键值对的集合
     * @return 新插入数据的ID，错误返回-1
     * @throws Exception
     * Added by Bao guangpu on 2016/2/15
     */
    public long insert(String tableName,String nullColumn,ContentValues contentValues)
        throws Exception{
        try{
            return mSQLiteDatabase.insert(tableName, nullColumn, contentValues);
        }catch(Exception e){
            Log.e(TAG, "Error : ", e);
            throw e;
        }
    }

    /**
     * 删除
     * @param tableName 表名
     * @param key 主键名
     * @param keyValue 主键值
     * @return 受影响的记录数
     * @throws Exception
     * Added by Bao guangpu on 2016/2/15
     */
    public long delete(String tableName,String key,String keyValue)
        throws Exception{
        try{
            return mSQLiteDatabase.delete(tableName, key + "=" + keyValue, null);
        }catch(Exception e){
            Log.e(TAG, "Error : ", e);
            throw e;
        }
    }

    /**
     * 查询整张表的数据
     * @param tableName 表名
     * @return 包含查询结果集的游标
     * @throws Exception
     * Added by Bao guangpu on 2016/2/15
     */
    public Cursor findAll(String tableName)
        throws Exception{
        try{
            cursor=mSQLiteDatabase.query(tableName,null,null,null,null,null,null);
            return cursor;
        }catch(Exception e) {
            Log.e(TAG, "Error : ", e);
            throw e;
        }
    }

    /**
     * 根据主键值查找记录
     * @param tableName 表名
     * @param key 主键名
     * @param id 主键值
     * @param columns 如果返回所有列，则填null
     * @return 包含查询结果集的游标
     * @throws Exception
     * Added by Bao guangpu on 2016/2/15
     */
    public Cursor findById(String tableName,String key,int id,String[] columns)
        throws Exception{
        try{
            return mSQLiteDatabase.query(tableName, columns, key + "=" + id, null, null, null, null);
        }catch(Exception e){
            Log.e(TAG, "Error : ", e);
            throw e;
        }
    }

    /**
     * 使用max聚合函数
     * @param tableName
     * @param key
     * @return
     * @throws Exception
     */
    public Cursor getMaxIdTurple(String tableName,String key)
        throws Exception{
        try{
            Cursor cursor=mSQLiteDatabase.rawQuery("select * from " + tableName + " where id=(select max(" +key+") from " +tableName+")",null);
            cursor.moveToFirst();
            return cursor;
        }catch(Exception e){
            Log.e(TAG, "Error : ", e);
            throw e;
        }
    }

    /**
     * 按照指定的条件查询记录
     * @param tableName 表名
     * @param names 查询条件
     * @param values 查询条件值
     * @param columns 要查询的字段名，如果要查询所有列，则置为null
     * @param orderColumn 查询结果按照该列值进行排序
     * @param limit 限制返回数
     * @return 查询结果集游标
     * @throws Exception
     * Added by Bao guangpu on 2016/2/15
     */
    public Cursor find(String tableName,String[] names,String[] values, String[] columns,
                       String orderColumn,String limit) throws Exception{
        try{
            StringBuilder selection=new StringBuilder();
            for(int i=0;i<names.length;i++){
                selection.append(names[i]);
                selection.append("=?");
                if(i!=names.length-1){
                    selection.append(",");
                }
            }
            cursor=mSQLiteDatabase.query(true,tableName,columns,selection.toString(),values,
                    null,null,orderColumn,limit);
            cursor.moveToFirst();
            return cursor;
        }catch(Exception e){
            Log.e(TAG, "Error : ", e);
            throw e;
        }
    }

    /**
     * 更新
     * @param tableName 表名
     * @param names 查询条件
     * @param values 查询条件值
     * @param args 用来更新的键值对
     * @return 成功返回true，失败返回false
     * @throws Exception
     * 最终调用native方法nativeExecuteForChangedRowCount()，具体的数据库操作最终还是由C++实现
     * Added by Bao guangpu on 2016/2/15
     */
    public boolean update(String tableName,String[] names,String[] values,ContentValues args)
        throws Exception{
        try{
            StringBuilder selection=new StringBuilder();
            for(int i=0;i<names.length;i++){
                selection.append(names[i]);
                selection.append("=?");
                if(i!=names.length-1){
                    selection.append(",");
                }
            }
            return mSQLiteDatabase.update(tableName,args,selection.toString(),values)>0;
        }catch(Exception e){
            Log.e(TAG, "Error : ", e);
            throw e;
        }
    }

    /**
     * 执行sql语句
     * @param sql 要执行的Sql语句
     * Added by Bao guangpu on 2016/2/15
     */
    public void executeSql(String sql) throws Exception{
        try{
            mSQLiteDatabase.execSQL(sql);
        }catch(Exception e){
            Log.e(TAG,"Error : ",e);
            throw e;
        }
    }

    /**
     * 获取时间
     * @return
     */
    public static String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
    }
}
