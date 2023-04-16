package com.example.demobaidumap;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.demobaidumap.fall.FallDetectionService;
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

import java.util.ArrayList;
import java.util.List;

public class MyNavigation extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMyNavigationBinding binding;
    private SharedViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 开始跌倒检测
        Intent intent = new Intent(this, FallDetectionService.class);
        startService(intent);


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
//                    requestLocation();
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