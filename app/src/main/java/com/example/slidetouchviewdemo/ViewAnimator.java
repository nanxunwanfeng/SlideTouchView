package com.example.slidetouchviewdemo;

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;

import java.util.ArrayList;
import java.util.List;

/**
 * 类描述：@class describe
 * 创建人 :XB
 * 创建时间 2021/4/8 13:06
 * 修改人：
 * 修改时间：
 * 修改备注：
 */
public class ViewAnimator {
    List<AnimationBuilder> animationList = new ArrayList<>();
    Long duration = null;
    Long startDelay = null;
    Interpolator interpolator = null;

    AnimatorSet animatorSet;
    View waitForThisViewHeight = null;

    AnimationListener.Start startListener;
    AnimationListener.Stop stopListener;

   ViewAnimator prev = null;
   ViewAnimator next = null;

    public static AnimationBuilder animate(View...view) {
       ViewAnimator viewAnimator = new ViewAnimator();
        return viewAnimator.addAnimationBuilder(view);
    }

    public AnimationBuilder thenAnimate(View...views) {
       ViewAnimator nextViewAnimator = new ViewAnimator();
        this.next = nextViewAnimator;
        nextViewAnimator.prev = this;
        return nextViewAnimator.addAnimationBuilder(views);
    }

    public AnimationBuilder addAnimationBuilder(View...views) {
        AnimationBuilder animationBuilder = new AnimationBuilder(this, views);
        animationList.add(animationBuilder);
        return animationBuilder;
    }

    protected AnimatorSet createAnimatorSet() {
        List<Animator> animators = new ArrayList<>();
        for (AnimationBuilder animationBuilder : animationList) {
            animators.addAll(animationBuilder.createAnimators());
        }

        for (AnimationBuilder animationBuilder : animationList) {
            if (animationBuilder.isWaitForHeight()) {
                waitForThisViewHeight = animationBuilder.getView();
                break;
            }
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);

        if (duration != null)
            animatorSet.setDuration(duration);
        if (startDelay != null)
            animatorSet.setDuration(startDelay);
        if (interpolator != null)
            animatorSet.setInterpolator(interpolator);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {
                if (startListener != null) startListener.onStart();
            }

            @Override public void onAnimationEnd(Animator animation) {
                if (stopListener != null) stopListener.onStop();
                if (next != null) {
                    next.prev = null;
                    next.start();
                }
            }

            @Override public void onAnimationCancel(Animator animation) {

            }

            @Override public void onAnimationRepeat(Animator animation) {

            }
        });

        return animatorSet;
    }

    public ViewAnimator start() {
        if (prev != null)
            prev.start();
        else {
            animatorSet = createAnimatorSet();

            if (waitForThisViewHeight != null)
                waitForThisViewHeight.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override public boolean onPreDraw() {
                        animatorSet.start();
                        waitForThisViewHeight.getViewTreeObserver().removeOnPreDrawListener(this);
                        return false;
                    }
                });
            else
                animatorSet.start();
        }
        return this;
    }

    public void cancel() {
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        if (next != null) {
            next.cancel();
            next = null;
        }
    }

    public ViewAnimator duration(long duration) {
        this.duration = duration;
        return this;
    }

    public ViewAnimator startDelay(long startDelay) {
        this.startDelay = startDelay;
        return this;
    }

    //region callbacks

    public ViewAnimator onStart(AnimationListener.Start startListener) {
        this.startListener = startListener;
        return this;
    }

    public ViewAnimator onStop(AnimationListener.Stop stopListener) {
        this.stopListener = stopListener;
        return this;
    }

    //endregion

    //region interpolator

    public ViewAnimator interpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
        return this;
    }

    //endregion
}
