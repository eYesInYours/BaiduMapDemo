package com.example.demobaidumap.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class UnlockView extends View {

    private float startX, endX;
    private int width, height;
    private boolean unlocked = false;
    private Paint paint;
    private OnUnlockListener listener;

    private float mCircleRadius;
    private float mCirclePosition;

    // 设置默认背景色为灰色透明
    private int mBackgroundColor = Color.parseColor("#88000000");

    public UnlockView(Context context) {
        super(context);
        init();
    }

    public UnlockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UnlockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化画笔
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStrokeWidth(5);

        //
        this.setBackgroundColor(Color.TRANSPARENT);

        // 计算圆的半径
        mCircleRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制背景
        paint.setColor(mBackgroundColor);
        canvas.drawRect(0, 0, width, height, paint);

        // 绘制解锁文字
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);
        String unlockText = "向右滑动解锁";
        canvas.drawText(unlockText, width/2 - paint.measureText(unlockText)/2, height/2, paint);

        // 绘制圆
        paint.setColor(Color.BLUE);
        Log.e("startX",""+startX);
        Log.e("mCircleRadius",""+mCircleRadius);
        Log.e("height",""+height);
        canvas.drawCircle(mCirclePosition, height/2, mCircleRadius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                // 更新圆的当前位置
                mCirclePosition = height;
                break;
            case MotionEvent.ACTION_MOVE:
                endX = event.getX();
                mCirclePosition = endX;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (endX - startX >= width * 3 / 5 ) {
                    if(endX > width){
                        mCirclePosition = endX;
                    }
                    unlocked = true;
                    if (listener != null) {
                        listener.onUnlock();
                    }
                } else {
                    unlocked = false;
                    // 回到原来位置
                    mCirclePosition = height / 2;
                }
                endX = 0;
                startX = 0;
                invalidate();
                break;
        }
        return true;
    }

    public interface OnUnlockListener {
        void onUnlock();
    }

    public void setOnUnlockListener(OnUnlockListener listener) {
        this.listener = listener;
    }

}
