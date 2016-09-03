/**
 * 连接服务
 * Created by Bao guangpu  on 2016/1/21.
 */
package com.aiofm.eminem.aiofmbgp.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.aiofm.eminem.aiofmbgp.common.Utils;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConnectService extends Service {
    private static final String TAG="CONNECT_SERVICE";

    private int nConDevNum;
    private int nDeviceId;
    private String mStrIP;
    private int nPort;
    private boolean isConnect=false;
    private Thread mConnThread;
    //已连接设备信息列表
    private List<ConDevInfo> mConDevSet=new ArrayList();
    private Handler mHandler;
    public static HashMap hashMap=new HashMap();

    public ConnectService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        nConDevNum=0;
        mHandler=new Handler();
        if(intent==null){
            return -1;
        }
        Bundle bundle=intent.getExtras();
        if(bundle==null){
            return -1;
        }
        //遍历bundle
        for(String key:bundle.keySet()){
            ConDevInfo devItem=new ConDevInfo();
            Bundle item=bundle.getBundle(key);
            devItem.deviceId=item.getInt("deviceId");
            devItem.deviceIP=item.getString("IP");
            devItem.devicePort=item.getInt("Port");
            devItem.strDeviceName=item.getString("deviceName");
            devItem.strDeviceType=item.getString("deviceType");
            devItem.saveConInfo=item.getBoolean("saveConInfo");
            devItem.conSuccess=false;
            ++nConDevNum;
            mConDevSet.add(devItem);
        }
        if(nConDevNum==0){
            //获取主UI线程Handler
            mHandler=new Handler(Looper.getMainLooper());
            mHandler.post(new Runnable(){
                public void run(){
                    Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_LONG).show();
                }
            });
            //关闭service
            this.stopSelf();
        }
        mConnThread=new Thread(mConnRunnable);
        mConnThread.start();
        return super.onStartCommand(intent, Service.START_NOT_STICKY, startId);
    }

    /**
     * 连接子线程Runnable
     * Added by Bao guangpu on 2016/1/22
     */
    private Runnable mConnRunnable=new Runnable() {
        @Override
        public void run() {
            if(!isConnect){
                initSocket();
            }
        }
    };

    /**
     * 初始化套接字
     * Added by Bao guangpu on 2016/1/22
     */
    private void initSocket(){
        //发送广播
        Intent intent=new Intent();
        Log.d(TAG,Integer.toString(mConDevSet.size()));
        for(int i=0;i<nConDevNum;i++){
            ConDevInfo item=mConDevSet.get(i);
            mStrIP=item.deviceIP;
            nPort=item.devicePort;
            if(mStrIP.length()<=0 || nPort==0){
                return;
            }
            try{
                //Socket(String IP,int Port)包含了connect操作
                //Socket socket=new Socket(mStrIP,nPort);
                //socket!=null
                if(true){
                    //如果连接上server，重置设备连接标志位，并将连接成功设备信息添加到意图中
                    item.conSuccess=true;
                    mConDevSet.set(i, item);
                    Bundle bundle=new Bundle();
                    bundle.putInt("deviceId", item.deviceId);
                    bundle.putString("deviceName", item.strDeviceName);
                    bundle.putString("deviceType", item.strDeviceType);
                    bundle.putString("IP", item.deviceIP);
                    bundle.putInt("Port", item.devicePort);
                    bundle.putBoolean("saveConInfo", item.saveConInfo);
                    intent.putExtra(Integer.toString(i), bundle);
                    //将成功连接socket存入hashMap
                    //hashMap.put(Integer.toString(item.deviceId),socket);
                    isConnect = true;
                }else{
                    Log.d(TAG,item.deviceId+"设备连接失败");
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        if(isConnect){
            intent.setAction("com.aiofm.eminem.aiofmbgp.services.ConnectService");
            Log.d(TAG,"发送广播");
            //连接成功则发送广播
            sendBroadcast(intent);
        }else{
            //获取主UI线程Handler
            mHandler=new Handler(Looper.getMainLooper());
            mHandler.post(new Runnable(){
                public void run(){
                    Toast.makeText(getApplicationContext(), "所有设备连接失败", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isConnect=false;
    }

    /**
     * 已连接设备信息类
     */
    public class ConDevInfo{
        public int deviceId;
        public String strDeviceName;
        public String strDeviceType;
        public String deviceIP;
        public int devicePort;
        public boolean saveConInfo;
        public boolean conSuccess;
    }
}
