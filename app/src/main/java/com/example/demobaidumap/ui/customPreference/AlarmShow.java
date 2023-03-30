package com.example.demobaidumap.ui.customPreference;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demobaidumap.R;
import com.example.demobaidumap.ui.components.UnlockView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmShow extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    private TextView mTimeDisplay;
    private Switch mSwitchCancel;
    private Button mButtonRemindLater;

    private UnlockView unlockView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置窗口背景为透明色
//        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        getWindow().setDimAmount(0.6f);


        // 设置布局为全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.alarm_show);

        // 获取控件的引用
        mButtonRemindLater = findViewById(R.id.button_remind_later);

        Intent intent = getIntent();
        // 获取传递过来的参数
        String title = intent.getStringExtra("title");

        Log.e("Alarm Show Title",""+title);

        // 设置标题
        TextView titleTextView = findViewById(R.id.alarm_title);
        titleTextView.setText(title);

        // 初始化UnlockView
        unlockView = findViewById(R.id.unlock_view);
        unlockView.setOnUnlockListener(new UnlockView.OnUnlockListener() {
            @Override
            public void onUnlock() {
                // 处理解锁事件
                Toast.makeText(AlarmShow.this, "解锁成功", Toast.LENGTH_SHORT).show();
                // 取消震动
                vibrator.cancel();
                // 关闭页面
                finish();
            }
        });

        // 播放音乐
//        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
//        mediaPlayer.setLooping(true);
//        mediaPlayer.start();

        // 震动
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            long[] pattern = {0, 1000, 1000};
            vibrator.vibrate(pattern, 0);
        }

        // 显示当前时间
        TextView currentTimeTextView = findViewById(R.id.current_time);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        currentTimeTextView.setText(currentTime);


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放资源
//        mediaPlayer.stop();
//        mediaPlayer.release();
        vibrator.cancel();
    }
}
