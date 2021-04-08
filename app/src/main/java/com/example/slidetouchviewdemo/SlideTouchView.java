package com.example.slidetouchviewdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;



import com.nineoldandroids.view.ViewHelper;


public class SlideTouchView extends RelativeLayout {
    private static final String TAG = "CustomSlideToUnlockView";
    private static final long DEAFULT_DURATIN_LONG = 200;//左弹回,动画时长
    private static final long DEAFULT_DURATIN_SHORT = 200;//右弹,动画时长
    private static int DISTANCE_LIMIT = 600;//滑动阈值
    private static float THRESHOLD = 0.5F;//滑动阈值比例:默认是0.5,即滑动超过父容器宽度的一半再松手就会触发
    protected Context mContext;
    private ImageView iv_slide;//滑块
    private TextView tv_hint, tv_hints, tvTips;//提示文本
    private boolean mIsUnLocked;//已经滑到最右边,将不再响应touch事件
    private CallBack mCallBack;//回调
    private int slideImageViewWidth;//滑块宽度
    private int slideImageViewResId;//滑块资源
    private int slideImageViewResIdAfter;//滑动到右边时,滑块资源id
    private int viewBackgroundResId;//root 背景
    private String textHint;//文本
    private int textSize;//单位是sp,只拿数值
    private int textColorResId;//颜色,@color
    private int mActionDownX, mLastX, mSlidedDistance;

    public SlideTouchView(Context mContext) {
        super(mContext);
        this.mContext = mContext;
        initView();
    }

    public SlideTouchView(Context mContext, AttributeSet attrs) {
        super(mContext, attrs);
        this.mContext = mContext;
        TypedArray mTypedArray = mContext.obtainStyledAttributes(attrs, R.styleable.SlideToUnlockView);
        //获取自定义属性
        init(mTypedArray);
        initView();
    }

    public SlideTouchView(Context mContext, AttributeSet attrs, int defStyleAttr) {
        super(mContext, attrs, defStyleAttr);
        this.mContext = mContext;
        TypedArray mTypedArray = mContext.obtainStyledAttributes(attrs, R.styleable.SlideToUnlockView);
        init(mTypedArray);
        initView();
    }

    //获取自定义属性
    private void init(TypedArray mTypedArray) {
        slideImageViewWidth = (int) mTypedArray.getDimension(R.styleable.SlideToUnlockView_slideImageViewWidth, DensityUtil.dp2px(getContext(), 50));
        slideImageViewResId = mTypedArray.getResourceId(R.styleable.SlideToUnlockView_slideImageViewResId, -1);
        slideImageViewResIdAfter = mTypedArray.getResourceId(R.styleable.SlideToUnlockView_slideImageViewResIdAfter, -1);
        viewBackgroundResId = mTypedArray.getResourceId(R.styleable.SlideToUnlockView_viewBackgroundResId, -1);
        textHint = mTypedArray.getString(R.styleable.SlideToUnlockView_textHint);
        textSize = mTypedArray.getInteger(R.styleable.SlideToUnlockView_textSize, 7);
        textColorResId = mTypedArray.getColor(R.styleable.SlideToUnlockView_textColorResId, getResources().getColor(android.R.color.white));
        THRESHOLD = mTypedArray.getFloat(R.styleable.SlideToUnlockView_slideThreshold, 0.5f);
        mTypedArray.recycle();
    }

    /**
     * 初始化界面布局
     */
    @SuppressLint("ClickableViewAccessibility")
    protected void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.layout_view_slide, this, true);
        iv_slide = (ImageView) findViewById(R.id.iv_slide);
        tv_hint = (TextView) findViewById(R.id.tv_hint);
        tv_hints = (TextView) findViewById(R.id.tv_hints);
        tvTips = (TextView) findViewById(R.id.tv_tip);


        ViewGroup.LayoutParams params = iv_slide.getLayoutParams();
        //获取当前控件的布局对象
        params.width = slideImageViewWidth;//设置当前控件布局的高度
        params.height = params.width;
        iv_slide.setLayoutParams(params);//将设置好的布局参数应用到控件中

        setImageDefault();
        if (viewBackgroundResId > 0) {
            iv_slide.setBackgroundResource(viewBackgroundResId);//rootView设置背景
        }
        tv_hint.setTextSize(DensityUtil.sp2px(getContext(), textSize));
        tv_hint.setTextColor(textColorResId);
        tv_hint.setText(TextUtils.isEmpty(textHint) ? mContext.getString(R.string.hint) : textHint);

        tv_hints.setTextSize(DensityUtil.sp2px(getContext(), textSize));
        tv_hints.setTextColor(textColorResId);
        tv_hints.setText(TextUtils.isEmpty(textHint) ? mContext.getString(R.string.hint) : textHint);

        //添加滑动监听
        iv_slide.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                DISTANCE_LIMIT = (int) (SlideTouchView.this.getWidth() * THRESHOLD);//默认阈值是控件宽度的一半

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN://按下时记录纵坐标
                        if (mIsUnLocked) {//滑块已经在最右边则不处理touch
                            return false;
                        }
                        mLastX = (int) event.getRawX();//最后一个action时x值
                        mActionDownX = (int) event.getRawX();//按下的瞬间x
                        break;
                    case MotionEvent.ACTION_MOVE://上滑才处理,如果用户一开始就下滑,则过掉不处理
                        int dX = (int) event.getRawX() - mLastX;

                        mSlidedDistance = (int) event.getRawX() - mActionDownX;
                        Log.i(TAG, "mSlidedDistance=============================" + mSlidedDistance);
                        final MarginLayoutParams params = (MarginLayoutParams) v.getLayoutParams();
                        int left = params.leftMargin;
                        int top = params.topMargin;
                        int right = params.rightMargin;
                        int bottom = params.bottomMargin;

                        int leftNew = left + dX;
                        int rightNew = right - dX;

                        if (mSlidedDistance > 0) {//直接通过margin实现滑动
                            params.setMargins(leftNew, top, rightNew, bottom);
                            Log.i(TAG, leftNew + "=============================MOVE");
                            v.setLayoutParams(params);
                            resetTextViewAlpha(mSlidedDistance);

                            //回调
                            if (mCallBack != null) {
                                mCallBack.onSlide(mSlidedDistance);
                            }
                            mLastX = (int) event.getRawX();
                        } else {
                            return true;
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        Log.i(TAG, "MotionEvent.ACTION_UP,之前移动的偏移值：" + ViewHelper.getTranslationY(v));
                        if (Math.abs(mSlidedDistance) > DISTANCE_LIMIT) {
                            scrollToRight(v);//右边
                        } else {
                            scrollToLeft(v);//左边
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return true;
            }
        });

    }

    //重置提示文本的透明度
    private void resetTextViewAlpha(int distance) {
        if (Math.abs(distance) >= Math.abs(DISTANCE_LIMIT)) {
            tv_hint.setAlpha(0.0f);
            tv_hints.setAlpha((SlideTouchView.this.getWidth() * 1.0f) / (Math.abs(distance) * 1.0f));
        } else {
            tv_hint.setAlpha(1.0f - Math.abs(distance) * 1.0f / Math.abs(DISTANCE_LIMIT));
            tvTips.setAlpha(1.0f - Math.abs(distance) * 1.0f / Math.abs(DISTANCE_LIMIT));
        }
    }

    //滑动未到阈值时松开手指, 弹回到最左边
    private void scrollToLeft(final View v) {
        final MarginLayoutParams params1 = (MarginLayoutParams) v.getLayoutParams();
        ViewAnimator
                .animate(iv_slide)
                .translationX(ViewHelper.getTranslationX(v), -params1.leftMargin)
                .interpolator(new AccelerateInterpolator())
                .duration(DEAFULT_DURATIN_LONG)
                .onStop(new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        MarginLayoutParams para = (MarginLayoutParams) v.getLayoutParams();
                        mSlidedDistance = 0;
                        tv_hints.setAlpha(0.0f);
                        tv_hint.setAlpha(1.0f);
                        tvTips.setAlpha(1.0f);
                        mIsUnLocked = false;
                        if (mCallBack != null) {
                            mCallBack.onSlide(mSlidedDistance);
                        }
                        setImageDefault();
                    }
                }).start();
    }

    //滑动到右边,并触发回调

    private void scrollToRight(final View v) {
        final MarginLayoutParams params1 = (MarginLayoutParams) v.getLayoutParams();
        //移动到最右端  移动的距离是 父容器宽度-leftMargin
        ViewAnimator
                .animate(iv_slide)
                .translationX(ViewHelper.getTranslationX(v), (SlideTouchView.this.getWidth() - params1.leftMargin - slideImageViewWidth) - DensityUtil.dp2px(this.mContext, 5))//移动距离=view的宽度-(左间距-减去图片宽度)
                .interpolator(new AccelerateInterpolator())
                .duration(DEAFULT_DURATIN_SHORT)
                .onStop(new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        MarginLayoutParams para = (MarginLayoutParams) v.getLayoutParams();
                        mSlidedDistance = 0;
                        tv_hint.setAlpha(0.0f);
                        tvTips.setAlpha(0.0f);
                        tv_hints.setAlpha(1.0f);
                        mIsUnLocked = true;

                        if (slideImageViewResIdAfter > 0) {
                            iv_slide.setImageResource(slideImageViewResIdAfter);//滑块imagview设置资源
                        }
                        //回调
                        if (mCallBack != null) {
                            mCallBack.onSlideEnd();
                        }
                    }
                })
                .start();


    }

    //重置状态
    public void resetView() {
        mIsUnLocked = false;
        setImageDefault();
        scrollToLeft(iv_slide);
    }

    private void setImageDefault() {
        //设置默认图片
        if (slideImageViewResId > 0) {
            iv_slide.setImageResource(slideImageViewResId);//滑块imagview设置资源
        }
    }

    public interface CallBack {
        void onSlide(int distance);//右滑距离回调

        void onSlideEnd();//滑动到了右边,事件回调
    }


    public void setmCallBack(CallBack mCallBack) {
        this.mCallBack = mCallBack;
    }

    public void setRightText(String text) {
        if (tv_hint == null) return;
        tv_hint.setText(text);
    }

    public void setLeftText(String text) {
        if (tv_hints == null) return;
        tv_hints.setText(text);
    }
}