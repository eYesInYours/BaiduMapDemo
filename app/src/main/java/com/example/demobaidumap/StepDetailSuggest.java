package com.example.demobaidumap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class StepDetailSuggest extends AppCompatActivity {

    TextView detail_nowadays;
    TextView weekT;
    String show_week_text;
    ProgressBar mProgressBar;
    TextView progress_text;     // 进度条步数
    TextView now_steps;         // 已走
    TextView assign_steps;      // 目标
    TextView diff_step_progress; // 还差多少步

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.step_detail_suggest);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        String selected_track_data = intent.getStringExtra("date");
        // 从1到7分别表示周日到周六
        String week_data = intent.getStringExtra("week");

        // 设置日期
        detail_nowadays = findViewById(R.id.nowadays);
        detail_nowadays.setText(selected_track_data.split("-")[0] + "年" +
                selected_track_data.split("-")[1] + "月" + selected_track_data.split("-")[2] + "日");
        // 设置星期
        weekT = findViewById(R.id.week);

        switch (week_data){
            case "1": show_week_text = "星期日";break;
            case "2": show_week_text = "星期一";break;
            case "3": show_week_text = "星期二";break;
            case "4": show_week_text = "星期三";break;
            case "5": show_week_text = "星期四";break;
            case "6": show_week_text = "星期五";break;
            case "7": show_week_text = "星期六";break;
        }
        weekT.setText(show_week_text);

        mProgressBar = findViewById(R.id.progressBar);
        String nowadays = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        // 目标步数
        String today_assign_step = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()).getString("list_preference"+selected_track_data, "6000");
        // 当日已走步数
        SharedPreferences prefs2 = getApplicationContext().getSharedPreferences("StepCounterPrefs", MODE_PRIVATE);
        String savedStepCount = prefs2.getString("stepCount"+selected_track_data, "");
        String doneSteps = savedStepCount.split("@")[1];

        Log.e("progress",""+Integer.parseInt(doneSteps) + "" + Integer.parseInt(today_assign_step));
        // 设置进度
        mProgressBar.setProgress(Integer.parseInt(doneSteps)/Integer.parseInt(today_assign_step));

        // 设置步数
        progress_text = findViewById(R.id.progress_text);
        progress_text.setText(doneSteps+"步");
        now_steps = findViewById(R.id.now_steps);
        now_steps.setText(doneSteps+"步");
        assign_steps = findViewById(R.id.assign_steps);
        assign_steps.setText(today_assign_step+"步");
        diff_step_progress = findViewById(R.id.diff_step_progress);
        diff_step_progress.setText("差 "+ (Integer.parseInt(today_assign_step) - Integer.parseInt(doneSteps)) +" 步就达成目标了！");

        DecimalFormat df = new DecimalFormat("#.#");

        // 将已走步数转换成消耗的能量
        // 假设平均体重为60kg，步长为0.6米，每走一步需要消耗0.05卡路里的热量，则：
        // 千卡和卡的关系 1千卡 == 1000卡
        Double engine = Integer.parseInt(doneSteps) * 0.05;     // 卡
        TextView list2_left2 = findViewById(R.id.list2_left2);
        list2_left2.setText(df.format(engine)+"卡路里");

        TextView list2_right1 = findViewById(R.id.list2_right1);    // 相当于食物热量;
        if(engine == 0.0){
            list2_right1.setText("");
        }
        Log.e("engine",""+engine);

        String f = getFoodByCalories(engine);
        Log.i("food",""+f);
        list2_right1.setText("相当于消耗"+f);

        // 行走距离
        TextView list3_left2 = findViewById(R.id.list3_left2);
        double distance = Double.parseDouble(doneSteps) * 0.6;   // 米
        list3_left2.setText(df.format(distance/1000) + "公里");

        // 相当于行走操场圈数
        TextView list3_right1 = findViewById(R.id.list3_right1);
        Double circleCount = distance / 400;    // 400米一圈
        list3_right1.setText("相当于走了"+df.format(circleCount)+"圈标准操场");

    }

    // 传入能量（卡路里），转换成相应的食物。需要将对应的视频降序排列，比如100卡路里，等于多少蔬菜加多少鸡蛋的热量
    // 参考食品：鸡蛋、白菜、1千克猪肉、500克米饭
    public static String getFoodByCalories(double calories) {
        String food = "";
        double caloriesLeft = calories;

        // 高热量食品
        if (caloriesLeft >= 1000) {
            int kgOfPork = (int)Math.ceil(caloriesLeft / 1000);  // 1千克猪肉
            caloriesLeft -= kgOfPork * 2500;    // 1千克猪肉的热量约为2500卡路里
            food += "1千克猪肉 x " + kgOfPork + " ";
        }
        if (caloriesLeft >= 500) {
            int gOfRice = (int)Math.ceil(caloriesLeft / 500);   // 500克米饭
            caloriesLeft -= gOfRice * 700;      // 500克米饭的热量约为700卡路里
            food += "500克米饭 x " + gOfRice + " ";
        }

        // 次高热量食品
        if (caloriesLeft >= 120) {
            int eggs = (int)Math.ceil(caloriesLeft / 120);      // 1个鸡蛋的热量约为120卡路里
            caloriesLeft -= eggs * 120;
            food += eggs + "个鸡蛋 ";
        }
        if (caloriesLeft >= 20) {
            int gOfCabbage = (int)Math.ceil(caloriesLeft / 20); // 100克白菜的热量约为20卡路里
            caloriesLeft -= gOfCabbage * 20;
            food += gOfCabbage + "克白菜";
        }

        return food;
    }




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // 返回键
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}