package com.example.slidetouchviewdemo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;

import com.example.slidetouchviewdemo.R;
import com.example.slidetouchviewdemo.SlideTouchView;

public class MainActivity extends AppCompatActivity {

    private SlideTouchView slide;
    private TextView tv_text,tv_plus;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slide=findViewById(R.id.slide);
        button=findViewById(R.id.button);
        tv_text= (TextView) findViewById(R.id.tv_text);
        tv_plus = (TextView) findViewById(R.id.tv_plus);
        //  initFragment();
        AnimationSet animationSet = new AnimationSet(false);

        TranslateAnimation translateAnimation = new TranslateAnimation(0,0,0,-500);
        translateAnimation.setDuration(800L);
        translateAnimation.setFillBefore(true);
        translateAnimation.setFillAfter(true);
        animationSet.addAnimation(translateAnimation);

        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(800L);
        alphaAnimation.setFillBefore(true);
        alphaAnimation.setFillAfter(true);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tv_plus.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        SlideTouchView.CallBack callBack=new SlideTouchView.CallBack() {
            @Override
            public void onSlide(int distance) {
                tv_text.setText("slide distance:"+distance);
                slide.setLeftText("5/8");
            }

            @Override
            public void onSlideEnd() {
                tv_plus.setVisibility(View.VISIBLE);
                tv_plus.startAnimation(animationSet);
                tv_text.setText("SlideEnd");
            }
        };
        slide.setmCallBack(callBack);
        button.setOnClickListener(this::click);
    }

    private void click(View view) {
        slide.resetView();
    }

}