/**
 * 血的教训，通过apktool反编译，不写注释了
 */
package com.aiofm.eminem.aiofmbgp.views.fragments;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.aiofm.eminem.aiofmbgp.R;
import com.aiofm.eminem.aiofmbgp.services.ConnectService;
import com.aiofm.eminem.aiofmbgp.views.activities.ConnectActivity;
import com.aiofm.eminem.aiofmbgp.views.activities.DevconsetActivity;
import com.aiofm.eminem.aiofmbgp.common.Utils;
import com.aiofm.eminem.aiofmbgp.views.activities.QaActivity;
import com.aiofm.eminem.aiofmbgp.views.controls.SliderListView;
import com.aiofm.eminem.aiofmbgp.views.controls.SliderView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConnectFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConnectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConnectFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private static final String TAG = "ConnectFragment";
    private Button btn_Connect;
    private View connect_Layout;
    private Cursor cursor=null;
    private String[] devTypeSet;
    public SharedPreferences.Editor editor;
    private ImageView iv_AddDevice;
    private SliderListView mSliderListView;
    private String mStrDevType;
    private Bundle savedState;
    private Spinner spin_Devtype;
    private TextView tv_Qa;
    private static Boolean gbAddDev = Boolean.valueOf(false);
    private static Boolean gbChangeDev = Boolean.valueOf(false);
    public static Boolean mbFirstLaunch = Boolean.valueOf(true);
    private int nLastDevPos = 0;
    private long mLastClick = 0;
    public static int deviceNum = 0;
    public static int maxDeviceId = 0;
    public static int mClickedItem = 0;
    public static int mSelectedItem =0;
    public static ConnectFragment.SliderAdapter sliderAdapter = null;
    public static List<DeviceInfo> mDeviceInfo = new ArrayList();
    private AlertDialog myDialog = null;
    private ArrayAdapter<String> adapter;

    public ConnectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConnectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConnectFragment newInstance(String param1, String param2) {
        ConnectFragment fragment = new ConnectFragment();
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
        Log.d(TAG,"fragment create success");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        connect_Layout=inflater.inflate(R.layout.fragment_connect, container, false);
        Log.d(TAG, "fragment createview success");
        return connect_Layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initControls(connect_Layout);
        initData();
        Log.d(TAG,"Activity start success");
        if(this.getActivity()!=null){
            Log.d(TAG,"在onActivityCreated中获取Activity成功");
        }
        if(restoreStateFromArguments()) {
        } else {
            nLastDevPos = 0;
            mStrDevType = devTypeSet[0];
        }
        initView();
        setListener();
    }

    /**
     * 初始化控件
     * @param view fragment视图
     * Added by Bao guangpu on 2016/4/8
     */
    private void initControls(View view) {
        spin_Devtype = (Spinner)view.findViewById(R.id.spinnerLjsb);
        iv_AddDevice = (ImageView)view.findViewById(R.id.imageAddDevice);
        btn_Connect = (Button)view.findViewById(R.id.btnConnect);
        tv_Qa = (TextView)view.findViewById(R.id.tvQa);
        mSliderListView = (SliderListView)view.findViewById(R.id.sliderlvDeviceInfo);
    }

    /**
     * 初始化数据
     * Added by Bao guangpu on 2016/4/8
     */
    private void initData() {
        devTypeSet=getResources().getStringArray(R.array.device);
        if(mbFirstLaunch){
            ConnectActivity.utils.opendb(getActivity());
            try{
                cursor=ConnectActivity.utils.findAll("ConInfo");
                while(cursor.moveToNext()){
                    DeviceInfo item=new DeviceInfo();
                    item.deviceId=cursor.getInt(0);
                    item.strDeviceName=cursor.getString(1);
                    item.strDeviceType=cursor.getString(2);
                    item.deviceIP=cursor.getString(3);
                    item.devicePort=cursor.getInt(4);
                    item.saveFlag=true;
                    item.mbbkYellow=0;
                    item.selectFlag=false;
                    mDeviceInfo.add(item);
                    ++deviceNum;
                    maxDeviceId=item.deviceId;
                }
            }catch(Exception e){
                Log.e(TAG,e.toString());
                Utils.showShortToast(getActivity(),"初始化数据失败");
            }finally {
                mbFirstLaunch=false;
                if(cursor!=null){
                    cursor.close();
                }
                ConnectActivity.utils.closedb();
            }
        }
    }

    /**
     * 初始化视图
     * Added by Bao guangpu on 2016/4/8
     */
    private void initView() {
        spin_Devtype.setSelection(nLastDevPos);
        Log.d(TAG, "init sliderlistview success");
        sliderAdapter=new SliderAdapter(this);
        mSliderListView.setAdapter(sliderAdapter);
    }

    /**
     * 设置监听器
     * Added by Bao guangpu on 2016/4/8
     */
    private void setListener() {
        spin_Devtype.setOnItemSelectedListener(this);
        iv_AddDevice.setOnClickListener(this);
        tv_Qa.setOnClickListener(this);
        btn_Connect.setOnClickListener(this);
        mSliderListView.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ConnectFragment.DeviceInfo item = new ConnectFragment.DeviceInfo();
        sliderAdapter = getAdapter();
        if(DevconsetActivity.gbSetDone) {
            if(gbAddDev.booleanValue()) {
                if(DevconsetActivity.bSave){
                    ConnectActivity.utils.opendb(getActivity().getApplicationContext());
                    try {
                        cursor=ConnectActivity.utils.getMaxIdTurple("ConInfo","id");
                        item.deviceId = cursor.getInt(0);
                        item.strDeviceName = cursor.getString(1);
                        item.strDeviceType = cursor.getString(2);
                        item.deviceIP = cursor.getString(3);
                        item.devicePort = cursor.getInt(4);
                        item.saveFlag = DevconsetActivity.bSave;
                        item.mbbkYellow = 0;
                    } catch(Exception e) {
                        Utils.showShortToast(getActivity(), "取最大Id失败");
                        item.deviceId = ++maxDeviceId;
                        item.strDeviceType = DevconsetActivity.strDevtype;
                        item.strDeviceName = DevconsetActivity.strDevname;
                        item.deviceIP = DevconsetActivity.strIP;
                        item.devicePort = Integer.parseInt(DevconsetActivity.strPort);
                        item.saveFlag = DevconsetActivity.bSave;
                        item.mbbkYellow = 0;
                    }finally {
                        if(cursor!=null){
                            cursor.close();
                        }
                        ConnectActivity.utils.closedb();
                    }
                }else{
                    item.deviceId = ++maxDeviceId;
                    item.strDeviceType = DevconsetActivity.strDevtype;
                    item.strDeviceName = DevconsetActivity.strDevname;
                    item.deviceIP = DevconsetActivity.strIP;
                    item.devicePort = Integer.parseInt(DevconsetActivity.strPort);
                    item.saveFlag = DevconsetActivity.bSave;
                    item.mbbkYellow = 0;
                }
                mDeviceInfo.add(item);
                ++deviceNum;
                gbAddDev = Boolean.valueOf(false);
                sliderAdapter.notifyDataSetChanged();
                mSliderListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            }
            if(gbChangeDev.booleanValue()) {
                item.deviceId = DevconsetActivity.nDevId;
                item.strDeviceType = DevconsetActivity.strDevtype;
                item.strDeviceName = DevconsetActivity.strDevname;
                item.deviceIP = DevconsetActivity.strIP;
                item.devicePort = Integer.parseInt(DevconsetActivity.strPort);
                item.saveFlag = DevconsetActivity.bSave;
                item.mbbkYellow=0;
                gbChangeDev = Boolean.valueOf(false);
                sliderAdapter.updateView(mClickedItem, mSliderListView, item);
            }
            DevconsetActivity.gbSetDone = false;
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("ConnectFragment", "OnItemClick position: " + position);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mStrDevType = devTypeSet[position];
        nLastDevPos = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnConnect:
                connectToPC();
                break;
            case R.id.imageAddDevice:
                addDevice();
                break;
            case R.id.tvQa:
                help();
                break;
            case R.id.rl_edit:
                editDeviceInfo();
                break;
            case R.id.rl_select:
                selectItem();
                break;
            case R.id.rl_del:
                delOrNot();
                break;
        }
    }

    /**
     * 打开帮助界面
     * Added by Bao guangpu on 2016/4/8
     */
    private void help() {
        Intent intent=new Intent(getActivity(),QaActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        else
            startActivity(intent);
    }

    /**
     * 添加设备
     * Added by Bao guangpu on 2016/4/8
     */
    private void addDevice() {
        Intent intent = new Intent();
        intent.putExtra("operation", "add");
        intent.putExtra("device_type", mStrDevType);
        gbAddDev = Boolean.valueOf(true);
        Utils.start_Activity(getActivity(), DevconsetActivity.class, intent);
    }

    /**
     * 连接server
     * Added by Bao guangpu on 2016/4/8
     */
    private void connectToPC() {
        int size=mDeviceInfo.size();
        Intent intent=new Intent(getActivity(), ConnectService.class);
        int j=0;
        for(int i=1;i<=size;i++){
            DeviceInfo item=mDeviceInfo.get(i-1);
            //判断连接设备
            if(item.selectFlag){
                Bundle bundle=new Bundle();
                bundle.putInt("deviceId", item.deviceId);
                bundle.putString("deviceName", item.strDeviceName);
                bundle.putString("deviceType",item.strDeviceType);
                bundle.putString("IP", item.deviceIP);
                bundle.putInt("Port", item.devicePort);
                bundle.putBoolean("saveConInfo",item.saveFlag);
                intent.putExtra(Integer.toString(i),bundle);
                j++;
            }
        }
        if(j==0){
            Utils.showShortToast(getActivity().getApplicationContext(),"请选择连接设备");
        }
        getActivity().startService(intent);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public class DeviceInfo {
        public int deviceId;
        public String strDeviceName;
        public String strDeviceType;
        public String deviceIP;
        public int devicePort;
        public boolean saveFlag;
        public int mbbkYellow;
        public boolean selectFlag;
    }

    class ViewHolder {
        public ImageView iv_Icon;
        public TextView tv_DeviceId;
        public TextView tv_DeviceName;
        public TextView tv_DeviceType;
        public ViewGroup vg_rlDel;
        public ViewGroup vg_rlEdit;
        public ViewGroup vg_rlSelect;

        ViewHolder(View view) {
            iv_Icon = (ImageView)view.findViewById(R.id.image_icon);
            tv_DeviceName = (TextView)view.findViewById(R.id.tv_devicename);
            tv_DeviceType = (TextView)view.findViewById(R.id.tv_devicetype);
            tv_DeviceId = (TextView)view.findViewById(R.id.tv_deviceid);
            vg_rlEdit = (ViewGroup)view.findViewById(R.id.rl_edit);
            vg_rlDel = (ViewGroup)view.findViewById(R.id.rl_del);
            vg_rlSelect = (ViewGroup)view.findViewById(R.id.rl_select);
        }
    }

    public class SliderAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        private SliderAdapter(ConnectFragment p1) {
            mInflater = p1.getActivity().getLayoutInflater();
        }

        @Override
        public int getCount() {
            return mDeviceInfo.size();
        }

        @Override
        public Object getItem(int position) {
            return mDeviceInfo.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * 通过convertView和ViewHolder来复用自定义listview item，优化自定义listview效率
         * @param position 该视图在适配器数据中的位置
         * @param convertView 旧视图
         * @param parent 此视图最终会被附加到的父级视图
         * @return
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(ConnectFragment.this.getActivity()==null){
                Log.d(TAG,"获取activity失败");
                return null;
            }
            ConnectFragment.DeviceInfo item = mDeviceInfo.get(position);
            SliderView sliderView = (SliderView)convertView;
            ConnectFragment.ViewHolder holder;
            if(sliderView == null) {
                View itemView = mInflater.inflate(R.layout.listitem,null);
                sliderView = new SliderView(ConnectFragment.this.getActivity());
                sliderView.setContentView(itemView);
                holder = new ConnectFragment.ViewHolder(sliderView);
                sliderView.setTag(holder);
            } else {
                holder = (ConnectFragment.ViewHolder)sliderView.getTag();
            }
            sliderView.shrink();
            if(item.mbbkYellow == 0) {
                sliderView.setBackgroundResource(0);
            } else {
                sliderView.setBackgroundResource(R.drawable.holder_bg3);
            }
            holder.tv_DeviceId.setText(Integer.toString(item.deviceId));
            holder.tv_DeviceName.setText(item.strDeviceName);
            holder.tv_DeviceType.setText(item.strDeviceType);
            holder.vg_rlEdit.setOnClickListener(ConnectFragment.this);
            holder.vg_rlDel.setOnClickListener(ConnectFragment.this);
            holder.vg_rlSelect.setOnClickListener(ConnectFragment.this);
            return sliderView;
        }

        /**
         * 重置指定itemview位置
         * @param position
         * @param listView
         * @return
         * Added by Bao guangpu on 2016/4/8
         */
        public boolean resetItemPos(int position, SliderListView listView){
            int visibleFirstPos = listView.getFirstVisiblePosition();
            int visibleLastPos = listView.getLastVisiblePosition();
            if((position >= visibleFirstPos) && (position <= visibleLastPos)) {
                SliderView view=(SliderView)listView.getChildAt((position - visibleFirstPos));
                view.shrink();
                return true;
            }
            else{
                return false;
            }
        }

        /**
         * 重置指定item的背景
         * @param position
         * @param listView
         * @return
         * Added by Bao guangpu on 2016/4/8
         */
        public boolean resetItemBk(int position, SliderListView listView){
            int visibleFirstPos = listView.getFirstVisiblePosition();
            int visibleLastPos = listView.getLastVisiblePosition();
            if((position >= visibleFirstPos) && (position <= visibleLastPos)) {
                SliderView view=(SliderView)listView.getChildAt((position - visibleFirstPos));
                view.setBackgroundResource(0);
                return true;
            }
            return false;
        }

        /**
         * 获取指定item视图
         * @param position
         * @param listView
         * @return
         * Added by Bao guangpu on 2016/4/8
         */
        public SliderView getItemView(int position, SliderListView listView) {
            int visibleFirstPos = listView.getFirstVisiblePosition();
            int visibleLastPos = listView.getLastVisiblePosition();
            if((position >= visibleFirstPos) && (position <= visibleLastPos)) {
                return (SliderView)listView.getChildAt((position - visibleFirstPos));
            }
            return null;
        }

        /**
         * 更新指定item视图
         * @param position
         * @param listView
         * @param item
         * Added by Bao guangpu on 2016/4/8
         */
        public void updateView(int position, SliderListView listView, ConnectFragment.DeviceInfo item) {
            int visibleFirstPos = listView.getFirstVisiblePosition();
            int visibleLastPos = listView.getLastVisiblePosition();
            if((position >= visibleFirstPos) && (position <= visibleLastPos)) {
                SliderView view = (SliderView)listView.getChildAt((position - visibleFirstPos));
                ConnectFragment.ViewHolder holder = (ConnectFragment.ViewHolder)view.getTag();
                if(item != null) {
                    mDeviceInfo.set(position, item);
                    holder.tv_DeviceId.setText(Integer.toString(item.deviceId));
                    holder.tv_DeviceName.setText(item.strDeviceName);
                    holder.tv_DeviceType.setText(item.strDeviceType);
                    view.shrink();
                    Log.d(TAG, "更新"+position+"位置数据成功");
                    return;
                }
                return;
            }
            mDeviceInfo.set(position, item);
        }
    }

    /**
     * 编辑设备信息
     * Added by Bao guangpu on 2016/4/8
     */
    private void editDeviceInfo() {
        sliderAdapter = getAdapter();
        ConnectFragment.DeviceInfo deviceInfo;
        try {
            deviceInfo = (ConnectFragment.DeviceInfo)sliderAdapter.getItem(mClickedItem);
        } catch(IndexOutOfBoundsException e) {
            Utils.showShortToast(getActivity().getApplicationContext(), "编辑失败");
            return;
        }
        sliderAdapter.resetItemPos(mClickedItem,mSliderListView);
        Intent intent = new Intent();
        intent.putExtra("operation", "edit");
        intent.putExtra("id", deviceInfo.deviceId);
        intent.putExtra("device_name", deviceInfo.strDeviceName);
        intent.putExtra("device_type", deviceInfo.strDeviceType);
        intent.putExtra("IP", deviceInfo.deviceIP);
        intent.putExtra("Port", Integer.toString(deviceInfo.devicePort));
        intent.putExtra("Check_Status", deviceInfo.saveFlag);
        gbChangeDev = Boolean.valueOf(true);
        Utils.start_Activity(getActivity(), DevconsetActivity.class, intent);
        sliderAdapter.resetItemPos(mClickedItem, mSliderListView);
    }

    /**
     * 选中item
     * Added by Bao guangpu on 2016/4/8
     */
    private void selectItem() {
        DeviceInfo item = mDeviceInfo.get(mClickedItem);
        sliderAdapter = getAdapter();
        if(item.mbbkYellow == 1) {
            item.mbbkYellow = 0;
            item.selectFlag=false;
            mDeviceInfo.set(mClickedItem, item);
            sliderAdapter.notifyDataSetChanged();
            sliderAdapter.resetItemPos(mClickedItem, mSliderListView);
        }else if(item.mbbkYellow==0){
            item.mbbkYellow = 1;
            item.selectFlag=true;
            mDeviceInfo.set(mClickedItem, item);
            sliderAdapter.notifyDataSetChanged();
            sliderAdapter.resetItemPos(mClickedItem,mSliderListView);
        }
        sliderAdapter.resetItemPos(mClickedItem,mSliderListView);
    }

    /**
     * 判断是否删除
     * Added by Bao guangpu on 2016/4/8
     */
    private void delOrNot() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(getActivity());
        myDialog = myDialog.setTitle("确定删除？");
        myDialog.setIcon(R.drawable.ic_warning_black_36dp);
        myDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delDevInfo();
            }
        }).setNegativeButton("不删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create();
        myDialog.show();
    }

    /**
     * 删除设备信息
     * Added by Bao guangpu on 2016/4/8
     */
    private void delDevInfo() {
        sliderAdapter = getAdapter();
        ConnectFragment.DeviceInfo item = mDeviceInfo.get(mClickedItem);
        int devId = item.deviceId;
        item.mbbkYellow = 0;
        mDeviceInfo.set(mClickedItem, item);
        if(devId == maxDeviceId) {
            --maxDeviceId;
        }
        ConnectActivity.utils.opendb(getActivity());
        try {
            ConnectActivity.utils.delete("ConInfo", "id", Integer.toString(devId));
        } catch(Exception e) {
            Log.e("ConnectFragment", e.toString());
            Utils.showShortToast(getActivity().getApplicationContext(), "删除失败");
        } finally {
            ConnectActivity.utils.closedb();
        }
        SliderView view = sliderAdapter.getItemView(mClickedItem, mSliderListView);
        view.setBackgroundResource(0);
        mDeviceInfo.remove(mClickedItem);
        sliderAdapter.notifyDataSetChanged();
        --deviceNum;
    }

    /**
     * 获取自定义适配器
     * @return
     * Added by Bao guangpu on 2016/4/8
     */
    public ConnectFragment.SliderAdapter getAdapter() {
        if(sliderAdapter != null) {
            return sliderAdapter;
        }
        sliderAdapter = new ConnectFragment.SliderAdapter(this);
        return sliderAdapter;
    }

    /**
     * 恢复数据
     * @return
     * Added by Bao guangpu on 2016/4/8
     */
    private boolean restoreStateFromArguments() {
        Bundle b = getArguments();
        if(b != null) {
            savedState = b.getBundle("SaveAndRestore");
            if(savedState != null) {
                onRestoreState(savedState);
                return true;
            }
        }
        return false;
    }

    private void onRestoreState(Bundle savedInstanceState) {
        nLastDevPos = savedInstanceState.getInt("DeviceType");
        mStrDevType = savedInstanceState.getString("DeviceTypeName");
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
                b.putBundle("SaveAndRestore", savedState);
            }
        }
    }

    private Bundle saveState() {
        Bundle state = new Bundle();
        onSaveState(state);
        return state;
    }

    private void onSaveState(Bundle outState) {
        outState.putInt("DeviceType", nLastDevPos);
        outState.putString("DeviceTypeName", mStrDevType);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
        saveStateToArguments();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveStateToArguments();
    }
}
