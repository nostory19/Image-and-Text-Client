package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {
    private LinearLayout tabHome, tabFriends, tabCamera, tabMessage, tabMine;
    private FragmentManager fragmentManager;

    private LinearLayout bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化组件
        initViews();

        // 初始化FragmentManager
        fragmentManager = getSupportFragmentManager();

        // 默认显示首页
        switchFragment(new HomeFragment());

        // 设置底部导航栏点击事件
        setTabClickListener();
    }

    private void initViews() {
        tabHome = findViewById(R.id.tab_home);
        tabFriends = findViewById(R.id.tab_friends);
        tabCamera = findViewById(R.id.tab_camera);
        tabMessage = findViewById(R.id.tab_message);
        tabMine = findViewById(R.id.tab_mine);
        // 初始化底部导航栏引用
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    private void switchFragment(Fragment fragment){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void setTabClickListener() {
        tabHome.setOnClickListener(v -> switchFragment(new HomeFragment()));
//        tabFriends.setOnClickListener(v -> switchFragment(new FriendsFragment()));
//        tabCamera.setOnClickListener(v -> switchFragment(new CameraFragment()));
//        tabMessage.setOnClickListener(v -> switchFragment(new MessageFragment()));
//        tabMine.setOnClickListener(v -> switchFragment(new MineFragment()));
    }

    // 添加控制底部导航栏显示和隐藏的方法
    public void showBottomNavigation(boolean show) {
        if (bottomNavigation != null) {
            bottomNavigation.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}