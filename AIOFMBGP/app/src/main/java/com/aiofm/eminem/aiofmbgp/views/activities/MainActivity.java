/**
 * 主Activity
 * Created by Bao guangpu  on 2016/1/20.
 */
package com.aiofm.eminem.aiofmbgp.views.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
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
import android.os.PersistableBundle;
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

import com.aiofm.eminem.aiofmbgp.R;
import com.aiofm.eminem.aiofmbgp.common.FileUtil;
import com.aiofm.eminem.aiofmbgp.common.Utils;
import com.aiofm.eminem.aiofmbgp.services.ConnectService;
import com.aiofm.eminem.aiofmbgp.views.controls.CircleImage;
import com.aiofm.eminem.aiofmbgp.views.controls.SelectPicPopupWindow;
import com.aiofm.eminem.aiofmbgp.views.fragments.ConnectFragment;
import com.aiofm.eminem.aiofmbgp.views.fragments.DatavisualFragment;
import com.aiofm.eminem.aiofmbgp.views.fragments.DevsetFragment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends BaseActivity
    implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{
    private static Class lastOpenFragmentClass=DevsetFragment.class;
    private static final int TIME_INTERVAL2=2000;
    private long mBackPressed=0;
    private int nDeviceNum=0;
    private boolean mbExitApp=false;
    private String urlpath="";
    Bundle recvBundle=new Bundle();
    Bundle andorBundle=new Bundle();
    Bundle mvcBundle=new Bundle();

    private static final int REQUESTCODE_PICK = 0;
    private static final int REQUESTCODE_TAKE = 1;
    private static final int REQUESTCODE_CUTTING = 2;
    private static final String IMAGE_FILE_NAME = "avatarImage.jpg";

    private Context mContext;
    private CircleImage avatarImg;
    private SelectPicPopupWindow menuWindow;

    private SharedPreferences mySharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
            savedInstanceState.putParcelable("android:support:fragments", null);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,QaActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                else
                    startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_mainview);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        avatarImg = (CircleImage) headerView.findViewById(R.id.imageView);
        avatarImg.setOnClickListener(this);
        initControl();
        if(savedInstanceState==null){
            restoreForFirstLauch();
        }else{
            //initData(savedInstanceState);
        }
        initView();
        //设置监听器
        setOnListener();
}

    @Override
    protected void initControl() {
    }

    @Override
    protected void restoreForFirstLauch() {
        Intent intent=getIntent();
        recvBundle=intent.getExtras();
        urlpath=recvBundle.getString("filepath","");
        //对bundle按照设备型号进行分解，并存入新的Bundle中
        for(String key:recvBundle.keySet()){
            if(!key.equals("filepath")){
                Bundle item=recvBundle.getBundle(key);
                String strDevtype=item.getString("deviceType");
                if(strDevtype.equals("1型大气测量设备")){
                    andorBundle.putBundle(key,item);
                }else if(strDevtype.equals("2型大气测量设备")){
                    mvcBundle.putBundle(key,item);
                }
                ++nDeviceNum;
            }
        }
    }

    @Override
    protected void initData(Bundle bundle) {
    }

    @Override
    protected void initView() {
        mContext=this;
        if(urlpath.equals("")){
            return;
        }
        Bitmap bitmap=BitmapFactory.decodeFile(urlpath);
        avatarImg.setImageBitmap(bitmap);
    }

    @Override
    protected void setOnListener() {
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if((System.currentTimeMillis()-mBackPressed)>TIME_INTERVAL2){
                mBackPressed=System.currentTimeMillis();
                Utils.showShortToast(getApplicationContext(), "再按一次退出");
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
        getMenuInflater().inflate(R.menu.main, menu);
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
            Intent intent=new Intent(MainActivity.this,QaActivity.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
            else
                startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment=null;
        Class fragmentClass;
        boolean bDisconnect;
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch(id)
        {
            case R.id.andorset:
                fragmentClass= DevsetFragment.class;
                break;
            case R.id.mvcset:
                fragmentClass= DevsetFragment.class;
                break;
            case R.id.sbxh1ksh:
                fragmentClass= DatavisualFragment.class;
                break;
            case R.id.sbxh2ksh:
                fragmentClass= DatavisualFragment.class;
                break;
            default:
                fragmentClass=DevsetFragment.class;
                break;
        }

        try{
            lastOpenFragmentClass=fragmentClass;
            fragment = (Fragment) fragmentClass.newInstance();
            if(id==R.id.andorset || id==R.id.sbxh1ksh){
                andorBundle.putString("deviceType","1型大气测量设备");
                fragment.setArguments(andorBundle);
            }else if(id==R.id.mvcset || id==R.id.sbxh2ksh){
                mvcBundle.putString("deviceType","2型大气测量设备");
                fragment.setArguments(mvcBundle);
            }else{
                fragment.setArguments(andorBundle);
            }
        }
        catch(InstantiationException e)
        {
            e.printStackTrace();
        }
       catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }

        FragmentManager fragmentManager=getFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.content_main_frame,fragment);
        fragmentTransaction.addToBackStack("MAIN_FRAGMENT");
        fragmentTransaction.commit();

        //根据用户当前所在的导航页面设置视图的标题
        setTitle(item.getTitle());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public Boolean disconnect()
    {
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ConnectFragment","MainActivity destroy success");
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

            /*Method forceStopPackage = null;
            try {
                forceStopPackage = manager.getClass().getDeclaredMethod("forceStopPackage", String.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            forceStopPackage.setAccessible(true);
            try {
                forceStopPackage.invoke(manager, getPackageName());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }*/
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

    private View.OnClickListener itemsOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                case R.id.takePhotoBtn:
                    Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takeIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME)));
                    startActivityForResult(takeIntent, REQUESTCODE_TAKE);
                    break;
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
                    startPhotoZoom(data.getData());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            case REQUESTCODE_TAKE:
                File temp = new File(Environment.getExternalStorageDirectory() + "/" + IMAGE_FILE_NAME);
                startPhotoZoom(Uri.fromFile(temp));
                break;
            case REQUESTCODE_CUTTING:
                if (data != null) {
                    setPicToView(data);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
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

}
