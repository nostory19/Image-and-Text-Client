package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.myapplication.viewmodel.MineViewModel;

/**
 * 处理UI展示盒用户交互
 */
public class MineFragment extends Fragment {
    private MineViewModel viewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView userAvatar;
    private TextView userNickname;
    private TextView userBio;
    private TextView followingCount;
    private TextView followersCount;
    private TextView likesCount;
    private View loadingView;

    // 四个内容项标题
    private TextView tabContent;
    private TextView tabLocations;
    private TextView tabLikes;
    private TextView tabCollections;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(MineViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);

        // 初始化视图
        initViews(view);

        // 设置观察者
        setupObservers();

        // 设置下拉刷新
        setupRefresh();

        // 设置内容项点击事件
        setupTabListeners();

        return view;
    }

    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        userAvatar = view.findViewById(R.id.user_avatar);
        userNickname = view.findViewById(R.id.user_nickname);
        userBio = view.findViewById(R.id.user_bio);
        followingCount = view.findViewById(R.id.following_count);
        followersCount = view.findViewById(R.id.followers_count);
        likesCount = view.findViewById(R.id.likes_count);
        loadingView = view.findViewById(R.id.loading_view);

        // 初始化内容项标题
        tabContent = view.findViewById(R.id.tab_content);
        tabLocations = view.findViewById(R.id.tab_locations);
        tabLikes = view.findViewById(R.id.tab_likes);
        tabCollections = view.findViewById(R.id.tab_collections);
    }

    private void setupObservers() {
        // 观察加载状态
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                loadingView.setVisibility(View.VISIBLE);
            } else {
                loadingView.setVisibility(View.GONE);
            }
        });

        // 观察用户头像
        viewModel.getUserAvatar().observe(getViewLifecycleOwner(), avatarUrl -> {
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(this)
                        .load(avatarUrl)
                        .circleCrop()
                        .into(userAvatar);
            }
        });

        // 观察用户昵称
        viewModel.getUserNickname().observe(getViewLifecycleOwner(), nickname -> {
            userNickname.setText(nickname);
        });

        // 观察用户个性签名
        viewModel.getUserBio().observe(getViewLifecycleOwner(), bio -> {
            userBio.setText(bio);
        });

        // 观察统计数据
        viewModel.getFollowingCount().observe(getViewLifecycleOwner(), count -> {
            followingCount.setText(String.valueOf(count));
        });

        viewModel.getFollowersCount().observe(getViewLifecycleOwner(), count -> {
            followersCount.setText(String.valueOf(count));
        });

        viewModel.getLikesCount().observe(getViewLifecycleOwner(), count -> {
            likesCount.setText(String.valueOf(count));
        });
    }

    private void setupRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshUserData();
            // 隐藏刷新动画
            swipeRefreshLayout.setRefreshing(false);
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
    }

    private void setupTabListeners() {
        // 设置内容项点击事件
        tabContent.setOnClickListener(v -> {
            // 切换到内容标签的逻辑
            highlightSelectedTab(tabContent);
            // 这里可以添加加载内容的逻辑
        });

        tabLocations.setOnClickListener(v -> {
            highlightSelectedTab(tabLocations);
            // 这里可以添加加载机位的逻辑
        });

        tabLikes.setOnClickListener(v -> {
            highlightSelectedTab(tabLikes);
            // 这里可以添加加载喜欢的逻辑
        });

        tabCollections.setOnClickListener(v -> {
            highlightSelectedTab(tabCollections);
            // 这里可以添加加载收藏的逻辑
        });

        // 默认选中内容标签
        highlightSelectedTab(tabContent);
    }

    private void highlightSelectedTab(TextView selectedTab) {
        // 重置所有标签样式
        int unselectedColor = getResources().getColor(R.color.colorTabUnselected);
        int selectedColor = getResources().getColor(R.color.colorTabSelected);

        tabContent.setTextColor(unselectedColor);
        tabContent.setSelected(false);
        tabLocations.setTextColor(unselectedColor);
        tabLocations.setSelected(false);
        tabLikes.setTextColor(unselectedColor);
        tabLikes.setSelected(false);
        tabCollections.setTextColor(unselectedColor);
        tabCollections.setSelected(false);

        // 设置选中标签样式
        selectedTab.setTextColor(selectedColor);
        selectedTab.setSelected(true);

        // 添加点击动画效果
        selectedTab.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(100)
                .withEndAction(() -> {
                    selectedTab.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 显示底部导航栏
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNavigation(true);
        }
    }
}
