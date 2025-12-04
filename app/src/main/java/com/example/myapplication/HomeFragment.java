package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.adapter.PostAdapter;
import com.example.myapplication.model.Post;
import com.example.myapplication.viewmodel.PostViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements PostAdapter.OnPostClickListener {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout emptyStateLayout;
    private RelativeLayout loadingLayout;
    private TextView emptyStateTip;
    private Button retryButton;

    private PostViewModel viewModel;
    private boolean firstLoad = true;
    private int scrollPosition = 0;
    private boolean shouldRestorePosition = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(PostViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 初始化视图
        initViews(view);

        // 设置RecyclerView
        setupRecyclerView();

        // 设置ViewModel观察
        setupViewModelObservers();

        // 设置事件监听
        setupListeners();

        // 首次加载数据
        if (firstLoad) {
            viewModel.refreshData();
            firstLoad = false;
        }

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.waterfall_recycler);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);
        loadingLayout = view.findViewById(R.id.loading_layout);
        emptyStateTip = view.findViewById(R.id.empty_state_tip);
        retryButton = view.findViewById(R.id.retry_button);
    }

    private void setupRecyclerView() {
        // 设置瀑布流布局管理器，2列
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        // 创建并设置适配器
        postAdapter = new PostAdapter(getContext(), new ArrayList<>(), viewModel, this);
        recyclerView.setAdapter(postAdapter);

        // 添加滚动监听，实现上滑加载更多
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 只有在向下滚动且不在加载状态且还有更多数据时才触发加载更多
                if (dy > 0 && !viewModel.getIsLoadingLiveData().getValue() &&
                        viewModel.getHasMoreDataLiveData().getValue()) {
                    StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int[] lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null);
                        int lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);

                        // 当滑动到倒数第2个item时开始加载更多
                        if (lastVisibleItemPosition >= postAdapter.getItemCount() - 2) {
                            viewModel.loadMoreData();
                        }
                    }
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // 当滚动停止时，保存当前滚动位置
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int[] firstVisiblePositions = layoutManager.findFirstVisibleItemPositions(null);
                        if (firstVisiblePositions.length > 0) {
                            // 取最小的位置，确保是真正的第一个可见项
                            int minPosition = firstVisiblePositions[0];
                            for (int pos : firstVisiblePositions) {
                                minPosition = Math.min(minPosition, pos);
                            }
                            scrollPosition = minPosition;
                            shouldRestorePosition = true;
                        }
                    }
                }
            }
        });
    }

    private void setupViewModelObservers() {
        // 观察帖子数据变化
        viewModel.getPostsLiveData().observe(getViewLifecycleOwner(), posts -> {
            postAdapter.updateData(posts);
            updateEmptyState(!posts.isEmpty(), false);
        });

        // 观察加载状态
        viewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (!isLoading && !viewModel.getIsRefreshingLiveData().getValue()) {
                postAdapter.showLoadingFooter(false);
            }
        });

        // 观察刷新状态
        viewModel.getIsRefreshingLiveData().observe(getViewLifecycleOwner(), isRefreshing -> {
            swipeRefreshLayout.setRefreshing(isRefreshing);
        });

        // 观察加载更多状态
        viewModel.getHasMoreDataLiveData().observe(getViewLifecycleOwner(), hasMoreData -> {
            if (!hasMoreData) {
                postAdapter.showLoadingFooter(false);
            }
        });

        // 观察错误状态
        viewModel.getIsErrorLiveData().observe(getViewLifecycleOwner(), isError -> {
            if (isError && viewModel.getErrorMessageLiveData().getValue() != null) {
                Toast.makeText(getContext(), viewModel.getErrorMessageLiveData().getValue(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        // 设置下拉刷新
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // 刷新时重置保存的位置
            scrollPosition = 0;
            shouldRestorePosition = false;
            viewModel.refreshData();
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        // 设置重试按钮监听
        retryButton.setOnClickListener(v -> viewModel.refreshData());
    }

    @Override
    public void onResume() {
        super.onResume();

        // 从详情页返回时，应用本地存储的点赞状态
        List<Post> postList = viewModel.getPostsLiveData().getValue();
        if (postList != null && !postList.isEmpty()) {
            viewModel.applyLocalLikeStatus();

            // 恢复之前的滚动位置
            if (scrollPosition > 0 && recyclerView != null) {
                StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    recyclerView.post(() -> {
                        // 为了更好的用户体验，可以滚动到比保存位置稍前的位置
                        int positionToScroll = Math.max(0, scrollPosition - 2);
                        layoutManager.scrollToPosition(positionToScroll);
                    });
                }
            }
        }
    }

    // 获取最后一个可见item的位置
    private int getLastVisibleItem(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i];
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }

    // 根据数据显示空态页面
    private void updateEmptyState(boolean hasData, boolean isError) {
        if (hasData) {
            emptyStateLayout.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        } else {
            emptyStateLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
            emptyStateTip.setText(isError ? "加载失败，请重试" : "暂无内容");
        }
    }

    @Override
    public void onPostClick(Post post) {
        try {
            // 使用getParentFragmentManager代替getFragmentManager
            FragmentManager fragmentManager = getParentFragmentManager();
            if (fragmentManager != null) {
                // 替换当前Fragment为详情页
                PostDetailFragment fragment = PostDetailFragment.newInstance(post);
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "跳转失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
}
