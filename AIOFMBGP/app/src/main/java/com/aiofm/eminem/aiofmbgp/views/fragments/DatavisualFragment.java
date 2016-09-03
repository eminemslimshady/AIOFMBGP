/**
 * 数据可视化Fragment
 * Created by Bao guangpu  on 2016/1/20.
 */
package com.aiofm.eminem.aiofmbgp.views.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.aiofm.eminem.aiofmbgp.R;
import com.aiofm.eminem.aiofmbgp.common.Utils;
import com.aiofm.eminem.aiofmbgp.views.activities.DataVisualActivity;
import com.aiofm.eminem.aiofmbgp.views.activities.DevconsetActivity;
import com.aiofm.eminem.aiofmbgp.views.controls.PullToRefreshListView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DatavisualFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DatavisualFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DatavisualFragment extends Fragment implements AdapterView.OnItemClickListener,PullToRefreshListView.PRListViewListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "DatavisualFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String strDevType;

    private Handler mHandler;
    private int mIndex = 0;
    private int mRefreshIndex = 0;

    Bundle devBundle=new Bundle();

    private View datavisual_Layout;
    private PullToRefreshListView devOnCapture;
    private PullToRefreshAdapter pullToRefreshAdapter;
    private TextView tv_sbxhsm;

    private static int nAndorVisualOpenTime=0;
    private static int nMvcVisualOpenTime=0;
    private int nLastClickItem=0;

    private OnFragmentInteractionListener mListener;

    public DatavisualFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DatavisualFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DatavisualFragment newInstance(String param1, String param2) {
        DatavisualFragment fragment = new DatavisualFragment();
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
        datavisual_Layout=inflater.inflate(R.layout.fragment_datavisual, container, false);
        return datavisual_Layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initControls(datavisual_Layout);
        initData();
        initView();
        setListener();
    }

    private void initControls(View view) {
        tv_sbxhsm=(TextView)datavisual_Layout.findViewById(R.id.tv_sbxhsm);
        devOnCapture=(PullToRefreshListView)datavisual_Layout.findViewById(R.id.sbxhInfo);
        devOnCapture.setPullRefreshEnable(true);
        devOnCapture.setXListViewListener(this);
        devOnCapture.setRefreshTime(Utils.getTime());
        devOnCapture.setPullLoadEnable(true);
        devOnCapture.setAutoLoadEnable(true);
    }

    private void initData() {
        devBundle=getArguments();
        if(devBundle==null){
            return;
        }
        strDevType=devBundle.getString("deviceType");
        if(strDevType.equals("1型大气测量设备")){
            nAndorVisualOpenTime++;
        }else if(strDevType.equals("2型大气测量设备")){
            nMvcVisualOpenTime++;
        }
    }

    private void initView() {
        mHandler = new Handler();
        if(strDevType.equals("1型大气测量设备")){
            tv_sbxhsm.setText("正在采集1型大气测量设备：");
        }else if(strDevType.equals("2型大气测量设备")){
            tv_sbxhsm.setText("正在采集2型大气测量设备：");
        }
        pullToRefreshAdapter=new PullToRefreshAdapter(this);
        devOnCapture.setAdapter(pullToRefreshAdapter);
        pullToRefreshAdapter.notifyDataSetChanged();
    }

    private void setListener() {
        devOnCapture.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        devOnCapture.autoRefresh();
        pullToRefreshAdapter.notifyDataSetChanged();
        super.onResume();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIndex = ++mRefreshIndex;
                pullToRefreshAdapter.notifyDataSetChanged();
                onLoad();
            }
        },2000);
    }

    @Override
    public void onLoadMore() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onLoad();
            }
        }, 2500);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //跳转
        Intent intent = new Intent();
        DevsetFragment.ConDevInfo item=null;
        if(strDevType=="1型大气测量设备"){
            intent.putExtra("deviceType","1型大气测量设备");
            item=DevsetFragment.mAndorOnCap.get(position-1);
        }else if(strDevType=="2型大气测量设备"){
            intent.putExtra("deviceType","2型大气测量设备");
            item=DevsetFragment.mMvcOnCap.get(position-1);
        }
        if(item!=null){
            intent.putExtra("deviceName",item.strDeviceName);
            intent.putExtra("deviceIP",item.strDeviceIP);
            intent.putExtra("devicePort",item.nDevicePort);
        }
        Utils.start_Activity(getActivity(), DataVisualActivity.class, intent);
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

    public class PullToRefreshAdapter extends BaseAdapter{
        private LayoutInflater mInflater;

        private PullToRefreshAdapter(DatavisualFragment p1) {
            mInflater = p1.getActivity().getLayoutInflater();
        }

        @Override
        public int getCount() {
            int nCount=0;
            if(strDevType.equals("1型大气测量设备")){
                nCount=DevsetFragment.mAndorOnCap.size();
            }else if(strDevType.equals("2型大气测量设备")){
                nCount=DevsetFragment.mMvcOnCap.size();
            }
            return nCount;
        }

        @Override
        public Object getItem(int position) {
            DevsetFragment.ConDevInfo item=null;
            if(strDevType.equals("1型大气测量设备")){
                item=DevsetFragment.mAndorOnCap.get(position);
            }else if(strDevType.equals("2型大气测量设备")){
                item=DevsetFragment.mMvcOnCap.get(position);
            }
            return item;
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
            if(DatavisualFragment.this.getActivity()==null){
                Log.d(TAG,"获取activity失败");
                return null;
            }
            DevsetFragment.ConDevInfo item=null;
            if(strDevType.equals("1型大气测量设备")){
                item= DevsetFragment.mAndorOnCap.get(position);
            }else if(strDevType.equals("2型大气测量设备")){
                item = DevsetFragment.mMvcOnCap.get(position);
            }
            DatavisualFragment.ViewHolder holder=null;
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.listitem1,null);
                holder=new DatavisualFragment.ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (DatavisualFragment.ViewHolder)convertView.getTag();
            }
            if(item!=null){
                holder.tv_DeviceId.setText(Integer.toString(item.deviceId));
                holder.tv_DeviceName.setText(item.strDeviceName);
                holder.tv_DeviceType.setText(item.strDeviceType);
            }
            return convertView;
        }

        /**
         * 获取指定item视图
         * @param position
         * @param listView
         * @return
         * Added by Bao guangpu on 2016/4/8
         */
        public View getItemView(int position, PullToRefreshListView listView) {
            int visibleFirstPos = listView.getFirstVisiblePosition();
            int visibleLastPos = listView.getLastVisiblePosition();
            if((position >= visibleFirstPos) && (position <= visibleLastPos)) {
                return listView.getChildAt((position - visibleFirstPos));
            }
            return null;
        }
    }

    public class ViewHolder{
        public ImageView iv_Icon;
        public TextView tv_DeviceId;
        public TextView tv_DeviceName;
        public TextView tv_DeviceType;

        ViewHolder(View view){
            iv_Icon=(ImageView)view.findViewById(R.id.image_icon1);
            tv_DeviceId=(TextView)view.findViewById(R.id.tv_deviceid1);
            tv_DeviceName=(TextView)view.findViewById(R.id.tv_devicename1);
            tv_DeviceType=(TextView)view.findViewById(R.id.tv_devicetype1);
        }
    }

    private void onLoad() {
        devOnCapture.stopRefresh();
        devOnCapture.setRefreshTime(Utils.getTime());
    }


}
