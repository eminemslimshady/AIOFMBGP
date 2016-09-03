package com.aiofm.eminem.aiofmbgp.views.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by eminem on 2016/1/25.
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * 绑定控件id
     */
    protected abstract void initControl();

    /**
     * 从持久化文件中初始化数据
     */
    protected abstract  void restoreForFirstLauch();

    /**
     * 从savedInstanceState中初始化数据
     */
    protected abstract void initData(Bundle bundle);

    /**
     *初始化控件
     */
    protected abstract void initView();

    /**
     * 设置监听器
     */
    protected abstract void setOnListener();
}
