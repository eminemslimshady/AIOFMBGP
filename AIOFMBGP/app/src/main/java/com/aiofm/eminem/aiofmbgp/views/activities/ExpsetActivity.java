package com.aiofm.eminem.aiofmbgp.views.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.aiofm.eminem.aiofmbgp.R;
import com.aiofm.eminem.aiofmbgp.common.Utils;
import com.aiofm.eminem.aiofmbgp.views.fragments.DevsetFragment;

import java.util.ArrayList;
import java.util.List;

public class ExpsetActivity extends Activity implements
        AdapterView.OnItemSelectedListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private CheckBox chk_SaveExp;
    private Button btn_Set;
    private EditText edit_Cjzjg, edit_Cjzs, edit_Ljzjg, edit_Ljzs, edit_Bgsj, edit_Hdbhxs;
    private Spinner spinner_Cjzjg, spinner_Ljzjg, spinner_Bgsj;
    private String strDevName;
    private String strDevType;
    private String strDeviceIP;
    private int nDevicePort;
    private String strEditCjzjg="0",strEditCjzs="0",strEditLjzjg="0",strEditLjzs="0",strEditBgsj="0",strEditHdbhxs="0";
    private int devId;
    private int nCjzs=0,nCjzjgunit=0,nLjzs=0,nLjzjgunit=0,nBgsjunit=0;
    private double dStdCjzjg=0.0,dStdLjzjg=0.0,dStdBgsj=0.0;
    private double dCjzjg=0.0,dLjzjg=0.0,dBgsj=0.0,dHdbhxs=0.0;
    private boolean bSaveConInfo;
    private boolean bSaveExp = true;
    public static boolean gbSaveExp=true;
    public SharedPreferences expSharedPreferences;
    private Bundle devBundle=new Bundle();

    public static double gdCjzjg=0.0,gdLjzjg=0.0,gdBgsj=0.0,gdHdbhxs=0.0;
    public static int gnCjzs=0,gnLjzs=0,gnCjzjgunit=0,gnLjzjgunit=0,gnBgsjunit=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expset);
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
        //绑定控件ID
        edit_Cjzjg = (EditText) findViewById(R.id.edit_cjzjg);
        edit_Cjzs = (EditText) findViewById(R.id.edit_cjzs);
        edit_Ljzjg = (EditText) findViewById(R.id.edit_ljzjg);
        edit_Ljzs = (EditText) findViewById(R.id.edit_ljzs);
        edit_Bgsj = (EditText) findViewById(R.id.edit_bgsj);
        edit_Hdbhxs = (EditText) findViewById(R.id.edit_hdbhxs);
        spinner_Cjzjg = (Spinner) findViewById(R.id.spinner_cjzjg);
        spinner_Ljzjg = (Spinner) findViewById(R.id.spinner_ljzjg);
        spinner_Bgsj = (Spinner) findViewById(R.id.spinner_bgsj);
        btn_Set = (Button) findViewById(R.id.btn_set);
        chk_SaveExp = (CheckBox) findViewById(R.id.chk_saveExp);
    }

    protected void restoreForFirstLauch() {
        //第一次打开Activity
        Intent intent=getIntent();
        devBundle=intent.getExtras();
        if(devBundle==null){
            return;
        }
        devId=devBundle.getInt("deviceId");
        strDevName=devBundle.getString("deviceName");
        strDevType=devBundle.getString("deviceType");
        strDeviceIP=devBundle.getString("deviceIP");
        nDevicePort=devBundle.getInt("devicePort");
        dCjzjg=devBundle.getDouble("cjzjg", 0.0);
        strEditCjzjg=String.valueOf(dCjzjg);
        nCjzjgunit=devBundle.getInt("cjzjgunit", 0);
        nCjzs=devBundle.getInt("cjzs", 0);
        strEditCjzs=String.valueOf(nCjzs);
        dLjzjg=devBundle.getDouble("ljzjg", 0.0);
        strEditLjzjg=String.valueOf(dLjzjg);
        nLjzjgunit=devBundle.getInt("ljzjgunit", 0);
        nLjzs=devBundle.getInt("ljzs", 0);
        strEditLjzs=String.valueOf(nLjzs);
        dBgsj=devBundle.getDouble("bgsj", 0.0);
        strEditBgsj=String.valueOf(dBgsj);
        nBgsjunit=devBundle.getInt("bgsjunit", 0);
        dHdbhxs=devBundle.getDouble("hdbhxs", 0.0);
        strEditHdbhxs=String.valueOf(dHdbhxs);
        bSaveConInfo=devBundle.getBoolean("saveConInfo");
        bSaveExp=devBundle.getBoolean("saveExp");
        gbSaveExp=bSaveExp;
    }

    protected void initData(Bundle bundle) {
        //非第一次打开Activity
        strDeviceIP=bundle.getString("deviceIP");
        nDevicePort=bundle.getInt("devicePort");
        nCjzjgunit=bundle.getInt("cjzjgunit");
        nLjzjgunit=bundle.getInt("ljzjgunit");
        nBgsjunit=bundle.getInt("bgsjunit");
        strEditCjzjg=bundle.getString("editcjzjg");
        strEditCjzs=bundle.getString("editcjzs");
        strEditLjzjg=bundle.getString("editljzjg");
        strEditLjzs=bundle.getString("editljzs");
        strEditBgsj=bundle.getString("editbgsj");
        strEditHdbhxs=bundle.getString("edithdbhxs");
        bSaveConInfo=bundle.getBoolean("saveConInfo");
        bSaveExp=bundle.getBoolean("chkSaveExp");
        gbSaveExp=bSaveExp;
    }

    protected void initView() {
        //初始化Spinner
        spinner_Cjzjg.setSelection(nCjzjgunit,true);
        spinner_Ljzjg.setSelection(nLjzjgunit,true);
        spinner_Bgsj.setSelection(nBgsjunit,true);
        //初始化EditText
        edit_Cjzjg.setText(strEditCjzjg);
        edit_Cjzs.setText(strEditCjzs);
        edit_Ljzjg.setText(strEditLjzjg);
        edit_Ljzs.setText(strEditLjzs);
        edit_Bgsj.setText(strEditBgsj);
        edit_Hdbhxs.setText(strEditHdbhxs);
        chk_SaveExp.setChecked(bSaveExp);
    }

    protected void setOnListener() {
        //设置监听器
        spinner_Cjzjg.setOnItemSelectedListener(this);
        spinner_Ljzjg.setOnItemSelectedListener(this);
        spinner_Bgsj.setOnItemSelectedListener(this);
        chk_SaveExp.setOnCheckedChangeListener(this);
        btn_Set.setOnClickListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch ((int) id) {
            case R.id.spinner_cjzjg:
                nCjzjgunit=position;
                break;
            case R.id.spinner_ljzjg:
                nLjzjgunit=position;
                break;
            case R.id.spinner_bgsj:
                nBgsjunit=position;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    //求num的n次幂
    private int pow(int num, int n) {
        int m = num;
        for (int i = 1; i < n; i++) {
            num = num * m;
        }
        return num;
    }

    @Override
    public void onClick(View v) {
        //设置相应的曝光参数
        strEditCjzjg=edit_Cjzjg.getText().toString();
        strEditLjzjg=edit_Ljzjg.getText().toString();
        strEditBgsj=edit_Bgsj.getText().toString();
        strEditCjzs=edit_Cjzs.getText().toString();
        strEditLjzs=edit_Ljzs.getText().toString();
        strEditHdbhxs=edit_Hdbhxs.getText().toString();
        if(bSaveExp==true){
            ConnectActivity.utils.opendb(this);
            ContentValues values=new ContentValues();
            values.put("cjzjg",strEditCjzjg);
            values.put("cjzjgunit",nCjzjgunit);
            values.put("cjzs",strEditCjzs);
            values.put("ljzjg",strEditLjzjg);
            values.put("ljzjgunit",nLjzjgunit);
            values.put("ljzs",strEditLjzs);
            values.put("bgsj",strEditBgsj);
            values.put("bgsjunit",nBgsjunit);
            values.put("hdbhxs",strEditHdbhxs);
            values.put("bcbgsz",bSaveExp);
            if(bSaveConInfo==true){
                try {
                    String[] where = new String[1];
                    where[0] = "id";
                    String[] whereArgus = new String[1];
                    whereArgus[0] = Integer.toString(devId);
                    ConnectActivity.utils.update("ConInfo", where, whereArgus, values);
                    Utils.showShortToast(this.getApplicationContext(), "保存连接设备曝光信息成功");
                }catch(Exception e){
                    Utils.showShortToast(this.getApplicationContext(), "保存连接设备曝光信息失败");
                }finally {
                    ConnectActivity.utils.closedb();
                }
            }else {
                bSaveConInfo=true;
                values.put("deviceName",strDevName);
                values.put("deviceType",strDevType);
                values.put("IP",strDeviceIP);
                values.put("Port",nDevicePort);
                Boolean bInsertSuccess=false;
                try {
                    ConnectActivity.utils.insert("ConInfo", null, values);
                    bInsertSuccess=true;
                    Utils.showShortToast(this.getApplicationContext(), "保存连接设备曝光信息成功");
                } catch (Exception e) {
                    Log.e("ExpsetActivity", e.toString());
                    Utils.showShortToast(this.getApplicationContext(), "保存连接设备曝光信息信息失败");
                    ConnectActivity.utils.closedb();
                }
                if(bInsertSuccess){
                    Cursor cursor=null;
                    try {
                        cursor=ConnectActivity.utils.getMaxIdTurple("ConInfo", "id");
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
                values.put("bcbgsz",bSaveExp);
                try {
                    String[] where = new String[1];
                    where[0] = "id";
                    String[] whereArgus = new String[1];
                    whereArgus[0] = Integer.toString(devId);
                    ConnectActivity.utils.update("ConInfo", where, whereArgus, values);
                }catch(Exception e){
                    Utils.showShortToast(this.getApplicationContext(), "保存保存曝光标志位到数据库失败");
                }finally {
                    ConnectActivity.utils.closedb();
                }
            }
        }
        DevsetFragment.gnDevId=devId;
        gdCjzjg=Double.valueOf(strEditCjzjg).doubleValue();
        gdLjzjg=Double.valueOf(strEditLjzjg).doubleValue();
        gdBgsj=Double.valueOf(strEditBgsj).doubleValue();
        gnCjzs=Integer.valueOf(strEditCjzs).intValue();
        gnLjzs=Integer.valueOf(strEditLjzs).intValue();
        gdHdbhxs=Double.valueOf(strEditHdbhxs).doubleValue();
        gnCjzjgunit=nCjzjgunit;
        gnLjzjgunit=nLjzjgunit;
        gnBgsjunit=nBgsjunit;
        gbSaveExp=bSaveExp;
        DevsetFragment.gbSaveConInfo=bSaveConInfo;
        DevsetFragment.gbExpSetDone=true;
        Utils.finish(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (chk_SaveExp.isChecked()) {
            bSaveExp=true;
            Utils.showShortToast(this.getApplicationContext(), "保存设置");
        } else {
            bSaveExp=false;
            Utils.showShortToast(this.getApplicationContext(), "不保存设置");
        }
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

    /**
     * 非主动退出调用
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
        saveStateToArguments(outState);
    }

    private void saveStateToArguments(Bundle bundle){
        bundle.putString("deviceIP", strDeviceIP);
        bundle.putInt("devicePort", nDevicePort);
        bundle.putInt("cjzjgunit", nCjzjgunit);
        bundle.putInt("ljzjgunit", nLjzjgunit);
        bundle.putInt("bgsjunit", nBgsjunit);
        bundle.putString("editcjzjg", edit_Cjzjg.getText().toString());
        bundle.putString("editcjzs", edit_Cjzs.getText().toString());
        bundle.putString("editljzjg", edit_Ljzjg.getText().toString());
        bundle.putString("editljzs", edit_Ljzs.getText().toString());
        bundle.putString("editbgsj", edit_Bgsj.getText().toString());
        bundle.putString("edithdbhxs",edit_Hdbhxs.getText().toString());
        bundle.putBoolean("saveConInfo", bSaveConInfo);
        bundle.putBoolean("chkSaveExp", bSaveExp);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectActivity.utils.closedb();
    }
}
