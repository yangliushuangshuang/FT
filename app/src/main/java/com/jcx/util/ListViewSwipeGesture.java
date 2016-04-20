package com.jcx.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.*;

import com.jcx.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@SuppressWarnings("ConstantConditions")
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class ListViewSwipeGesture implements OnTouchListener {
    Activity activity;

    // Cached ViewConfiguration and system-wide constant values
    private int mSlop;//表示滑动的时候，手的移动要大于这个距离才开始移动控件
    private int mMinFlingVelocity;//最小的滑动速度
    private int mMaxFlingVelocity;//最大的滑动速度
    private long mAnimationTime;//显示动画的时间

    // Fixed properties
    private ListView mListView;

    //private DismissCallbacks mCallbacks;
    private int mViewWidth = 1;//item的宽度
    private int smallWidth = 1;
    private int largewidth = 1;
    private int textwidth = 1;//溢出文本的宽度
    private int textheight = 1;//滑出文本的高度

    // Transient properties
    private List<PendingDismissData> mPendingDismisses = new ArrayList<PendingDismissData>();//存储item及其位置
    private int mDismissAnimationRefCount = 0;
    private float mDownX;//手指按下的X坐标
    private float mDownY;//手指按下的Y坐标
    private boolean mSwiping;//滑动标志
    private VelocityTracker mVelocityTracker;//滑动速度检测器
    private int mDownPosition;
    private int temp_position, opened_position, stagged_position;
    private ViewGroup mDownView, old_mDownView;//管理选中item的容器
    private ViewGroup mDownView_parent;//记录滑动前的item视图
    private TextView mDownView_txt;//滑动之后在新加载的textview上显示的文本
    private boolean mPaused;//暂停标志
    public boolean moptionsDisplay = false;//操作响应的标志

    static TouchCallbacks tcallbacks;//接口，传递给外界

    //Intermediate Usages
    String TextColor = "#FFFFFF";      //#FF4444
    String RangeColor = "#FFD060";   //"#FFD060"
    String singleColor = "#FF4444";

    //Functional  Usages
    public String tv_color;          //Green
    public String tv_action;

    //Swipe Types
    public int SwipeType;//滑动的类型
    public static int Single = 1;
    public static int Item_swipe_firstInvoked = 2;//设置背景
    public static int Dismiss =	3;
    private float DeltaX;//横向差值
    private float DeltaY;//纵向差值

    /**
     * 构造方法
     *
     * @param listView
     * @param Callbacks 调用的接口
     * @param context   当前Activity
     */
    public ListViewSwipeGesture(ListView listView, TouchCallbacks Callbacks, Activity context) {
        ViewConfiguration vc = ViewConfiguration.get(listView.getContext());//包含了用来设置UI的超时、大小和距离的方法和标准的常量
        mSlop = vc.getScaledTouchSlop();//获得手指滑动的距离
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;//获得手指滑动时的最小速度
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();//获得手指滑动时的最大速度
        mListView = listView;
        activity = context;
        tcallbacks = Callbacks;//接受从接口返回的数据
        SwipeType = Item_swipe_firstInvoked;//设置滑动的类型是2

        GetResourcesValues();//获得背景颜色，字体等资源
    }

    /**
     * 接口：在Activity中设置相应的事件
     */
    public interface TouchCallbacks {
//        void FullSwipeListView(int position);

        void HalfSwipeListView(int position);

//        void OnClickListView(int position);

//        void LoadDataForScroll(int count);

//        void onDismiss(ListView listView, int[] reverseSortedPositions);
    }

    /**
     * 获得颜色，显示的文字
     */
    private void GetResourcesValues() {
        mAnimationTime = mListView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);//动画的时间
        tv_color = RangeColor;          //Green
        tv_action = activity.getResources().getString(R.string.item_swip_action);
    }

    /**
     * 设置mpause
     *
     * @param enabled 启动标志
     */
    public void setEnabled(boolean enabled) {
        mPaused = !enabled;
    }

    /**
     * 手指接触屏幕的监听
     *
     * @param view
     * @param event 手指在屏幕上的事件
     * @return
     */
    @SuppressLint("ResourceAsColor")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public boolean onTouch(final View view, MotionEvent event) {
        if (mViewWidth < 2) {
            mViewWidth = mListView.getWidth();//获得listview的宽度(屏幕的宽度)
            smallWidth = mViewWidth / 7;//设置最小的宽度是当前宽度的1/7
            textwidth = mViewWidth / 4;//设置文本的宽度是当前宽度的1/4
            largewidth = textwidth;//设置最大的宽度是文本的宽度，item宽度的1/4
        }

        int tempwidth = 0;
        if (SwipeType == 1) {//如果滑动的类型是1
            tempwidth = smallWidth;
        } else {
            tempwidth = textwidth / 2;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {//手指触摸屏幕时
                if (mPaused) {//处于阻塞
                    return false;
                }
                Rect rect = new Rect();//定义一个矩形
                int childCount = mListView.getChildCount();//获得当前屏幕可见的item的个数

                int[] listViewCoords = new int[2];//listview的坐标
                mListView.getLocationOnScreen(listViewCoords);//获得listview在屏幕上的坐标
                int x = (int) event.getRawX() - listViewCoords[0];//手指在屏幕上X坐标-listview的X坐标
                int y = (int) event.getRawY() - listViewCoords[1];//手指在屏幕上的Y坐标-listview的Y坐标
                ViewGroup child;//定义一个view的容器

                int count = 0;
                for (int i = 0; i < childCount; i++) {//将当前屏幕中的item都加入到viewgroup容器中
                    child = (ViewGroup) mListView.getChildAt(i);//获得当前能看到的第i个item
                    child.getHitRect(rect);//找到控件占据的矩形区域的矩形坐标
                    if (rect.contains(x, y)) {//如果手指点击的坐标在某个item的区域当中
                        mDownView_parent = child;//记录下该item视图
                        mDownView = (ViewGroup) child.findViewById(R.id.list_display_view_container);//绑定当前的item

                        if (mDownView_parent.getChildCount() == 1) {
                            textheight = mDownView_parent.getHeight();//获得当前item的高度
//                            if(SwipeType==Dismiss){//滑动的类型是dismiss
//                                tv_color =	singleColor;
////                                HalfDrawable	=	activity.getResources().getDrawable(R.drawable.content_discard);
//                            }
                            SetBackGroundforList();//把视图加载到item上
                        }

                        if (old_mDownView != null && mDownView != old_mDownView) {//如果上一个点击的item不为Null，并且 当前点击的item不是上一个点击的item
                            ResetListItem(old_mDownView);//将上一个item恢复到原样
                            old_mDownView = null;
                            return false;
                        }
                        break;
                    }
                    count++;
                }//获得点击的item

                if (mDownView != null) {
                    mDownX = event.getRawX();//获得点击的item的坐标
                    mDownY = event.getRawY();
                    mDownPosition = mListView.getPositionForView(mDownView);//获得选中item的位置

                    mVelocityTracker = VelocityTracker.obtain();//检索手指滑动的速度
                    mVelocityTracker.addMovement(event);//将event加入到velocityTracker中
                } else {
                    mDownView = null;
                }

                //mSwipeDetected = false;
                temp_position = mListView.pointToPosition((int) event.getX(), (int) event.getY());//依据触摸点的坐标计算出点击的是ListView的哪个Item
                view.onTouchEvent(event);
                return true;
            }

            case MotionEvent.ACTION_UP: {//当手指从屏幕上拿起时
                if (mVelocityTracker == null) {
                    break;
                }
                float deltaX = event.getRawX() - mDownX;
                //在OnItemClick事件中判断如果Deltax！=0则表示不是点击事件
                DeltaX = deltaX;
                mVelocityTracker.addMovement(event);//将事件添加到velocityTracker中
                mVelocityTracker.computeCurrentVelocity(1000); // 1000 by defaut but
                float velocityX = mVelocityTracker.getXVelocity();
                float absVelocityX = Math.abs(velocityX);//取绝对值
                float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());
                boolean swipe = false;
                boolean swipeRight = false;

                //如果滑动的类型是double，并且手指滑动的距离大于要加载的textview长度的1/2
                //如果滑动的类型是single，并且手指滑动的距离大于item宽度的1/7
                if (Math.abs(deltaX) > tempwidth) {//如果手指滑动大于text的一半，则出发更新item布局事件
                    swipe = true;
                    swipeRight = deltaX > 0;//是否是向右滑动

                } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity && absVelocityY < absVelocityX) {//如果手指滑动速度适当，且横向滑动速度大于纵向速度
                    // dismiss only if flinging in the same direction as dragging
                    swipe = (velocityX < 0) == (deltaX < 0);//如果是向右滑动
                    swipeRight = mVelocityTracker.getXVelocity() > 0;//如果是向右滑动
                }
                if (deltaX < 0 && swipe) {//如果是向左滑动并且滑动触发
                    mListView.setDrawSelectorOnTop(false);//将textview显示在item下面
                    // && Math.abs(DeltaY)<30
                    if (swipe && !swipeRight && deltaX <= -tempwidth && Math.abs(DeltaY) < 100) {//如果滑动触发，并且向左滑动，并且滑动的距离超过触发阀值，并且纵向的滑动距厘<100px
                        FullSwipeTrigger();//自动完成textview的显示
                    } else if (deltaX >= -textwidth && SwipeType == Item_swipe_firstInvoked) {//向左滑动
                        ResetListItem(mDownView);
                    } else {
                        ResetListItem(mDownView);
                    }
                } else if (deltaX != 0) {
                    ResetListItem(mDownView);
                }

                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownView = null;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                break;
            }

            //根据手势滑动方向滑出滑入
            case MotionEvent.ACTION_MOVE: {

                float deltaX = event.getRawX() - mDownX;
                float deltaY = event.getRawY() - mDownY;
                DeltaY = deltaY;//delta差值
                if (mVelocityTracker == null || mPaused || deltaX > 0) {
                    break;
                }

                mVelocityTracker.addMovement(event);
                // && Math.abs(deltaY)<30
                if (Math.abs(deltaX) > (mSlop * 5) && Math.abs(deltaY) < 50) {//如果手指横向滑动的距离大于阀值，并且纵向滑动的距离<50
                    mSwiping = true;
                    mListView.requestDisallowInterceptTouchEvent(true);

                    // Cancel ListView's touch (un-highlighting the item)
                    MotionEvent cancelEvent = MotionEvent.obtain(event);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mListView.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                } else if (Math.abs(deltaY) > 50) {
                    mSwiping = false;
                     ResetListItem(mDownView);
                }
                if (mSwiping && deltaX < 0) {
                    int width;
                    if(SwipeType==1){
                        width = textwidth;
                    }
                    else{
                        width	=	largewidth;
                    }

                    if (-deltaX < width) {
                        mDownView.setTranslationX(deltaX);
                        return false;
                    }
                    return false;
                } else if (mSwiping) {
                    ResetListItem(mDownView);
                }
                break;
            }
        }
        return false;
    }

    /**
     * 为当前选中的item设置左滑动弹出按钮后的布局
     */
    private void SetBackGroundforList() {
        mDownView_txt = new TextView(activity.getApplicationContext());
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);//为滑动弹出的按钮设置大小和显示位置
        mDownView_txt.setLayoutParams(lp1);
        mDownView_txt.setGravity(Gravity.CENTER_HORIZONTAL);
        mDownView_txt.setText(tv_action);
        mDownView_txt.setWidth(textwidth);
        mDownView_txt.setPadding(0, textheight / 4, 0, 0);
        mDownView_txt.setHeight(textheight);
        mDownView_txt.setBackgroundColor(Color.parseColor(tv_color));
        mDownView_txt.setTextColor(Color.parseColor(TextColor));
        mDownView_parent.addView(mDownView_txt, 0);//将文本添加到当前选中的item中

    }

    /**
     * 重置当前选中的item
     * @param tempView 当前的item
     */
    private void ResetListItem(View tempView) {

        tempView.animate().translationX(0).alpha(1f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                int count = mDownView_parent.getChildCount() - 1;
                for (int i = 0; i < count; i++) {
//                    View v=	mDownView_parent.getChildAt(i);
                    mDownView_parent.removeViewAt(i);//移除view--------------？
                }
                moptionsDisplay = false;
            }
        });
        stagged_position = -1;
        opened_position = -1;

    }

    /**
     * 触发自动完成textview显示
     */
    private void FullSwipeTrigger(){
        old_mDownView =	mDownView;
        int width;
        if(SwipeType==Single || SwipeType==Dismiss){
            width =	textwidth;
            if(SwipeType==Dismiss) {
                ++mDismissAnimationRefCount;
            }
        }
        else{
            width =	largewidth;
        }

        mDownView.animate().translationX(-width).setDuration(10).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                moptionsDisplay=true;

                stagged_position =	temp_position;//stage:舞台，平台
                mDownView_txt.setOnTouchListener(new touchClass());

            }
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
    }

    /**
     * 获得item之间的item长度
     */
    class PendingDismissData implements Comparable<PendingDismissData> {
        public int position;
        public View view;

        public PendingDismissData(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public int compareTo(PendingDismissData other) {
            // Sort by descending position
            return other.position - position;
        }
    }

    class touchClass implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
//            opened_position     =   mListView.getPositionForView((View) v.getParent());
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {//如果手指按下
                    if(SwipeType==Dismiss){
                        moptionsDisplay = false;
                        performDismiss(mDownView_parent,temp_position);
                    }else
                    if(old_mDownView!=null && mDownView!=old_mDownView){
                        ResetListItem(old_mDownView);
                        old_mDownView=null;
                        mDownView=null;
                    }
                    tcallbacks.HalfSwipeListView(temp_position);
                    return true;
                }
            }
            return false;
        }
    }
    private void performDismiss(final View dismissView, final int dismissPosition) {
        // Animate the dismissed list item to zero-height and fire the dismiss callback when
        // all dismissed list item animations have completed. This triggers layout on each animation
        // frame; in the future we may want to do something smarter and more performant.

        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
        final int originalHeight = dismissView.getHeight();

        ((ViewGroup)dismissView).getChildAt(1).animate().translationX(0).alpha(1f).setListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ((ViewGroup) dismissView).removeViewAt(0);
                ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0).setDuration(mAnimationTime);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        --mDismissAnimationRefCount;
                        if (mDismissAnimationRefCount == 0) {
                            // No active animations, process all pending dismisses.
                            // Sort by descending position
                            Collections.sort(mPendingDismisses);

                            int[] dismissPositions = new int[mPendingDismisses.size()];
                            for (int i = mPendingDismisses.size() - 1; i >= 0; i--) {
                                dismissPositions[i] = mPendingDismisses.get(i).position;
                            }
//                            tcallbacks.onDismiss(mListView, dismissPositions);
//                            ViewGroup.LayoutParams lp;
//                          for (PendingDismissData pendingDismiss : mPendingDismisses) {
//                              // Reset view presentation
//                              lp = pendingDismiss.view.getLayoutParams();
//                              lp.height = originalHeight;
//                              pendingDismiss.view.setLayoutParams(lp);
//                          }
                            mPendingDismisses.clear();
                        }
                    }
                });

                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        lp.height = (Integer) valueAnimator.getAnimatedValue();
                        dismissView.setLayoutParams(lp);
                    }
                });

                mPendingDismisses.add(new PendingDismissData(dismissPosition, dismissView));
                animator.start();
            }
        });
    }
}
