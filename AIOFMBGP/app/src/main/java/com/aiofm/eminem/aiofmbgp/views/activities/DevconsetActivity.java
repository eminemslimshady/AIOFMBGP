package com.aiofm.eminem.aiofmbgp.views.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.aiofm.eminem.aiofmbgp.R;
import com.aiofm.eminem.aiofmbgp.common.Utils;

public class DevconsetActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    public static boolean gbSetDone = false;
    private CheckBox chk_Save;
    private Button btn_SetConInfo;
    private EditText edit_IP, edit_Port, edit_Devname;

    private boolean mbSave;
    private String operation;
    private String mStrIP;
    private String mStrPort;
    private String mStrDevname;
    private String mStrDevtype;
    public static Boolean bSave;
    public static int nDevId;
    public static String strIP;
    public static String strPort;
    public static String strDevname;
    public static String strDevtype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devconset);
        initControl();
        initData();
        initView();
        setOnListener();
    }

    protected void initControl() {
        chk_Save = (CheckBox) this.findViewById(R.id.chk_saveIPAndPort);
        btn_SetConInfo = (Button) this.findViewById(R.id.btn_setDevConInfo);
        edit_Devname = (EditText) this.findViewById(R.id.edit_devName);
        edit_IP = (EditText) this.findViewById(R.id.edit_IP);
        edit_Port = (EditText) this.findViewById(R.id.edit_Port);
    }

    protected void initData() {
        //第一次加载
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            operation = bundle.getString("operation");
            //判断Java字符串相等不能像C++一样用"==，而应该用equals，按照ASCII比较字符串大
            //小可以用compareToJava中"=="两边的比较的对象必须完全相同，包括在内存中的位置
            if (operation.equals("add")) {
                mStrDevtype = bundle.getString("device_type");
                mStrDevname = mStrDevtype;
                mStrIP = "127.0.0.1";
                mStrPort = "8080";
            } else if (operation.equals("edit")) {
                nDevId = bundle.getInt("id");
                mbSave = bundle.getBoolean("Check_Status", true);
                mStrDevname = bundle.getString("device_name");
                mStrDevtype = bundle.getString("device_type");
                mStrIP = bundle.getString("IP", "127.0.0.1");
                mStrPort = bundle.getString("Port", "8080");
            } else if (operation.equals("")) {
                Utils.showShortToast(this.getApplicationContext(), "添加或修改失败");
                Utils.finish(this);
            }
        } else {
        }
    }

    protected void initView() {
        chk_Save.setChecked(mbSave);
        edit_Devname.setText(mStrDevname);
        edit_IP.setText(mStrIP);
        edit_Port.setText(mStrPort);
    }

    protected void setOnListener() {
        chk_Save.setOnCheckedChangeListener(this);
        btn_SetConInfo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_setDevConInfo:
                setDevconinfo();
                break;
            default:
                break;
        }
    }

    /**
     * 设置连接信息
     * Added by Bao guangpu on 2016/2/10.
     */
    private void setDevconinfo() {
        mStrIP = edit_IP.getText().toString();
        mStrPort = edit_Port.getText().toString();
        //检查IP是否合法
        boolean mbIPCorrect = checkIPCorrect();
        mStrDevname = edit_Devname.getText().toString();
        if (mStrDevname.equals("")) {
            Utils.showShortToast(this.getApplicationContext(), "给设备去一个名称吧，它需要一个名字");
            return;
        }
        if (mbIPCorrect) {
            //如果输入的IP合法
            if (chk_Save.isChecked()) {
                //保存到sqlite
                mbSave = true;
                //打开数据库
                ConnectActivity.utils.opendb(this);
                //如果输入IP合法，则将连接设备信息存入sqlite中
                ContentValues values = new ContentValues();
                values.put("device_type", mStrDevtype);
                values.put("device_name", mStrDevname);
                values.put("IP", mStrIP);
                values.put("Port", mStrPort);
                if (operation.equals("add")) {
                    //添加设备连接信息
                    try {
                        ConnectActivity.utils.insert("ConInfo", null, values);
                    } catch (Exception e) {
                        Log.e("DevconsetActivity", e.toString());
                        Utils.showShortToast(this.getApplicationContext(), "保存连接信息失败");
                    } finally {
                        ConnectActivity.utils.closedb();
                    }
                } else if (operation.equals("edit")) {
                    try {
                        String[] where = new String[1];
                        where[0] = "id";
                        String[] whereArgus = new String[1];
                        whereArgus[0] = Integer.toString(nDevId);
                        ConnectActivity.utils.update("ConInfo", where, whereArgus, values);
                    } catch (Exception e) {
                        Utils.showShortToast(this.getApplicationContext(), "保存连接信息失败");
                    } finally {
                        ConnectActivity.utils.closedb();
                    }
                }
            } else {
                //不保存到sqlite
                mbSave = false;
            }
        } else {
            //如果输入IP非法向用户提示错误信息
            Utils.showShortToast(this.getApplicationContext(), "输入的IP地址非法，请重新输入");
            return;
        }
        //设备连接信息设置完毕，将相应标志位置为true
        gbSetDone = true;
        //将设备设置信息通过静态变量的形式在不同组件之间共享，用持久化方式时空开销太大
        strIP = mStrIP;
        strPort = mStrPort;
        strDevname = mStrDevname;
        strDevtype = mStrDevtype;
        bSave = mbSave;
        //完成连接信息设置后退出当前activity
        Utils.finish(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (chk_Save.isChecked()) {
            Utils.showShortToast(this.getApplicationContext(), "保存IP与端口");
        } else {
            Utils.showShortToast(this.getApplicationContext(), "取消保存");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //单击返回键退出本Activity
        Utils.finish(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectActivity.utils.closedb();
    }

    /**
     * 检查IP地址是否合法
     * Added by Bao guangpu on 2016/3/12
     */
    public boolean checkIPCorrect() {
        mStrIP = edit_IP.getText().toString();
        int segment = 0;
        int num = 0;
        int posTmp = 0;
        int pos = mStrIP.indexOf(".");
        if (pos >= 0) {
            num++;
        } else {
            return false;
        }
        try {
            segment = Integer.parseInt(mStrIP.substring(posTmp, pos));
        } catch (NumberFormatException e) {
            return false;
        }
        if (segment > 255 || segment < 0) {
            return false;
        }
        posTmp = pos + 1;
        pos = mStrIP.indexOf(".", posTmp);
        if (pos >= 0) {
            num++;
        } else {
            return false;
        }
        try {
            segment = Integer.parseInt(mStrIP.substring(posTmp, pos));
        } catch (NumberFormatException e) {
            return false;
        }
        if (segment > 255 || segment < 0) {
            return false;
        }
        posTmp = pos + 1;
        pos = mStrIP.indexOf(".", posTmp);
        if (pos >= 0) {
            num++;
        } else {
            return false;
        }
        try {
            segment = Integer.parseInt(mStrIP.substring(posTmp, pos));
        } catch (NumberFormatException e) {
            return false;
        }
        if (segment > 255 || segment < 0) {
            return false;
        }
        posTmp = pos + 1;
        pos = mStrIP.indexOf(".", posTmp);
        if(pos>=0){
            return false;
        }
        try{
            segment = Integer.parseInt(mStrIP.substring(posTmp));
        }catch(NumberFormatException e){
            return false;
        }
        if (segment > 255 || segment < 0) {
            return false;
        }
        return true;
    }
}
