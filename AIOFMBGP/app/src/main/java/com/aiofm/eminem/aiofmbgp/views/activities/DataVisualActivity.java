package com.aiofm.eminem.aiofmbgp.views.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.aiofm.eminem.aiofmbgp.R;
import com.aiofm.eminem.aiofmbgp.common.Utils;
import com.aiofm.eminem.aiofmbgp.views.controls.MyMarkerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataVisualActivity extends BaseActivity implements
        OnChartValueSelectedListener {
    private LineChart mChart;
    private Bundle recvBundle=new Bundle();
    private String strDevType;
    private String strDevName="";
    private String strIP;
    private int nPort;
    private boolean bSetStartDone=false;
    private boolean bSetEndDone=false;
    private boolean bCapData=true;
    private String strStartTime;
    private String strEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datavisual);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //设置自定义toolbar，不使用自带actionbar
        setSupportActionBar(toolbar);

        initControl();
        if(savedInstanceState==null){
            restoreForFirstLauch();
        }else{
            initData(savedInstanceState);
        }
        initView();
        //设置监听器
        setOnListener();

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("暂无数据");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        //创建一个自定义表及视图，指定其所使用的布局
        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        //设置自定义标记视图
        mChart.setMarkerView(mv);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // 设置标签 ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        //设置标签样式
        l.setForm(Legend.LegendForm.LINE);
        //设置标签字体
        l.setTypeface(tf);
        //设置标签文字颜色
        l.setTextColor(Color.WHITE);
        //设置标签文字大小
        l.setTextSize(10f);

        XAxis xl = mChart.getXAxis();
        xl.setTypeface(tf);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setSpaceBetweenLabels(5);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(tf);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaxValue(100f);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setStartAtZero(true);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        setTitle(strDevName);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "横屏模式", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "竖屏模式", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void initControl() {

    }

    @Override
    protected void restoreForFirstLauch() {
        Intent intent=getIntent();
        recvBundle=intent.getExtras();
        if(recvBundle!=null){
            strDevType=recvBundle.getString("deviceType");
            strDevName=recvBundle.getString("deviceName");
            strIP=recvBundle.getString("deviceIP");
            nPort=recvBundle.getInt("devicePort");
        }
    }

    @Override
    protected void initData(Bundle bundle) {
        if(bundle!=null){
            strDevType=bundle.getString("deviceType");
            strDevName=bundle.getString("deviceName");
            strIP=bundle.getString("deviceIP");
            nPort=bundle.getInt("devicePort");
        }
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void setOnListener() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.realtime, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.actionSuspend: {
                //mChart.clearValues();
                //关闭生成绘制数据子线程
                bCapData=false;
                Toast.makeText(this, "暂停", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.actionFeedMultiple: {
                //开启生成绘制数据子线程，并触发UI线程绘制
                bCapData=true;
                Toast.makeText(this, "绘制", Toast.LENGTH_SHORT).show();
                feedMultiple();
                break;
            }
            case R.id.actionSetStartPoint:{
                //设置保存起始点

                bSetStartDone=true;
                break;
            }
            case R.id.actionSetEndPoint:{
                //设置保存终止点

                bSetEndDone=true;
                break;
            }
            case R.id.actionSave:{
                //保存数据
                if(!bSetStartDone){
                    AlertDialog.Builder builder=new AlertDialog.Builder(this);
                    final AlertDialog dialog=builder.setTitle("提示").setMessage("请设置保存起始点").create();
                    //创建自动关闭任务
                    Runnable runnable=new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    };
                    ScheduledExecutorService executorService= Executors.newSingleThreadScheduledExecutor();
                    //新建一个调度任务，在两秒之后执行runnable中的代码
                    executorService.schedule(runnable,2000, TimeUnit.MILLISECONDS);
                    dialog.show();
                    bSetStartDone=false;
                    bSetEndDone=false;
                    break;
                }
                if(!bSetEndDone){
                    AlertDialog.Builder builder=new AlertDialog.Builder(this);
                    final AlertDialog dialog=builder.setTitle("提示").setMessage("请设置保存终止点").create();
                    //创建自动关闭任务
                    Runnable runnable=new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    };
                    ScheduledExecutorService executorService= Executors.newSingleThreadScheduledExecutor();
                    //新建一个调度任务，在两秒之后执行runnable中的代码
                    executorService.schedule(runnable,2000, TimeUnit.MILLISECONDS);
                    dialog.show();
                    bSetStartDone=false;
                    bSetEndDone=false;
                    break;
                }

                bSetStartDone=false;
                bSetEndDone=false;
                if(saveToDb()){
                    Toast.makeText(this, "保存数据到数据库成功", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "保存数据到数据库失败", Toast.LENGTH_SHORT).show();
                }
                break;
            }

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Utils.finish(this);
        super.onBackPressed();
    }

    /**
     * 生成一个绘制点以及其所对应横轴值，并完成触发数据改变消息、设置屏幕最多显示点个数、指定左边y轴的位置
     */
    private void addEntry() {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            // 添加点的横坐标值
            data.addXValue(Utils.getTime());
            //添加点的纵坐标值，暂时使用随机数
            data.addEntry(new Entry((float) (Math.random() * 40) + 30f, set.getEntryCount()), 0);

            // 通知折线图控件数据已经改变
            mChart.notifyDataSetChanged();

            // 屏幕中最多显示的点的个数
            mChart.setVisibleXRangeMaximum(15);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);
            //将左边的y轴放到指定的位置
            mChart.moveViewToX(data.getXValCount() - 16);
        }
    }

    /**
     * 生成LineDataSet，并依靠它设置绘制的线和点的样式等参数
     * @return
     */
    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "能见度表");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);    //设置点颜色
        set.setLineWidth(2f);   //设置线宽
        set.setCircleSize(4f);  //设置点的大小
        set.setFillAlpha(65);   //设置填充透明度c
        set.setFillColor(ColorTemplate.getHoloBlue());  //设置填充颜色
        set.setHighLightColor(Color.rgb(244, 117, 117)); //设置高亮的线的颜色
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(true);
        return set;
    }

    /**
     * 绘制子线程，生成绘制数据，并触发UI线程进行绘制
     */
    private void feedMultiple() {
        //在生成绘制数据子线程中更新数据，并触发UI线程进行绘制操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(bCapData) {
                    //一次绘制一条线加一个点，不使用Handler，而使用runOnUiThread，当需要更新UI时，将Runnable发送到UI线程消息队列中去
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //从大气测量设备端接收能见度值
                            Log.d("DataVisual","DataVisual");
                            addEntry();
                        }
                    });

                    try {
                        //休眠1s
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 保存数据到数据库中
     */
    private boolean saveToDb(){

        return true;
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("Entry selected", e.toString());
        //选中数据点

    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("deviceName", strDevName);
        outState.putString("deviceType", strDevType);
        outState.putString("devIP", strIP);
        outState.putInt("devPort",nPort);
    }

    @Override
    protected void onDestroy() {
        //关闭生成绘制数据子线程
        bCapData=false;
        super.onDestroy();
    }
}
