package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    // 保存Fragement实例引用
    private HomeFragment homeFragment;
    private MineFragment mineFragment;

    private Fragment currentFragment;

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
    // 切换到首页Fragment
    private void switchToHomeFragment() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 如果homeFragment不存在，则创建
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
            transaction.add(R.id.fragment_container, homeFragment, "HomeFragment");
        }

        // 隐藏当前Fragment
        if (currentFragment != null && currentFragment != homeFragment) {
            transaction.hide(currentFragment);
        }

        // 显示首页Fragment
        if (homeFragment != null) {
            transaction.show(homeFragment);
        }

        transaction.commit();
        currentFragment = homeFragment;
    }

    // 切换到我的Fragment
    private void switchToMineFragment() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 如果mineFragment不存在，则创建
        if (mineFragment == null) {
            mineFragment = new MineFragment();
            transaction.add(R.id.fragment_container, mineFragment, "MineFragment");
        }

        // 隐藏当前Fragment
        if (currentFragment != null && currentFragment != mineFragment) {
            transaction.hide(currentFragment);
        }

        // 显示我的Fragment
        if (mineFragment != null) {
            transaction.show(mineFragment);
        }

        transaction.commit();
        currentFragment = mineFragment;
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

    private void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void setTabClickListener() {
        // 设置首页点击事件
        tabHome.setOnClickListener(v -> {
            switchToHomeFragment();
            updateTabStates(tabHome);
        });

        // 设置我的标签点击事件
        tabMine.setOnClickListener(v -> {
            switchToMineFragment();
            updateTabStates(tabMine);
        });

        // 初始状态设置首页为选中状态
        updateTabStates(tabHome);
    }

    // 更新导航栏标签状态的方法
    private void updateTabStates(LinearLayout selectedTab) {
        // 重置所有标签的状态
        resetTabState(tabHome);
        resetTabState(tabFriends);
        resetTabState(tabCamera);
        resetTabState(tabMessage);
        resetTabState(tabMine);

        // 设置选中标签的状态
        if (selectedTab != null) {
            // 确保标签可点击
            selectedTab.setEnabled(true);
            selectedTab.setAlpha(1.0f);

            // 设置选中标签的文字颜色
            if (selectedTab.getChildAt(0) instanceof TextView) {
                TextView textView = (TextView) selectedTab.getChildAt(0);
                textView.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }


    // 重置单个标签状态的方法
    private void resetTabState(LinearLayout tab) {
        if (tab != null) {
            // 对于已启用的标签（首页和我的），设置为非选中状态
            if (tab.getId() == R.id.tab_home || tab.getId() == R.id.tab_mine) {
                tab.setAlpha(0.6f);
                if (tab.getChildAt(0) instanceof TextView) {
                    TextView textView = (TextView) tab.getChildAt(0);
                    textView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                }
            }
            // 其他禁用的标签保持原有状态
        }
    }

    // 添加控制底部导航栏显示和隐藏的方法
    public void showBottomNavigation(boolean show) {
        if (bottomNavigation != null) {
            bottomNavigation.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}