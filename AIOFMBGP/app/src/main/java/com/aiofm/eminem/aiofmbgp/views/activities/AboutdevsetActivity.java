package com.aiofm.eminem.aiofmbgp.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.aiofm.eminem.aiofmbgp.R;

public class AboutdevsetActivity extends AppCompatActivity {
    private String strDevType;

    private TextView tv_devType;
    private TextView tv_aboutDevSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutdevset);
        initControl();
        initData();
        initView();
        //设置自定义toolbar
        setupToolbar();
    }

    private void initControl() {
        tv_devType=(TextView)findViewById(R.id.tv_devtype);
        tv_aboutDevSet=(TextView)findViewById(R.id.tv_aboutandor);
    }

    private void initData() {
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        strDevType=bundle.getString("devType");
    }

    private void initView() {
        tv_devType.setText(strDevType);
        tv_aboutDevSet.setText("关于"+strDevType+"设置,之后添加");
    }

    private void setupToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.abouttoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_abouttoolbar);
        collapsingToolbar.setTitle(getTitle());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //点击返回箭头
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
