/**
 * 连接Activity
 * Created by Bao guangpu  on 2016/1/20.
 */
package com.aiofm.eminem.aiofmbgp.views.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.aiofm.eminem.aiofmbgp.R;
import com.aiofm.eminem.aiofmbgp.common.FileUtil;
import com.aiofm.eminem.aiofmbgp.common.Utils;
import com.aiofm.eminem.aiofmbgp.views.controls.CircleImage;
import com.aiofm.eminem.aiofmbgp.views.controls.SelectPicPopupWindow;
import com.aiofm.eminem.aiofmbgp.views.fragments.DatavisualFragment;
import com.aiofm.eminem.aiofmbgp.services.ConnectService;
import com.aiofm.eminem.aiofmbgp.views.fragments.ConnectFragment;
import com.aiofm.eminem.aiofmbgp.views.fragments.DevsetFragment;
import com.aiofm.eminem.aiofmbgp.views.fragments.OffLineDataVisualFragment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

//实现监听器接口
public class ConnectActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{
    public static int runMode=0;
    private final int TIME_INTERVAL=2000;
    private int nCurrentDevId;
    private long mBackPressed=0;
    private boolean mbExitApp=false;
    private static final int REQUESTCODE_PICK = 0;
    private static final int REQUESTCODE_TAKE = 1;
    private static final int REQUESTCODE_CUTTING = 2;
    private static final String IMAGE_FILE_NAME = "avatarImage.jpg";
    private String urlpath;

    private Context mContext;
    private CircleImage avatarImg;
    private SelectPicPopupWindow menuWindow;
    private FragmentManager fragmentManager;
    private ConnectService connectService;
    private MyReceiver receiver = null;
    public SharedPreferences mySharedPreferences;
    public static Utils utils=Utils.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.putParcelable("android:support:fragments", null);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //设置自定义toolbar，不使用自带actionbar
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "关于", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent intent = new Intent(ConnectActivity.this, QaActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ConnectActivity.this).toBundle());
                else
                    startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_connectview);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        avatarImg = (CircleImage) headerView.findViewById(R.id.imageView);
        initControl();
        if (savedInstanceState == null) {
            restoreForFirstLauch();
        } else {
            initData(savedInstanceState);
        }
        initView();
        setOnListener();
        //注册广播接收器
        regReceiver("com.aiofm.eminem.aiofmbgp.services.ConnectService");
    }

    @Override
    protected void initControl() {
    }

    @Override
    protected void restoreForFirstLauch() {
        mySharedPreferences= getSharedPreferences("connectsoft", Activity.MODE_PRIVATE);
        urlpath=mySharedPreferences.getString("filepath", "");
    }

    @Override
    protected void initData(Bundle bundle) {
    }

    @Override
    protected void initView() {
        Fragment fragment = null;
        mContext = ConnectActivity.this;
        Class fragmentClass=ConnectFragment.class;
        try {
            fragment = (Fragment)fragmentClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        fragmentManager = getFragmentManager();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //第一个参数为要替换的fragment的父容器的ID，第二个参数为新的Fragment
        fragmentTransaction.replace(R.id.content_connect_frame, fragment);
        //将FragmentTransaction添加到back栈中，这样当用户按Back按键之后会返回到前一个布局
        fragmentTransaction.addToBackStack("FRAGMENT");
        fragmentTransaction.commit();
        if(urlpath.equals("")){
            return;
        }
        Bitmap bitmap= BitmapFactory.decodeFile(urlpath);
        avatarImg.setImageBitmap(bitmap);
    }

    @Override
    protected void setOnListener() {
        avatarImg.setOnClickListener(this);
    }

    /**
     * 动态注册广播监听器
     * @param action 广播Intent action
     * Added by Bao guangpu on 2016/1/22
     */
    private void regReceiver(String action) {
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        //过滤ConnectService发送过来的广播
        filter.addAction(action);
        this.registerReceiver(receiver, filter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if((System.currentTimeMillis()-mBackPressed)>TIME_INTERVAL){
                mBackPressed=System.currentTimeMillis();
                Utils.showShortToast(getApplicationContext(),"再按一次退出");
            }else{
                //如果连续两次back按钮单击事件时间小于2s
                mbExitApp=true;
                Utils.finish(this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.connect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent=new Intent(ConnectActivity.this,QaActivity.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ConnectActivity.this).toBundle());
            else
                startActivity(intent);
            return true;
        }else if(id == R.id.action_testMode){
            runMode=1;
        }

        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener itemsOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                //打开相机
                case R.id.takePhotoBtn:
                    Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takeIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME)));
                    startActivityForResult(takeIntent, REQUESTCODE_TAKE);
                    break;
                //打开相册
                case R.id.pickPhotoBtn:
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                    pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(pickIntent, REQUESTCODE_PICK);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUESTCODE_PICK:
                try {
                    //从相册选择照片后截取图像
                    startPhotoZoom(data.getData());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            case REQUESTCODE_TAKE:
                //拍完照后截取图像
                File temp = new File(Environment.getExternalStorageDirectory() + "/" + IMAGE_FILE_NAME);
                startPhotoZoom(Uri.fromFile(temp));
                break;
            case REQUESTCODE_CUTTING:
                if (data != null) {
                    //截图成功，设置头像
                    setPicToView(data);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startPhotoZoom(Uri uri) {
        //截图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        //设置裁剪
        intent.putExtra("crop", "true");
        //设置宽高比
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //裁剪图片具体宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }


    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(null, photo);
            urlpath = FileUtil.saveFile(mContext, "temphead.jpg", photo);
            avatarImg.setImageDrawable(drawable);
            mySharedPreferences= getSharedPreferences("connectsoft", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor=mySharedPreferences.edit();
            editor.putString("filepath",urlpath);
            editor.commit();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    //实现导航菜单监听器接口中相应的回调方法
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Class fragmentClass;
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.connect_connect:
                fragmentClass = ConnectFragment.class;
                jumpToOtherFragment(fragmentClass);
                break;
            case R.id.connect_lxsjksh:
                //fragmentClass = OffLineDataVisualFragment.class;
                Intent intent=new Intent();
                Utils.start_Activity(this,QaActivity.class,intent);
                break;
            default:
                fragmentClass = ConnectFragment.class;
                jumpToOtherFragment(fragmentClass);
                break;
        }

        //根据用户当前所在的导航页面设置视图的标题
        setTitle(item.getTitle());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void jumpToOtherFragment(Class fragmentCls){
        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentCls.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        fragmentManager = getFragmentManager();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //第一个参数为要替换的fragment的父容器的ID，第二个参数为新的Fragment
        fragmentTransaction.replace(R.id.content_connect_frame, fragment);
        //将FragmentTransaction添加到back栈中，这样当用户按Back按键之后会返回到前一个布局
        fragmentTransaction.addToBackStack("FRAGMENT");
        fragmentTransaction.commit();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭广播接收器
        unregisterReceiver(receiver);
        //关闭数据库相关
        this.utils.closedb();
        Log.d("ConnectFragment", "ConnectActivity destroy success");
        if(mbExitApp){
            DevsetFragment.mConAndorInfo.clear();
            DevsetFragment.mConMvcInfo.clear();
            DevsetFragment.mAndorOnCap.clear();
            DevsetFragment.mMvcOnCap.clear();
            ConnectFragment.mDeviceInfo.clear();
            ConnectFragment.mbFirstLaunch=true;
            ConnectActivity.runMode=0;
            ConnectFragment.deviceNum = 0;
            ConnectFragment.maxDeviceId = 0;
            ConnectFragment.mClickedItem = 0;
            ConnectFragment.mSelectedItem =0;
            ConnectFragment.sliderAdapter = null;
            DevsetFragment.nLastAndorPos = 0;
            DevsetFragment.nLastMvcPos=0;
            DevsetFragment.nAndorSetUIOpenTime=0;
            DevsetFragment.nMvcSetUIOpenTime=0;
            Intent intent=new Intent(getApplicationContext(),ConnectService.class);
            stopService(intent);
            //获取应用程序管理器
            ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
            manager.killBackgroundProcesses(getPackageName());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView:
                menuWindow = new SelectPicPopupWindow(mContext, itemsOnClick);
                menuWindow.showAtLocation(findViewById(R.id.drawer_layout),
                        Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
                break;
        }
    }

    /**
     * 监听器类
     * Added by Bao guangpu on 2016/1/22
     */
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Log.d("ConnectActivity",Integer.toString(bundle.size()));
            if(bundle.size()>0){
                //跳转操作
                Intent skipIntent = new Intent(ConnectActivity.this, MainActivity.class);
                bundle.putString("filepath",urlpath);
                skipIntent.putExtras(bundle);
                startActivity(skipIntent);
                ConnectActivity.this.finish();
            }else{
                Utils.showShortToast(getApplicationContext(),"连接失败");
            }
        }
    }
}
