/**
 * 设备设置Fragment
 * Created by Bao guangpu  on 2016/1/20.
 */
package com.aiofm.eminem.aiofmbgp.views.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.aiofm.eminem.aiofmbgp.R;
import com.aiofm.eminem.aiofmbgp.application.MyApplication;
import com.aiofm.eminem.aiofmbgp.common.Utils;
import com.aiofm.eminem.aiofmbgp.services.ConnectService;
import com.aiofm.eminem.aiofmbgp.views.activities.AboutdevsetActivity;
import com.aiofm.eminem.aiofmbgp.views.activities.ConnectActivity;
import com.aiofm.eminem.aiofmbgp.views.activities.ExpsetActivity;
import com.aiofm.eminem.aiofmbgp.views.activities.ImagesetActivity;
import com.aiofm.eminem.aiofmbgp.views.activities.MainActivity;
import com.aiofm.eminem.aiofmbgp.views.controls.SliderListView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DevsetFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DevsetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DevsetFragment extends Fragment implements AdapterView.OnItemSelectedListener,View.OnClickListener,CompoundButton.OnCheckedChangeListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private CheckBox chk_OpenCCD;
    private CheckBox chk_startCapture;
    private TextView tv_bgsz;
    private TextView tv_txsz;
    private TextView tv_aboutDev;
    private TextView tv_DevType;
    private TextView tv_DevRunInfo;
    private Spinner mySpinner;
    private View devset_Layout;
    private String strDevType;
    public static int nLastAndorPos = 0;
    public static int nLastMvcPos=0;
    public static int gnDevId=0;
    public int mDevNum=0;
    public int nControlDevId=0;
    public static int nAndorSetUIOpenTime=0;
    public static int nMvcSetUIOpenTime=0;

    private List<String> list = new ArrayList<String>();
    private List<Integer> devIdlist=new ArrayList<Integer>();
    public static List<ConDevInfo> mConAndorInfo=new ArrayList<>();
    public static List<ConDevInfo> mConMvcInfo=new ArrayList<>();
    public static List<ConDevInfo> mAndorOnCap=new ArrayList();
    public static List<ConDevInfo> mMvcOnCap=new ArrayList();
    private ArrayAdapter<String> adapter;
    Bundle devBundle=new Bundle();
    public static boolean gbSaveConInfo=false;
    public static boolean gbExpSetDone=false;
    public static boolean gbImgSetDone=false;

    private OnFragmentInteractionListener mListener;
    private Bundle outState;
    private Bundle savedState;

    public DevsetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DevsetFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DevsetFragment newInstance(String param1, String param2) {
        DevsetFragment fragment = new DevsetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        devset_Layout=inflater.inflate(R.layout.fragment_devset, container, false);
        return devset_Layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initControls(devset_Layout);
        if(getActivity()!=null){
            initData();
            initView();
            setListener();
        }
    }

    private void initControls(View view) {
        tv_DevRunInfo=(TextView)view.findViewById(R.id.tv_sbyxxx);
        tv_aboutDev=(TextView)view.findViewById(R.id.tv_aboutDev);
        tv_bgsz=(TextView)view.findViewById(R.id.tv_bgsz);
        tv_txsz=(TextView)view.findViewById(R.id.tv_txsz);
        tv_DevType=(TextView)view.findViewById(R.id.tv_devType);
        chk_OpenCCD=(CheckBox)view.findViewById(R.id.chk_openCCD);
        chk_startCapture=(CheckBox)view.findViewById(R.id.chk_startCapture);
        mySpinner=(Spinner)view.findViewById(R.id.spinner_andorDev);
    }

    private void initData() {
        devBundle=getArguments();
        if(devBundle==null){
            return;
        }
        strDevType=devBundle.getString("deviceType");
        if(strDevType.equals("1型大气测量设备")){
            nAndorSetUIOpenTime++;
            initDataFromBundle(devBundle);
            mDevNum=mConAndorInfo.size();
        }else if(strDevType.equals("2型大气测量设备")){
            nMvcSetUIOpenTime++;
            initDataFromBundle(devBundle);
            mDevNum=mConMvcInfo.size();
        }
    }

    private void initDataFromBundle(Bundle bundle) {
        for(String key:bundle.keySet()) {
            if(!key.equals("deviceType")){
                Bundle item=bundle.getBundle(key);
                ConDevInfo devSetItem=new ConDevInfo();
                devSetItem.deviceId=item.getInt("deviceId");
                devSetItem.strDeviceType=item.getString("deviceType");
                devSetItem.strDeviceName=item.getString("deviceName");
                devSetItem.bSaveconinfo=item.getBoolean("saveConInfo");
                devSetItem.strDeviceIP=item.getString("IP");
                devSetItem.nDevicePort=item.getInt("Port");
                devSetItem.bSetDeviceOn=false;
                devSetItem.dCjzjg=0.0;
                devSetItem.nCjzjgunit=0;
                devSetItem.nCjzs=0;
                devSetItem.dLjzjg=0.0;
                devSetItem.nLjzjgunit=0;
                devSetItem.nLjzs=0;
                devSetItem.dBgsj=0.0;
                devSetItem.nBgsjunit=0;
                devSetItem.dhdbhxs=0.0;
                devSetItem.nQshzb=0;
                devSetItem.nQszzb=0;
                devSetItem.nTxkd=0;
                devSetItem.nTxgd=0;
                devSetItem.bSaveexp=false;
                devSetItem.bSaveimgset=false;
                devSetItem.bDevRun=false;   //初始都为false，等待大气测量设备端发送更新
                devSetItem.bCapture=false;  //初始都为false，等待大气测量设备端发送更新
                devSetItem.nAndroidId=-1;   //初始都为-1，等待大气测量设备端发送更新
                if(strDevType.equals("1型大气测量设备")){
                    mConAndorInfo.add(devSetItem);
                }else if(strDevType.equals("2型大气测量设备")){
                    mConMvcInfo.add(devSetItem);
                }
                mDevNum++;
            }
        }
    }

    private void initView() {
        int size= mDevNum;
        int deviceId=0;
        for(String key:devBundle.keySet()) {
            if(!key.equals("deviceType")){
                Bundle item=devBundle.getBundle(key);
                deviceId=item.getInt("deviceId");
                devIdlist.add(new Integer(deviceId));
                list.add(deviceId+"号设备");
            }
        }
        adapter = new ArrayAdapter<String>(this.getActivity(),android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(adapter);
        ConDevInfo item;
        if(strDevType.equals("1型大气测量设备")){
            if(mConAndorInfo.size()>0){
                mySpinner.setSelection(nLastAndorPos);
                item=mConAndorInfo.get(nLastAndorPos);
                if(item.bDevRun){
                    chk_OpenCCD.setChecked(true);
                    chk_startCapture.setChecked(item.bCapture);
                    if(item.nAndroidId==MyApplication.thisDevId){
                        nControlDevId=MyApplication.thisDevId;
                        tv_DevRunInfo.setText(nControlDevId+"号Android终端正在占用本设备");
                    }else{
                        chk_OpenCCD.setEnabled(false);
                        chk_startCapture.setEnabled(false);
                    }
                }else
                {
                    chk_OpenCCD.setChecked(false);
                    chk_startCapture.setChecked(false);
                    tv_DevRunInfo.setText("当前设备关闭");
                }
                mySpinner.setSelection(nLastAndorPos);
            }else{
                tv_DevRunInfo.setText("当前无1型大气测量设备运行");
                chk_OpenCCD.setEnabled(false);
                chk_startCapture.setEnabled(false);
            }
        }else if(strDevType.equals("2型大气测量设备")){
            if(mConMvcInfo.size()>0){
                mySpinner.setSelection(nLastMvcPos);
                item=mConMvcInfo.get(nLastMvcPos);
                if(item.bDevRun){
                    chk_OpenCCD.setChecked(true);
                    chk_startCapture.setChecked(item.bCapture);
                    if(item.nAndroidId==MyApplication.thisDevId){
                        nControlDevId=MyApplication.thisDevId;
                        tv_DevRunInfo.setText(nControlDevId + "号Android终端正在占用本设备");
                    }else{
                        chk_OpenCCD.setEnabled(false);
                        chk_startCapture.setEnabled(false);
                    }
                }else
                {
                    chk_OpenCCD.setChecked(false);
                    chk_startCapture.setChecked(false);
                    tv_DevRunInfo.setText("当前设备关闭");
                }
                mySpinner.setSelection(nLastMvcPos);
            }else{
                tv_DevRunInfo.setText("当前无2型大气测量设备运行");
                chk_OpenCCD.setEnabled(false);
                chk_startCapture.setEnabled(false);
            }
        }
        tv_DevType.setText(strDevType /*+ "设备"*/);
        tv_aboutDev.setText("关于" + strDevType + "设置");
    }

    private void setListener() {
        mySpinner.setOnItemSelectedListener(this);
        tv_bgsz.setOnClickListener(this);
        tv_txsz.setOnClickListener(this);
        tv_aboutDev.setOnClickListener(this);
        chk_OpenCCD.setOnCheckedChangeListener(this);
        chk_startCapture.setOnCheckedChangeListener(this);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(gbExpSetDone){
            ConDevInfo item=new ConDevInfo();
            if(strDevType.equals("1型大气测量设备")){
                item=mConAndorInfo.get(nLastAndorPos);
            }else if(strDevType.equals("2型大气测量设备")){
                item=mConMvcInfo.get(nLastMvcPos);
            }
            item.dCjzjg=ExpsetActivity.gdCjzjg;
            item.dLjzjg=ExpsetActivity.gdLjzjg;
            item.dBgsj=ExpsetActivity.gdBgsj;
            item.nCjzjgunit=ExpsetActivity.gnCjzjgunit;
            item.nLjzjgunit=ExpsetActivity.gnLjzjgunit;
            item.nBgsjunit=ExpsetActivity.gnBgsjunit;
            item.nCjzs=ExpsetActivity.gnCjzs;
            item.nLjzs=ExpsetActivity.gnLjzs;
            item.dhdbhxs=ExpsetActivity.gdHdbhxs;
            item.bSaveexp=ExpsetActivity.gbSaveExp;
            item.bSaveconinfo=gbSaveConInfo;
            item.deviceId=gnDevId;
            //为了保证mConDevInfo与数据库中相同记录数据的一致性，需要将数据库中所做的增加记录操作同步到mConDevInfo中
            if(strDevType.equals("1型大气测量设备")){
               mConAndorInfo.set(nLastAndorPos,item);
               list.set(nLastAndorPos,item.deviceId+"号设备");
               devIdlist.set(nLastAndorPos,item.deviceId);
            }else if(strDevType.equals("2型大气测量设备")){
                mConMvcInfo.set(nLastMvcPos,item);
                list.set(nLastMvcPos,item.deviceId+"号设备");
                devIdlist.set(nLastMvcPos,item.deviceId);
            }
            adapter = new ArrayAdapter<String>(this.getActivity(),android.R.layout.simple_spinner_item, list);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mySpinner.setAdapter(adapter);
            if(strDevType.equals("1型大气测量设备")){
                mySpinner.setSelection(nLastAndorPos);
            }else if(strDevType.equals("2型大气测量设备")){
                mySpinner.setSelection(nLastMvcPos);
            }
            gbExpSetDone=false;
        }
        if(gbImgSetDone){
            ConDevInfo item=new ConDevInfo();
            if(strDevType.equals("1型大气测量设备")){
                item=mConAndorInfo.get(nLastAndorPos);
            }else if(strDevType.equals("2型大气测量设备")){
                item=mConMvcInfo.get(nLastMvcPos);
            }
            item.nQshzb=ImagesetActivity.gnQshzb;
            item.nQszzb=ImagesetActivity.gnQszzb;
            item.nTxkd=ImagesetActivity.gnTxkd;
            item.nTxgd=ImagesetActivity.gnTxgd;
            item.bSaveimgset=ImagesetActivity.gbSaveImgSet;
            item.bSaveconinfo=gbSaveConInfo;
            item.deviceId=gnDevId;
            //为了保证mConDevInfo与数据库中相同记录数据的一致性，需要将数据库中所做的增加记录操作同步到mConDevInfo中
            if(strDevType.equals("1型大气测量设备")){
                mConAndorInfo.set(nLastAndorPos,item);
                list.set(nLastAndorPos,item.deviceId+"号设备");
                devIdlist.set(nLastAndorPos,item.deviceId);

            }else if(strDevType.equals("2型大气测量设备")){
                mConMvcInfo.set(nLastMvcPos,item);
                list.set(nLastMvcPos,item.deviceId+"号设备");
                devIdlist.set(nLastMvcPos,item.deviceId);
            }
            adapter = new ArrayAdapter<String>(this.getActivity(),android.R.layout.simple_spinner_item, list);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mySpinner.setAdapter(adapter);
            if(strDevType.equals("1型大气测量设备")){
                mySpinner.setSelection(nLastAndorPos);
            }else if(strDevType.equals("2型大气测量设备")){
                mySpinner.setSelection(nLastMvcPos);
            }
            gbImgSetDone=false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
        saveStateToArguments();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ConDevInfo item;
        if(strDevType.equals("1型大气测量设备")){
            nLastAndorPos=position;
            item=mConAndorInfo.get(nLastAndorPos);
            if(item.bDevRun){
                chk_OpenCCD.setChecked(true);
                nControlDevId=item.nAndroidId;
                tv_DevRunInfo.setText(nControlDevId+"号Android终端正在占用本设备");
                if(item.bCapture){
                    chk_startCapture.setChecked(true);
                }else{
                    chk_startCapture.setChecked(false);
                }
            }else
            {
                chk_OpenCCD.setChecked(false);
                tv_DevRunInfo.setText("当前设备关闭");
                chk_startCapture.setChecked(false);
            }
        }else if(strDevType.equals("2型大气测量设备")){
            nLastMvcPos=position;
            item=mConMvcInfo.get(nLastMvcPos);
            if(item.bDevRun){
                chk_OpenCCD.setChecked(true);
                nControlDevId=item.nAndroidId;
                tv_DevRunInfo.setText(nControlDevId+"号Android终端正在占用本设备");
                if(item.bCapture){
                    chk_startCapture.setChecked(true);
                }else{
                    chk_startCapture.setChecked(false);
                }
            }else
            {
                chk_OpenCCD.setChecked(false);
                tv_DevRunInfo.setText("当前设备关闭");
                chk_startCapture.setChecked(false);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.tv_bgsz:
                //打开曝光设置界面
                if(mDevNum>0){
                    OpenExpset();
                }
                break;
            case R.id.tv_txsz:
                //打开图像设置界面
                if(mDevNum>0){
                    OpenImageset();
                }
                break;
            case R.id.tv_aboutDev:
                //打开关于设备界面
                OpenAboutDev();
                break;
            default:
                break;
        }
    }

    /**
     * 打开关于设备设置界面
     * Added by Bao guangpu on 2016/4/8
     */
    private void OpenAboutDev() {
        Intent intent = new Intent();
        intent.putExtra("devType",strDevType);
        Utils.start_Activity(getActivity(), AboutdevsetActivity.class, intent);
    }

    /**
     * 打开图像设置界面
     * Added by Bao guangpu on 2016/4/8
     */
    private void OpenImageset() {
        Intent intent = new Intent();
        if(strDevType.equals("1型大气测量设备")){
            Bundle item= GetConDevInfo(mConAndorInfo,nLastAndorPos);
            intent.putExtras(item);
        }else if(strDevType.equals("2型大气测量设备")){
            Bundle item=GetConDevInfo(mConMvcInfo,nLastMvcPos);
            intent.putExtras(item);
        }
        Utils.start_Activity(getActivity(), ImagesetActivity.class, intent);
    }

    /**
     * 打开曝光设置界面
     * Added by Bao guangpu on 2016/4/8
     */
    private void OpenExpset() {
        Intent intent=new Intent();
        if(strDevType.equals("1型大气测量设备")){
            Bundle item= GetConDevInfo(mConAndorInfo,nLastAndorPos);
            intent.putExtras(item);
        }else if(strDevType.equals("2型大气测量设备")){
            Bundle item=GetConDevInfo(mConMvcInfo,nLastMvcPos);
            intent.putExtras(item);
        }
        Utils.start_Activity(getActivity(), ExpsetActivity.class, intent);
    }

    /**
     * 根据索引取出设备信息
     * @param nPos
     * @return
     * Added by Bao guangpu on 2016/4/8
     */
    private Bundle GetConDevInfo(List<ConDevInfo> mConDevInfo,int nPos) {
        Bundle bundle=new Bundle();
        if(mConDevInfo.size()==0) {
            return null;
        }
        ConDevInfo item=mConDevInfo.get(nPos);
        if(item.bSaveconinfo || item.bSaveexp || item.bSaveimgset){
            //从数据库中取出之前的设置
            ConnectActivity.utils.opendb(getActivity());
            Cursor cursor=null;
            try{
                cursor=ConnectActivity.utils.findById("ConInfo","id",item.deviceId,null);
                cursor.moveToFirst();
                bundle.putInt("deviceId", cursor.getInt(0));
                bundle.putString("deviceName", cursor.getString(1));
                bundle.putString("deviceType", cursor.getString(2));
                bundle.putString("deviceIP", cursor.getString(3));
                bundle.putInt("devicePort", cursor.getInt(4));
                bundle.putDouble("cjzjg", cursor.getDouble(5));
                bundle.putInt("cjzjgunit", cursor.getInt(6));
                bundle.putInt("cjzs", cursor.getInt(7));
                bundle.putDouble("ljzjg", cursor.getDouble(8));
                bundle.putInt("ljzjgunit", cursor.getInt(9));
                bundle.putInt("ljzs", cursor.getInt(10));
                bundle.putDouble("bgsj", cursor.getDouble(11));
                bundle.putInt("bgsjunit", cursor.getInt(12));
                bundle.putDouble("hdbhxs", cursor.getDouble(13));
                bundle.putInt("qshzb", cursor.getInt(14));
                bundle.putInt("qszzb", cursor.getInt(15));
                bundle.putInt("txkd",cursor.getInt(16));
                bundle.putInt("txgd", cursor.getInt(17));
                bundle.putBoolean("saveConInfo", item.bSaveconinfo);
                bundle.putInt("saveExp", cursor.getInt(18));
                bundle.putInt("saveImgSet", cursor.getInt(19));
            }catch(Exception e){
                //从数据库中取指定数据失败
                Log.e("DevconseFragment", e.toString());
                Log.d("DevsetFragment", "取指定数据失败");
                bundle.putInt("deviceId", item.deviceId);
                bundle.putString("deviceName", item.strDeviceName);
                bundle.putString("deviceType", item.strDeviceType);
                bundle.putString("deviceIP",item.strDeviceIP);
                bundle.putInt("devicePort", item.nDevicePort);
                bundle.putDouble("cjzjg", item.dCjzjg);
                bundle.putInt("cjzjgunit", item.nCjzjgunit);
                bundle.putInt("cjzs", item.nCjzs);
                bundle.putDouble("ljzjg", item.dLjzjg);
                bundle.putInt("ljzjgunit", item.nLjzjgunit);
                bundle.putInt("ljzs", item.nLjzs);
                bundle.putDouble("bgsj", item.dBgsj);
                bundle.putInt("bgsjunit", item.nBgsjunit);
                bundle.putDouble("hdbhxs", item.dhdbhxs);
                bundle.putInt("qshzb", item.nQshzb);
                bundle.putInt("qszzb", item.nQszzb);
                bundle.putInt("txkd",item.nTxkd);
                bundle.putInt("txgd", item.nTxgd);
                bundle.putBoolean("saveConInfo", item.bSaveconinfo);
                bundle.putBoolean("saveExp", item.bSaveexp);
                bundle.putBoolean("saveImgSet", item.bSaveimgset);
            }finally {
                if(cursor!=null) {
                    cursor.close();
                }
                ConnectActivity.utils.closedb();
            }
        }else{
            //如果不在数据库中
            bundle.putInt("deviceId", item.deviceId);
            bundle.putString("deviceName",item.strDeviceName);
            bundle.putString("deviceType", item.strDeviceType);
            bundle.putString("deviceIP",item.strDeviceIP);
            bundle.putInt("devicePort",item.nDevicePort);
            bundle.putDouble("cjzjg", item.dCjzjg);
            bundle.putInt("cjzjgunit", item.nCjzjgunit);
            bundle.putInt("cjzs", item.nCjzs);
            bundle.putDouble("ljzjg", item.dLjzjg);
            bundle.putInt("ljzjgunit",item.nLjzjgunit);
            bundle.putInt("ljzs", item.nLjzs);
            bundle.putDouble("bgsj", item.dBgsj);
            bundle.putInt("bgsjunit", item.nBgsjunit);
            bundle.putDouble("hdbhxs", item.dhdbhxs);
            bundle.putInt("qshzb",item.nQshzb);
            bundle.putInt("qszzb",item.nQszzb);
            bundle.putInt("txkd",item.nTxkd);
            bundle.putInt("txgd", item.nTxgd);
            bundle.putBoolean("saveConInfo",item.bSaveconinfo);
            bundle.putBoolean("saveExp",item.bSaveexp);
            bundle.putBoolean("saveImgSet",item.bSaveimgset);
        }
        return bundle;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch(buttonView.getId()){
            case R.id.chk_openCCD:
                if(strDevType=="1型大气测量设备"){
                    onOffCCD(mConAndorInfo);
                }else if(strDevType=="2型大气测量设备"){
                    onOffCCD(mConMvcInfo);
                }
                break;
            case R.id.chk_startCapture:
                if(strDevType=="1型大气测量设备"){
                    onOffCapture(mConAndorInfo);
                }else if(strDevType=="2型大气测量设备"){
                    onOffCapture(mConMvcInfo);
                }
                break;
        }
    }

    private void onOffCapture(List<ConDevInfo> mConDevInfo) {
        if (mConDevInfo.size() > 0) {
            if (chk_OpenCCD.isChecked()) {
                if (chk_startCapture.isChecked()) {
                    ConDevInfo item;
                    if (strDevType.equals("1型大气测量设备")) {
                        item = mConDevInfo.get(nLastAndorPos);
                        if(item.nAndroidId==MyApplication.thisDevId){
                            //如果本机是获得大气测量设备控制权的设备
                            if(item.bCapture){
                                return;
                            }else{
                                if(ConnectActivity.runMode==1){
                                    //以测试模式运行
                                    item.bCapture = true;
                                    mConDevInfo.set(nLastAndorPos, item);
                                    mAndorOnCap.add(item);
                                    return;
                                }
                                boolean bStartCap = startCapture(item.deviceId);
                                if (bStartCap) {
                                    item.bCapture = true;
                                    mConDevInfo.set(nLastAndorPos, item);
                                    mAndorOnCap.add(item);
                                    return;
                                }else{
                                    chk_startCapture.setChecked(false);
                                }
                            }
                        }else{
                            chk_OpenCCD.setEnabled(false);
                            chk_startCapture.setEnabled(false);
                        }
                    } else if (strDevType.equals("2型大气测量设备")) {
                        item = mConDevInfo.get(nLastMvcPos);
                        if(item.nAndroidId==MyApplication.thisDevId){
                            if(item.bCapture){
                                return;
                            }else{
                                if(ConnectActivity.runMode==1){
                                    //以测试模式运行
                                    item.bCapture = true;
                                    mConDevInfo.set(nLastMvcPos, item);
                                    mMvcOnCap.add(item);
                                    return;
                                }
                                boolean bStartCap = startCapture(item.deviceId);
                                if (bStartCap) {
                                    item.bCapture = true;
                                    mConDevInfo.set(nLastMvcPos, item);
                                    mMvcOnCap.add(item);
                                    return;
                                }else{
                                    chk_startCapture.setChecked(false);
                                }
                            }
                        }else{
                            chk_OpenCCD.setEnabled(false);
                            chk_startCapture.setEnabled(false);
                        }
                    }
                }else{
                    ConDevInfo item;
                    if (strDevType.equals("1型大气测量设备")) {
                        item = mConDevInfo.get(nLastAndorPos);
                        if(item.nAndroidId==MyApplication.thisDevId){
                            //如果本机是获得大气测量设备控制权的设备
                            if(!item.bCapture){
                                return;
                            }else{
                                if(ConnectActivity.runMode==1){
                                    //以测试模式运行
                                    item.bCapture = false;
                                    mConDevInfo.set(nLastAndorPos, item);
                                    mAndorOnCap.remove(item);
                                    return;
                                }
                                boolean bStopCap = stopCapture(item.deviceId);
                                if (bStopCap) {
                                    item.bCapture = false;
                                    mConDevInfo.set(nLastAndorPos, item);
                                    mAndorOnCap.remove(item);
                                    return;
                                }else{
                                    chk_startCapture.setChecked(true);
                                }
                            }
                        }else{
                            chk_OpenCCD.setEnabled(false);
                            chk_startCapture.setEnabled(false);
                        }
                    } else if (strDevType.equals("2型大气测量设备")) {
                        item = mConDevInfo.get(nLastMvcPos);
                        if(item.nAndroidId==MyApplication.thisDevId) {
                            if (!item.bCapture) {
                                return;
                            }else{
                                if(ConnectActivity.runMode==1){
                                    //以测试模式运行
                                    item.bCapture = false;
                                    mConDevInfo.set(nLastMvcPos, item);
                                    mMvcOnCap.remove(item);
                                    return;
                                }
                                boolean bStopCap = stopCapture(item.deviceId);
                                if (bStopCap) {
                                    item.bCapture = false;
                                    mConDevInfo.set(nLastMvcPos, item);
                                    mMvcOnCap.remove(item);
                                    return;
                                }else{
                                    chk_startCapture.setChecked(true);
                                }
                            }
                        }else{
                            chk_OpenCCD.setEnabled(false);
                            chk_startCapture.setEnabled(false);
                        }
                    }
                }
            }else{
                chk_startCapture.setChecked(false);
                chk_startCapture.setEnabled(false);
            }
        }
    }

    private void onOffCCD(List<ConDevInfo> mConDevInfo){
        if(mConDevInfo.size()>0){
            if (chk_OpenCCD.isChecked()) {
                ConDevInfo item;
                if(strDevType.equals("1型大气测量设备")){
                    item=mConDevInfo.get(nLastAndorPos);
                    if(item.nAndroidId!=-1 && item.nAndroidId==MyApplication.thisDevId) {
                        //大气测量设备被当前Android设备占用运行
                        chk_startCapture.setEnabled(true);
                        return;
                    }else {
                        if(item.nAndroidId==-1) {
                            //当前设备没有被占用,打开设备
                            if(ConnectActivity.runMode==1){
                                //测试模式
                                item.bDevRun=true;
                                //设置控制大气测量设备Android终端Id
                                item.nAndroidId=MyApplication.thisDevId;
                                mConDevInfo.set(nLastAndorPos, item);
                                tv_DevRunInfo.setText(item.nAndroidId + "号Android终端正在占用本设备");
                                chk_startCapture.setEnabled(true);
                                Utils.showShortToast(this.getActivity().getApplicationContext(), "打开成功");
                                return;
                            }
                            boolean bOpenSuccess=openDevice(item.deviceId);
                            if(bOpenSuccess){
                                item.bDevRun=true;
                                item.nAndroidId=MyApplication.thisDevId;
                                mConDevInfo.set(nLastAndorPos,item);
                                chk_startCapture.setEnabled(true);
                                tv_DevRunInfo.setText(item.nAndroidId + "号Android终端正在占用本设备");
                                Utils.showShortToast(this.getActivity().getApplicationContext(), "打开成功");
                            }else{
                                chk_OpenCCD.setChecked(false);
                                chk_startCapture.setChecked(false);
                                chk_startCapture.setEnabled(false);
                                Utils.showShortToast(this.getActivity().getApplicationContext(), "打开失败");
                            }
                            return;
                        }
                        //大气测量设备被其他设备占用
                        chk_OpenCCD.setChecked(true);
                        chk_OpenCCD.setEnabled(false);
                        if(item.bCapture==true){
                            chk_startCapture.setChecked(true);
                        }
                        chk_startCapture.setEnabled(false);
                    }
                }else if(strDevType.equals("2型大气测量设备")) {
                    item=mConDevInfo.get(nLastMvcPos);
                    if(item.nAndroidId!=-1 && item.nAndroidId==MyApplication.thisDevId) {
                        //大气测量设备被当前Android设备占用
                    }else {
                        if(item.nAndroidId==-1) {
                            //当前设备没有被占用,打开设备
                            if(ConnectActivity.runMode==1){
                                //测试模式
                                item.bDevRun=true;
                                //设置控制大气测量设备Android终端Id
                                item.nAndroidId=MyApplication.thisDevId;
                                mConDevInfo.set(nLastMvcPos,item);
                                chk_startCapture.setEnabled(true);
                                tv_DevRunInfo.setText(item.nAndroidId + "号Android终端正在占用本设备");
                                Utils.showShortToast(this.getActivity().getApplicationContext(), "打开成功");
                                return;
                            }
                            boolean bOpenSuccess=openDevice(item.deviceId);
                            if(bOpenSuccess){
                                item.bDevRun=true;
                                item.nAndroidId=MyApplication.thisDevId;
                                mConDevInfo.set(nLastMvcPos,item);
                                chk_startCapture.setEnabled(true);
                                tv_DevRunInfo.setText(item.nAndroidId + "号Android终端正在占用本设备");
                                Utils.showShortToast(this.getActivity().getApplicationContext(), "打开成功");
                            }else{
                                chk_OpenCCD.setChecked(false);
                                Utils.showShortToast(this.getActivity().getApplicationContext(), "打开失败");
                            }
                            return;
                        }
                        //大气测量设备被其他设备占用
                        chk_OpenCCD.setChecked(true);
                        if(item.bCapture==true){
                            chk_startCapture.setChecked(true);
                        }
                        chk_OpenCCD.setEnabled(false);
                        chk_startCapture.setEnabled(false);
                    }
                }
                Utils.showShortToast(this.getActivity().getApplicationContext(), "打开设备");
            } else {
                ConDevInfo item;
                if(strDevType.equals("1型大气测量设备")){
                    item=mConDevInfo.get(nLastAndorPos);
                    if(!item.bDevRun){
                        //如果设备已经关闭
                        item.bCapture=false;
                        //设置控制大气测量设备Android终端Id
                        item.nAndroidId=-1;
                        mConDevInfo.set(nLastAndorPos,item);
                        chk_startCapture.setChecked(false);
                        chk_startCapture.setEnabled(false);
                        mAndorOnCap.remove(item);
                        tv_DevRunInfo.setText("当前设备关闭");
                        return;
                    }
                    if(ConnectActivity.runMode==1){
                        //测试模式
                        item.bDevRun=false;
                        item.bCapture=false;
                        //设置控制大气测量设备Android终端Id
                        item.nAndroidId=-1;
                        mConDevInfo.set(nLastAndorPos,item);
                        chk_startCapture.setChecked(false);
                        chk_startCapture.setEnabled(false);
                        mAndorOnCap.remove(item);
                        tv_DevRunInfo.setText("当前设备关闭");
                        Utils.showShortToast(this.getActivity().getApplicationContext(), "关闭成功");
                        return;
                    }
                    boolean bShutDownSuccess=shutDownDevice(item.deviceId);
                    if(bShutDownSuccess){
                        item.bDevRun=false;
                        item.bCapture=false;
                        //设置控制大气测量设备Android终端Id
                        item.nAndroidId = -1;
                        mConDevInfo.set(nLastAndorPos, item);
                        chk_startCapture.setChecked(false);
                        chk_startCapture.setEnabled(false);
                        mAndorOnCap.remove(item);
                        tv_DevRunInfo.setText("当前设备关闭");
                        Utils.showShortToast(this.getActivity().getApplicationContext(), "关闭成功");
                        return;
                    }else{
                        chk_OpenCCD.setChecked(true);
                        Utils.showShortToast(this.getActivity().getApplicationContext(), "关闭失败");
                        return;
                    }
                }else if(strDevType.equals("2型大气测量设备")){
                    item=mConDevInfo.get(nLastMvcPos);
                    if(!item.bDevRun){
                        //如果设备已经关闭
                        item.bCapture=false;
                        //设置控制大气测量设备Android终端Id
                        item.nAndroidId=-1;
                        mConDevInfo.set(nLastMvcPos,item);
                        chk_startCapture.setChecked(false);
                        chk_startCapture.setEnabled(false);
                        mAndorOnCap.remove(item);
                        tv_DevRunInfo.setText("当前设备关闭");
                        return;
                    }
                    if(ConnectActivity.runMode==1){
                        //测试模式
                        item.bDevRun=false;
                        item.bCapture=false;
                        //设置控制大气测量设备Android终端Id
                        item.nAndroidId=-1;
                        mConDevInfo.set(nLastMvcPos,item);
                        chk_startCapture.setChecked(false);
                        chk_startCapture.setEnabled(false);
                        mMvcOnCap.remove(item);
                        tv_DevRunInfo.setText("当前设备关闭");
                        Utils.showShortToast(this.getActivity().getApplicationContext(), "关闭成功");
                        return;
                    }
                    boolean bShutDownSuccess=shutDownDevice(item.deviceId);
                    if(bShutDownSuccess){
                        item.bDevRun=false;
                        item.bCapture=false;
                        //设置控制大气测量设备Android终端Id
                        item.nAndroidId=-1;
                        mConDevInfo.set(nLastMvcPos,item);
                        chk_startCapture.setChecked(false);
                        chk_startCapture.setEnabled(false);
                        mMvcOnCap.remove(item);
                        tv_DevRunInfo.setText("当前设备关闭");
                        Utils.showShortToast(this.getActivity().getApplicationContext(), "关闭成功");
                        return;
                    }else{
                        chk_OpenCCD.setChecked(true);
                        Utils.showShortToast(this.getActivity().getApplicationContext(), "关闭失败");
                        return;
                    }
                }
                Utils.showShortToast(this.getActivity().getApplicationContext(), "关闭设备");
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }



    public class ConDevInfo{
        public int deviceId;
        public String strDeviceName;
        public String strDeviceType;
        public String strDeviceIP;
        public int nDevicePort;
        public boolean bSaveconinfo;
        public boolean bSetDeviceOn;
        public double dCjzjg;
        public int nCjzjgunit;
        public int nCjzs;
        public double dLjzjg;
        public int nLjzjgunit;
        public int nLjzs;
        public double dBgsj;
        public int nBgsjunit;
        public double dhdbhxs;
        public boolean bSaveexp;
        public int nQshzb;
        public int nQszzb;
        public int nTxkd;
        public int nTxgd;
        public boolean bSaveimgset;
        public boolean bDevRun;
        public boolean bCapture;
        public int nAndroidId;
    }

    /**
     * 打开大气测量设备操作
     * @param nDevId 打开设备ID
     * @return 打开成功与否
     * Added by Bao guangpu on 2016/4/8
     */
    private boolean openDevice(int nDevId){

        return false;
    }

    /**
     * 关闭大气测量设备操作
     * @param nDevId 关闭设备ID
     * @return
     * Added by Bao guangpu on 2016/4/8
     */
    private boolean shutDownDevice(int nDevId){

        return false;
    }

    /**
     * 开始采集
     * @param nDevId
     * @return
     * Added by Bao guangpu on 2016/4/8
     */
    private boolean startCapture(int nDevId){

        return false;
    }

    /**
     * 停止采集
     * @param nDevId
     * @return
     * Added by Bao guangpu on 2016/4/8
     */
    private boolean stopCapture(int nDevId){

        return false;
    }

    /**
     * 恢复数据
     * @return
     * Added by Bao guangpu on 2016/4/8
     */
    private boolean restoreStateFromArguments() {
        Bundle b = getArguments();
        if(b != null) {
            savedState = b.getBundle("DevsetFragmentUI");
            if(savedState != null) {
                onRestoreState(savedState);
                return true;
            }
        }
        return false;
    }

    private void onRestoreState(Bundle savedInstanceState) {

    }

    /**
     * 保存数据
     * Added by Bao guangpu on 2016/4/8
     */
    private void saveStateToArguments() {
        if(getView() != null) {
            savedState = saveState();
        }
        if(savedState != null) {
            Bundle b = getArguments();
            if(b != null) {
                b.putBundle("DevsetFragmentUI", savedState);
            }
        }
    }

    private Bundle saveState() {
        Bundle state = new Bundle();
        onSaveState(state);
        return state;
    }

    private void onSaveState(Bundle outState) {

    }
}

    
