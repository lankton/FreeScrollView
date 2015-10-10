package cn.lankton.freescrollview;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

/**
 * Created by taofangxin on 15/10/8.
 */
public class FreeScrollView extends ScrollView {

    private Context mContext;
    private float mLastY;
    private float mLastY_2;

    private boolean someFingerOut = false;// 有指头抬起

    private boolean hasInited = false;

    private int footerOriginHeight = -1; //底部本来的大小


    private View header; //头部空间
    private LinearLayout footer; //底部空间
    private LinearLayout container; // 容器
    private View content; //真正的内容

    private final float DELTA_RATIO = 2; //数值越大, 头部空白下拉越慢. 值为1为完全跟手.

    public FreeScrollView(Context context) {
        super(context);
        mContext = context;
    }

    public FreeScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public FreeScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (true == someFingerOut) {
            mLastY = ev.getY();
            someFingerOut = false;
        }
        switch(ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getY();
                if(footerOriginHeight < 0)
                {
                    footerOriginHeight = footer.getHeight();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = ev.getY() - mLastY;
                mLastY = ev.getY();
                if (this.getScrollY() == 0
                        && (this.getScrollY() + this.getHeight() != container.getHeight() || deltaY > 0)) {
                    updateHeaderHeight((int) (deltaY / DELTA_RATIO));
                }

                else if (this.getScrollY() + this.getHeight() == container.getHeight()) {
                    updateFooterHeight((int) (deltaY / DELTA_RATIO));
                }

                break;
            default:
                if(1 == ev.getPointerCount()) {
                    //手指全部离开, scrollview弹回

                    if (this.getScrollY() < header.getHeight()) {
                        //下拉恢复头部状态
                        restoreHeader();
                    }

                    else if (footer.getHeight() > footerOriginHeight) {
                        //上提恢复底部状态
                        restoreFooter();
                    }
                }
                someFingerOut = true;
                break;

        }
        return super.onTouchEvent(ev);
    }

    //随拖动更改头部空间的大小
    public void updateHeaderHeight(int delta) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) header.getLayoutParams();
        int height = delta + header.getHeight();
        lp.height = height > 0 ? height : 0;
        header.setLayoutParams(lp);
        if(lp.height > 0) {
            this.scrollTo(0,0);
        }
    }

    //随拖动更改底部空间的大小
    public void updateFooterHeight(int delta) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) footer.getLayoutParams();
        int height = -delta + footer.getHeight();
        lp.height = height > 0 ? height : 0;
        footer.setLayoutParams(lp);
        if(lp.height > footerOriginHeight) {
            this.scrollTo(0, container.getHeight() - this.getHeight());
        }

    }

    public void restoreHeader() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) header.getLayoutParams();
        lp.height = 0;
        header.setLayoutParams(lp);
        this.freeSmoothScrollTo(0, 0);
    }

    public void restoreFooter() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) footer.getLayoutParams();
        lp.height = footerOriginHeight;
        footer.setLayoutParams(lp);
        this.freeSmoothScrollTo(0, container.getHeight());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (true == hasInited)
        {
            return;
        }
        //构造线性容器
        container = new LinearLayout(mContext);

        container.setOrientation(LinearLayout.VERTICAL);
        ScrollView.LayoutParams lpContainer = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT
                , LayoutParams.MATCH_PARENT);
        //添加头部空间
        header = new View(mContext);
        LinearLayout.LayoutParams lpHeader = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0);
        container.addView(header, lpHeader);

        //向容器添加xml中定义的内容, 并且从scrollview删除该内容
        View content = this.getChildAt(0);
        if(null != content) {
            this.removeView(content);
            LinearLayout.LayoutParams lpContent = new LinearLayout.LayoutParams(content.getLayoutParams().width
                    , content.getLayoutParams().height);
            container.addView(content, lpContent);
        }

        //添加底部空间
        footer = new LinearLayout(mContext);
        LinearLayout.LayoutParams lpfooter = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT);
        container.addView(footer, lpfooter);


        //将container添加到已经清空的scrollview
        this.addView(container, lpContainer);
        for(int index = 0; index < getChildCount(); index++){
            View c = getChildAt(index);
            c.layout(l, t, r, b);
        }
        for(int index = 0; index < container.getChildCount(); index++){
            View c = container.getChildAt(index);
            c.layout(l, t, r, b);
        }

        hasInited = true;
    }

    public void freeSmoothScrollTo(final int x, final int y) {
        this.post(new Runnable() {
            @Override
            public void run() {
                FreeScrollView.this.smoothScrollTo(x, y);
            }
        });
    }
}
