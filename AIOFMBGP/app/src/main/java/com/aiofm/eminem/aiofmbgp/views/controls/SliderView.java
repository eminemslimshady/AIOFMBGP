package com.aiofm.eminem.aiofmbgp.views.controls;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.aiofm.eminem.aiofmbgp.R;

/**
 * Created by eminem on 2016/2/6.
 */
public class SliderView extends LinearLayout {
    private static final String TAG = "SlideView";
    private static final int TAN = 2;
    private int mHolderWidth = 240;
    private float mLastX = 0;
    private float mLastY = 0;
    private Context mContext;
    private LinearLayout mViewContent;
    private Scroller mScroller;

    public SliderView(Context context, Resources resources) {
        super(context);
        initView();
    }

    public SliderView(Context context) {
        super(context);
        initView();
    }

    public SliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    /**
     * Added by Bao guangpu on 2016/2/10
     */
    private void initView() {
        setOrientation(LinearLayout.HORIZONTAL);
        mContext = getContext();
        mScroller = new Scroller(mContext);
        View.inflate(mContext, R.layout.slide_view_merge,this);
        //listitem会放在view_content中
        mViewContent = (LinearLayout) findViewById(R.id.view_content);
        mHolderWidth = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, mHolderWidth, getResources()
                        .getDisplayMetrics()));
    }

    /**
     * Added by Bao guangpu on 2016/2/10
     * @param view
     */
    public void setContentView(View view) {
        //为SliderView视图添加子View
        mViewContent.addView(view);
    }

    /**
     * Added by Bao guangpu on 2016/2/10.
     */
    public void shrink() {
        int offset = getScrollX();
        if (offset == 0) {
            return;
        }
        //快速移动到初始位置
        scrollTo(0, 0);
    }

    /**
     * Added by Bao guangpu on 2016/2/10.
     */
    public void reset() {
        int offset = getScrollX();
        if (offset == 0) {
            return;
        }
        //平滑移动到初始位置，调用了Scroller的startScroll方法
        smoothScrollTo(0);
    }

    /**
     * Added by Bao guangpu on 2016/2/10.
     * @param destX
     */
    private void smoothScrollTo(int destX) {
        int scrollX = getScrollX();
        int delta = destX - scrollX;
        mScroller.startScroll(scrollX, 0, delta, 0, Math.abs(delta) * 3);
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    /**
     * Added by Bao guangpu on 2016/2/9.
     * @param left 左划标志位
     */
    public void adjust(boolean left) {
        //获取偏移量
        int offset = getScrollX();
        if (offset == 0) {
            return;
        }
        if (offset < 35) {
            //如果偏移量小于35
            this.smoothScrollTo(0);
        } else if (offset < mHolderWidth - 20) {
            if (left) {
                //左移到相应的位置
                this.smoothScrollTo(mHolderWidth);
            } else {
                //复位
                this.smoothScrollTo(0);
            }
        } else {
            this.smoothScrollTo(mHolderWidth);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                float deltaX = x - mLastX;
                float delatY = y - mLastY;
                mLastX = x;
                mLastY = y;
                //如果动作太小则退出
                if (Math.abs(deltaX) < Math.abs(delatY) * TAN) {
                    break;
                }
                if (deltaX != 0) {
                    float newScrollX = getScrollX() - deltaX;
                    if (newScrollX < 0) {
                        newScrollX = 0;
                    } else if (newScrollX > mHolderWidth) {
                        newScrollX = mHolderWidth;
                    }
                    this.scrollTo((int) newScrollX, 0);
                }
                break;
        }
        return super.onTouchEvent(event);
    }
}
