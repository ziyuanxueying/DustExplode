package com.dustexplode;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.dustexplode.factory.ParticleFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Administrator on 2015/11/28 0028.
 */
public class ExplosionField extends View{
    private static final String TAG = "ExplosionField";
    private ArrayList<ExplosionAnimator> explosionAnimators;
    private HashMap<View,ExplosionAnimator> explosionAnimatorsMap;
    private View.OnClickListener onClickListener;
    private ParticleFactory mParticleFactory;
    
    public ExplosionField(Context context,ParticleFactory particleFactory) {
        super(context);
        init(particleFactory);
    }

    public ExplosionField(Context context, AttributeSet attrs,ParticleFactory particleFactory) {
        super(context, attrs);
        init(particleFactory);
    }

    private void init(ParticleFactory particleFactory) {
        explosionAnimators = new ArrayList<ExplosionAnimator>();
        explosionAnimatorsMap = new HashMap<View,ExplosionAnimator>();
        mParticleFactory = particleFactory;
        attach2Activity((Activity) getContext());
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (ExplosionAnimator animator : explosionAnimators) {
            animator.draw(canvas);
        }
    }

    /**
     * 鐖嗙牬
     * @param view 浣垮緱璇iew鐖嗙牬
     */
    public void explode(final View view) {
        //闃叉閲嶅鐐瑰嚮
        if(explosionAnimatorsMap.get(view)!=null&&explosionAnimatorsMap.get(view).isStarted()){
            return;
        }
        if(view.getVisibility()!=View.VISIBLE||view.getAlpha()==0){
            return;
        }
        
        final Rect rect = new Rect();
        view.getGlobalVisibleRect(rect); //寰楀埌view鐩稿浜庢暣涓睆骞曠殑鍧愭爣
        int contentTop = ((ViewGroup)getParent()).getTop();
        Rect frame = new Rect();
        ((Activity) getContext()).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        rect.offset(0, -contentTop - statusBarHeight);//鍘绘帀鐘舵�佹爮楂樺害鍜屾爣棰樻爮楂樺害
        if(rect.width()==0||rect.height()==0){
            return;
        }
        
        //闇囧姩鍔ㄧ敾
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(150);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            Random random = new Random();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setTranslationX((random.nextFloat() - 0.5f) * view.getWidth() * 0.05f);
                view.setTranslationY((random.nextFloat() - 0.5f) * view.getHeight() * 0.05f);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                explode(view, rect);
            }
        });
        animator.start();
    }

    private void explode(final View view,Rect rect) {
        final ExplosionAnimator animator = new ExplosionAnimator(this, Utils.createBitmapFromView(view), rect,mParticleFactory);
        explosionAnimators.add(animator);
        explosionAnimatorsMap.put(view, animator);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                //缂╁皬,閫忔槑鍔ㄧ敾
                view.animate().setDuration(150).scaleX(0f).scaleY(0f).alpha(0f).start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(150).start();

                //鍔ㄧ敾缁撴潫鏃朵粠鍔ㄧ敾闆嗕腑绉婚櫎
                explosionAnimators.remove(animation);
                explosionAnimatorsMap.remove(view);
                animation = null;
            }
        });
        animator.start();
    }
    
    /**
     * 缁橝ctivity鍔犱笂鍏ㄥ睆瑕嗙洊鐨凟xplosionField
     */
    private void attach2Activity(Activity activity) {
        ViewGroup rootView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rootView.addView(this, lp);
    }
    

    /**
     * 甯屾湜璋佹湁鐮寸鏁堟灉锛屽氨缁欒皝鍔燣istener
     * @param view 鍙互鏄疺iewGroup
     */
    public void addListener(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int count = viewGroup.getChildCount();
            for (int i = 0 ; i < count; i++) {
                addListener(viewGroup.getChildAt(i));
            }
        } else {
            view.setClickable(true);
            view.setOnClickListener(getOnClickListener());
        }
    }


    private OnClickListener getOnClickListener() {
        if (null == onClickListener) {
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExplosionField.this.explode(v);
                }
            };
        }
        return onClickListener;
    }
}
