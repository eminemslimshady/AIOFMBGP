package com.aiofm.eminem.aiofmbgp.views.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.aiofm.eminem.aiofmbgp.R;
import com.aiofm.eminem.aiofmbgp.common.Utils;
import com.aiofm.eminem.aiofmbgp.views.fragments.DevsetFragment;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class ImagesetActivity extends Activity implements
        View.OnClickListener,CompoundButton.OnCheckedChangeListener {
    private CheckBox chk_SaveImg;
    private Button btn_Set2;
    private EditText edit_Qshzb, edit_Qszzb, edit_Txkd, edit_Txgd;

    private String strEditQshzb="0",strEditQszzb="0",strEditTxkd="0",strEditTxgd="0";
    private boolean bSaveConInfo=false;
    private boolean bSaveImgSet=false;
    public static boolean gbSaveImgSet=false;
    private String strDevName;
    private String strDevType;
    private String strDeviceIP;
    private int nDevicePort;
    private int devId;
    private int nQshzb=0,nQszzb=0,nTxkd=0,nTxgd=0;
    private Bundle devBundle=new Bundle();

    public static int gnQshzb=0,gnQszzb=0,gnTxkd=0,gnTxgd=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageset);
        initControl();
        if(savedInstanceState==null){
            restoreForFirstLauch();
        }else{
            initData(savedInstanceState);
        }
        initView();
        setOnListener();
    }

    protected void initControl() {
        edit_Qshzb = (EditText) findViewById(R.id.edit_qshzb);
        edit_Qszzb = (EditText) findViewById(R.id.edit_qszzb);
        edit_Txkd = (EditText) findViewById(R.id.edit_txkd);
        edit_Txgd = (EditText) findViewById(R.id.edit_txgd);
        btn_Set2 = (Button) findViewById(R.id.btn_set2);
        chk_SaveImg=(CheckBox)findViewById(R.id.chk_saveImg);
    }

    protected void restoreForFirstLauch() {
        //第一次打开该Activity
        Intent intent=getIntent();
        devBundle=intent.getExtras();
        if(devBundle==null){
            return;
        }
        devId=devBundle.getInt("deviceId");
        strDevName = devBundle.getString("deviceName");
        strDevType=devBundle.getString("deviceType");
        strDeviceIP=devBundle.getString("deviceIP");
        nDevicePort=devBundle.getInt("devicePort");
        nQshzb=devBundle.getInt("qshzb");
        nQszzb=devBundle.getInt("qszzb");
        nTxkd=devBundle.getInt("txkd");
        nTxgd=devBundle.getInt("txgd");
        strEditQshzb=String.valueOf(nQshzb);
        strEditQszzb=String.valueOf(nQszzb);
        strEditTxkd=String.valueOf(nTxkd);
        strEditTxgd=String.valueOf(nTxgd);
        bSaveConInfo=devBundle.getBoolean("saveConInfo");
        bSaveImgSet=devBundle.getBoolean("saveImgSet");
        gbSaveImgSet = bSaveImgSet;
    }

    protected void initData(Bundle bundle) {
        //非第一次打开该Activity
        if(bundle==null){
            return;
        }
        strDeviceIP=bundle.getString("deviceIP");
        nDevicePort=bundle.getInt("devicePort");
        strEditQshzb=bundle.getString("editqshzb");
        strEditQszzb=bundle.getString("editqszzb");
        strEditTxkd=bundle.getString("edittxkd");
        strEditTxgd=bundle.getString("edittxgd");
        bSaveConInfo=bundle.getBoolean("saveConInfo");
        bSaveImgSet=bundle.getBoolean("chkSaveImg");
        gbSaveImgSet=bSaveImgSet;
    }

    protected void initView() {
        edit_Qshzb.setText(strEditQshzb);
        edit_Qszzb.setText(strEditQszzb);
        edit_Txkd.setText(strEditTxkd);
        edit_Txgd.setText(strEditTxgd);
        chk_SaveImg.setChecked(bSaveImgSet);
    }

    protected void setOnListener() {
        chk_SaveImg.setOnCheckedChangeListener(this);
        btn_Set2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //设置相应的采集图像选项
        strEditQshzb=edit_Qshzb.getText().toString();
        strEditQszzb=edit_Qszzb.getText().toString();
        strEditTxkd=edit_Txkd.getText().toString();
        strEditTxgd=edit_Txgd.getText().toString();
        if(bSaveImgSet==true){
            ConnectActivity.utils.opendb(this);
            ContentValues values=new ContentValues();
            values.put("qshzb",strEditQshzb);
            values.put("qszzb",strEditQszzb);
            values.put("txkd",strEditTxkd);
            values.put("txgd",strEditTxgd);
            values.put("bctxsz",bSaveImgSet);
            if(bSaveConInfo==true){
                try {
                    String[] where = new String[1];
                    where[0] = "id";
                    String[] whereArgus = new String[1];
                    whereArgus[0] = Integer.toString(devId);
                    ConnectActivity.utils.update("ConInfo", where, whereArgus, values);
                    Utils.showShortToast(this.getApplicationContext(), "保存连接设备图像设置信息成功");
                }catch(Exception e){
                    Utils.showShortToast(this.getApplicationContext(), "保存连接设备图像设置信息失败");
                }finally {
                    ConnectActivity.utils.closedb();
                }
            }else{
                bSaveConInfo=true;
                values.put("deviceName",strDevName);
                values.put("deviceType",strDevType);
                values.put("IP",strDeviceIP);
                values.put("Port",nDevicePort);
                Boolean bInsertSuccess=false;
                try {
                    ConnectActivity.utils.insert("ConInfo", null, values);
                    bInsertSuccess=true;
                    Utils.showShortToast(this.getApplicationContext(), "保存连接设备图像设置信息成功");
                } catch (Exception e) {
                    Log.e("ImgsetActivity", e.toString());
                    Utils.showShortToast(this.getApplicationContext(), "保存连接设备图像设置信息失败");
                    ConnectActivity.utils.closedb();
                }
                if(bInsertSuccess){
                    Cursor cursor=null;
                    try {
                        cursor=ConnectActivity.utils.getMaxIdTurple("ConInfo","id");
                        devId=cursor.getInt(0);
                    } catch(Exception e) {
                        Utils.showShortToast(this, "取最大Id失败");
                    }finally {
                        cursor.close();
                        ConnectActivity.utils.closedb();
                    }
                }
            }
        }else{
            if(bSaveConInfo==true){
                ContentValues values=new ContentValues();
                values.put("bctxsz",bSaveImgSet);
                try {
                    String[] where = new String[1];
                    where[0] = "id";
                    String[] whereArgus = new String[1];
                    whereArgus[0] = Integer.toString(devId);
                    ConnectActivity.utils.update("ConInfo", where, whereArgus, values);
                }catch(Exception e){
                    Utils.showShortToast(this.getApplicationContext(), "保存保存图像设置标志位到数据库失败");
                }finally {
                    ConnectActivity.utils.closedb();
                }
            }
        }
        DevsetFragment.gnDevId=devId;
        gnQshzb=Integer.parseInt(strEditQshzb);
        gnQszzb=Integer.parseInt(strEditQszzb);
        gnTxkd=Integer.parseInt(strEditTxkd);
        gnTxgd=Integer.parseInt(strEditTxgd);
        gbSaveImgSet=bSaveImgSet;
        DevsetFragment.gbSaveConInfo=bSaveConInfo;
        DevsetFragment.gbImgSetDone=true;
        Utils.finish(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Utils.finish(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
        saveStateToArguments(outState);
    }

    private void saveStateToArguments(Bundle bundle){
        bundle.putString("deviceIP",strDeviceIP);
        bundle.putInt("devicePort",nDevicePort);
        bundle.putString("editqshzb",edit_Qshzb.getText().toString());
        bundle.putString("editqszzb",edit_Qszzb.getText().toString());
        bundle.putString("edittxkd",edit_Txkd.getText().toString());
        bundle.putString("edittxgd",edit_Txgd.getText().toString());
        bundle.putBoolean("chkSaveImg",bSaveImgSet);
        bundle.putBoolean("saveConInfo", bSaveConInfo);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Imageset Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.aiofm.eminem.aiofmbgp.activities/http/host/path")
        );
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            bSaveImgSet=true;
            Utils.showShortToast(this.getApplicationContext(), "保存设置");
        }else{
            bSaveImgSet=false;
            Utils.showShortToast(this.getApplicationContext(),"不保存设置");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectActivity.utils.closedb();
    }
}
