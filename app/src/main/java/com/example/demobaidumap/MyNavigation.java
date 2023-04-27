package com.example.demobaidumap;


import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demobaidumap.fall.FallDetectionService;
import com.example.demobaidumap.service.BackgroundLocationService;
import com.example.demobaidumap.service.GuardService;
import com.example.demobaidumap.service.JobWakeUpService;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demobaidumap.databinding.ActivityMyNavigationBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyNavigation extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMyNavigationBinding binding;
    private SharedViewModel sharedViewModel;

    TextView textview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 开始跌倒检测
        Intent intent = new Intent(this, FallDetectionService.class);
        startService(intent);

        // 零点自启，重设步数
//        selfStartingResetSteps();

        binding = ActivityMyNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMyNavigation.toolbar);

        View icon1 = findViewById(R.id.icon1);
        View icon2 = findViewById(R.id.icon2);

        ViewModelProvider provider = new ViewModelProvider(this);
        sharedViewModel = provider.get(SharedViewModel.class);

        binding.appBarMyNavigation.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            // fab点击事件
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Log.e("fab","click");

                boolean isShown = icon1.getVisibility() == View.VISIBLE;
                int translationX = getResources().getDimensionPixelSize(R.dimen.icon_translation_x);
                int translationY = getResources().getDimensionPixelSize(R.dimen.icon_translation_y);
                float rotation = isShown ? 0f : 45f;
                float alpha = isShown ? 0f : 1f;

                icon1.animate().translationX(isShown ? 0 : -translationX)
                        .alpha(alpha);
                icon2.animate().translationY(isShown ? 0 : -translationY)
                        .alpha(alpha);
                icon1.setVisibility(isShown ? View.GONE : View.VISIBLE);
                icon2.setVisibility(isShown ? View.GONE : View.VISIBLE);

                icon1.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ScrollingActivity.class);
                        startActivity(intent);

                        icon1.animate().translationX(translationX)
                                .alpha(0f);
                        icon2.animate().translationY(translationY)
                                .alpha(0f);
                        icon1.setVisibility(View.GONE);
                        icon2.setVisibility(View.GONE);

                    }
                });


            }
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // 指定了应用的主要目的地Fragment和DrawerLayout
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        // 获取NavController的实例，将在不同的目的地之间导航
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_my_navigation);
        // 正确显示当前目的地的标题
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        // 菜单项和导航目的地正确关联
        NavigationUI.setupWithNavController(navigationView, navController);


        // 获取导航标题：今日目标
        View headerLayout = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
        textview = headerLayout.findViewById(R.id.textView_step);
        Log.e("TextView",""+textview);

        String nowadays = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String today_assign_step = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()).getString("list_preference"+nowadays, "6000");

        Log.i("今日目标步数",""+today_assign_step);

        textview.setText("今日目标："+today_assign_step+"步！");


        /*
         * 这里是添加权限
         * */
        List<String> permissionList = new ArrayList<String>();
//        Context context = getContext().getApplicationContext();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACTIVITY_RECOGNITION);
        }
//        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED){
//            permissionList.add(Manifest.permission.SYSTEM_ALERT_WINDOW);
//        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.CALL_PHONE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.SEND_SMS);
        }
        if(!permissionList.isEmpty()){
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 1);
        }

        // 悬浮窗
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent2 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + this.getPackageName()));
            this.startActivity(intent2);
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences("Track",MODE_PRIVATE);
        Boolean isRoot = pref.getBoolean("isRoot",false);
        if(isRoot){
            startAllServices();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        String nowadays = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String today_assign_step = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()).getString("list_preference"+nowadays, "6000");

        Log.i("今日目标步数",""+today_assign_step);

        textview.setText("今日目标："+today_assign_step+"步！");
    }

    /**
     * 开启所有Service
     */
    private void startAllServices()
    {
        Intent bgIntent = new Intent(this, BackgroundLocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //android8.0以上通过startForegroundService启动service
            this.startForegroundService(bgIntent);
        } else {
            this. startService(bgIntent);
        }
        this.startService(new Intent(this, GuardService.class));
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP) {
            Log.d("start service", "startAllServices: ");
            //版本必须大于5.0
            this.startService(new Intent(this, JobWakeUpService.class));
        }
    }


    // 设置凌晨自启，用于更新步数
    public void selfStartingResetSteps(){
        // 创建Intent对象
        Intent intent = new Intent(this, MyNavigation.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        // 创建PendingIntent对象
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // 获取AlarmManager对象
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // 设置定时任务的触发时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long triggerTime =  calendar.getTimeInMillis();

        // 设置定时任务的重复间隔
        long interval = AlarmManager.INTERVAL_DAY;

        // 设置定时任务的类型为RTC_WAKEUP，即在指定时间唤醒设备
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, interval, pendingIntent);


    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("resCode","requestCode"+requestCode);
        switch (requestCode) {
            case 1:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        // 申请成功
                        Log.d("TAG", "["+permissions[i]+"]" + "ACTIVITY_RECOGNITION 申请成功");
                    } else {
                        // 申请失败
                        Log.d("TAG", "["+permissions[i]+"]" + "ACTIVITY_RECOGNITION 申请失败");
                    }
                }

                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    startAllServices();
                    // 存入一个标识，下次不走这一个判断权限后的服务，直接开启服务
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("Track",MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("isRoot",true);
                    editor.apply();

                }else{
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }


    // 返回共享数据实例
    public SharedViewModel getSharedViewModel(){
        return sharedViewModel;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
//                Toast.makeText(getApplicationContext(),"1",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_my_navigation);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}