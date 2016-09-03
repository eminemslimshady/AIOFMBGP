package com.aiofm.eminem.aiofmbgp.views.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

import com.aiofm.eminem.aiofmbgp.views.fragments.ConnectFragment;

/**
 * Created by eminem on 2016/2/4.
 */
public class SliderListView extends ListView{
    //用户点击item视图
    private SliderView mFocusedItemView;

    float mX = 0;
    float mY = 0;
    private int mPosition = -1;
    boolean isSlider = false;

    public SliderListView(Context context) {
        super(context);
    }

    public SliderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SliderListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isSlider = false;
                mX = x;
                mY = y;
                //计算用户点击的是listview中的哪个item
                int position = pointToPosition((int) x, (int) y);
                if (mPosition != position) {
                    mPosition = position;
                    ConnectFragment.mClickedItem=mPosition;
                    //如果前后两次单击的位置不同，则将上一次操作的item复位
                    if (mFocusedItemView != null) {
                        //重置上一次操作的item
                        mFocusedItemView.reset();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mPosition != -1) {
                    if (Math.abs(mY - y) < 30 && Math.abs(mX - x) > 80) {
                        //获取当前可见的第一个item
                        int first = this.getFirstVisiblePosition();
                        //计算用户点击item在所有可见item中的位置
                        int index = mPosition - first;
                        //获取用户点击item的视图
                        mFocusedItemView = (SliderView) getChildAt(index);
                        mFocusedItemView.onTouchEvent(ev);
                        isSlider = true;
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isSlider) {
                    isSlider = false;
                    if (mFocusedItemView != null) {
                        //调整用户点击item，第二个参数为左移标志位
                        mFocusedItemView.adjust(mX - x > 0);
                        return true;
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }
}
