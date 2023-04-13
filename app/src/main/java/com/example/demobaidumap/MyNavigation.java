package com.example.demobaidumap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demobaidumap.databinding.ActivityMyNavigationBinding;

public class MyNavigation extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMyNavigationBinding binding;
    private SharedViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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