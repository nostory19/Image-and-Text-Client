package com.example.myapplication;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.adapter.PostAdapter;
import com.example.myapplication.model.Post;
import com.example.myapplication.viewmodel.PostViewModel;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityOptions;
import android.os.Bundle;

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
//    private int scrollPosition = 0;
    // 保存滚动偏移量
//    private int[] scrollOffsets;
//    private boolean shouldRestorePosition = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(PostViewModel.class);
        // 在onCreate时就开始加载数据，而不是等到onCreateView完成
//        if (firstLoad) {
//            viewModel.refreshData();
//        }
        if (savedInstanceState == null) {
            viewModel.refreshData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 初始化视图
        initViews(view);

        // 设置RecyclerView
        loadingLayout.setVisibility(View.GONE);
        setupRecyclerView();

        // 设置ViewModel观察
        setupViewModelObservers();

        // 设置事件监听
        setupListeners();

//        // 首次加载数据
//        if (firstLoad) {
//            viewModel.refreshData();
//            firstLoad = false;
//        }

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

//    private void setupRecyclerView() {
//        // 设置瀑布流布局管理器，2列
//        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
//        recyclerView.setLayoutManager(layoutManager);
//
//        // 创建并设置适配器
//        postAdapter = new PostAdapter(getContext(), new ArrayList<>(), viewModel, this);
//        recyclerView.setAdapter(postAdapter);
//
//        // 延迟设置滚动监听器
//        recyclerView.post(() -> {
//            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//                @Override
//                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                    super.onScrolled(recyclerView, dx, dy);
//
//                    // 只有在向下滚动且不在加载状态且还有更多数据时才触发加载更多
//                    if (dy > 0 && !viewModel.getIsLoadingLiveData().getValue() &&
//                            viewModel.getHasMoreDataLiveData().getValue()) {
//                        StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
//                        if (layoutManager != null) {
//                            int[] lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null);
//                            int lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);
//
//                            // 当滑动到倒数第2个item时开始加载更多
//                            if (lastVisibleItemPosition >= postAdapter.getItemCount() - 2) {
//                                viewModel.loadMoreData();
//                            }
//                        }
//                    }
//                }
//
//                // 移除原来的onScrollStateChanged中的位置保存逻辑，因为我们现在在点击时立即保存
//            });
//        });
    private void setupRecyclerView() {
        // 使用GridLayoutManager替代StaggeredGridLayoutManager
//        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        // 添加ItemDecoration来减少卡片移动
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.set(8, 8, 8, 8);
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        // 设置适配器
        postAdapter = new PostAdapter(getContext(), new ArrayList<>(), viewModel, this);
        recyclerView.setAdapter(postAdapter);
        // 延迟设置滚动监听器
        recyclerView.post(() -> {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    // 只有在向下滚动且不在加载状态且还有更多数据时才触发加载更多
                    if (dy > 0 && !viewModel.getIsLoadingLiveData().getValue() &&
                            viewModel.getHasMoreDataLiveData().getValue()) {
//                        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                        StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                        if (layoutManager != null) {
                            int[] lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null);
//                            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                            int lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);
                            // 当滑动到倒数第2个item时开始加载更多
                            if (lastVisibleItemPosition >= postAdapter.getItemCount() - 2) {
                                viewModel.loadMoreData();
                            }
                        }
                    }
                }
            });
        });

        // 添加滚动监听，实现上滑加载更多
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                // 只有在向下滚动且不在加载状态且还有更多数据时才触发加载更多
//                if (dy > 0 && !viewModel.getIsLoadingLiveData().getValue() &&
//                        viewModel.getHasMoreDataLiveData().getValue()) {
//                    StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
//                    if (layoutManager != null) {
//                        int[] lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null);
//                        int lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);
//
//                        // 当滑动到倒数第2个item时开始加载更多
//                        if (lastVisibleItemPosition >= postAdapter.getItemCount() - 2) {
//                            viewModel.loadMoreData();
//                        }
//                    }
//                }
//            }
//
//            // 移除原来的onScrollStateChanged中的位置保存逻辑，因为我们现在在点击时立即保存
//        });
    }

    private void setupViewModelObservers() {
        // 观察帖子数据变化
        viewModel.getPostsLiveData().observe(getViewLifecycleOwner(), posts -> {
            postAdapter.updateData(posts);
            updateEmptyState(!posts.isEmpty(), false);
        });

        // 观察加载状态
        viewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            // 只在非刷新状态时显示全局加载布局，用于初始加载
//            if (!viewModel.getIsRefreshingLiveData().getValue()) {
//                loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
//            }
//            if (!isLoading && !viewModel.getIsRefreshingLiveData().getValue()) {
//                postAdapter.showLoadingFooter(false);
//            }
            // 不显示底部加载动画
            postAdapter.showLoadingFooter(false);
        });

        // 观察刷新状态
        viewModel.getIsRefreshingLiveData().observe(getViewLifecycleOwner(), isRefreshing -> {
            swipeRefreshLayout.setRefreshing(isRefreshing);
        });

        // 观察加载更多状态
        viewModel.getHasMoreDataLiveData().observe(getViewLifecycleOwner(), hasMoreData -> {
//            if (!hasMoreData) {
//                postAdapter.showLoadingFooter(false);
//            }
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
//            scrollPosition = 0;
//            scrollOffsets = null;
//            shouldRestorePosition = false;
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
        }

//        // 精确恢复之前保存的滚动位置
//        if (shouldRestorePosition && scrollPosition >= 0 && recyclerView != null) {
//            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
//            if (layoutManager != null) {
//                // 确保位置有效
//                int itemCount = postAdapter.getItemCount();
//                if (scrollPosition < itemCount) {
//                    recyclerView.post(() -> {
//                        // 使用scrollToPositionWithOffset进行精确滚动
//                        layoutManager.scrollToPositionWithOffset(scrollPosition, scrollOffsets != null && scrollOffsets.length > 0 ? scrollOffsets[0] : 0);
//                    });
//                }
//            }
//        }
    }

    // 获取最后一个可见item的位置
    private int getLastVisibleItem(int[] lastVisibleItemPositions) {
//        int maxSize = 0;
//        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
//            if (i == 0) {
//                maxSize = lastVisibleItemPositions[i];
//            } else if (lastVisibleItemPositions[i] > maxSize) {
//                maxSize = lastVisibleItemPositions[i];
//            }
//        }
//        return maxSize;

        int maxSize = 0;
        for (int position : lastVisibleItemPositions) {
            if (position > maxSize) {
                maxSize = position;
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

//    public void saveCurrentScrollPosition() {
//        if (recyclerView != null) {
//            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
//            if (layoutManager != null) {
//                int[] firstVisiblePositions = layoutManager.findFirstVisibleItemPositions(null);
//                if (firstVisiblePositions.length > 0) {
//                    // 取最小的位置
//                    int minPosition = firstVisiblePositions[0];
//                    for (int pos : firstVisiblePositions) {
//                        minPosition = Math.min(minPosition, pos);
//                    }
//                    scrollPosition = minPosition;
//                    // 保存第一个可见项的偏移量
//                    View firstVisibleView = layoutManager.findViewByPosition(minPosition);
//                    if (firstVisibleView != null) {
//                        // 保存每个列的偏移量
//                        scrollOffsets = new int[layoutManager.getSpanCount()];
//                        for (int i = 0; i < layoutManager.getSpanCount(); i++) {
//                            int pos = firstVisiblePositions[i];
//                            if (pos != RecyclerView.NO_POSITION) {
//                                View view = layoutManager.findViewByPosition(pos);
//                                if (view != null) {
//                                    scrollOffsets[i] = view.getTop();
//                                }
//                            }
//                        }
//                    }
//                    shouldRestorePosition = true;
//                }
//            }
//        }
//    }

    @Override
    public void onPostClick(Post post) {
        // 找到被点击的卡片视图
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(getPostPosition(post));
        if (holder instanceof PostAdapter.PostViewHolder) {
            PostAdapter.PostViewHolder postViewHolder = (PostAdapter.PostViewHolder) holder;
            ImageView postImage = postViewHolder.postImage;

            // 设置共享元素转场
            Intent intent = PostDetailActivity.newIntent(requireContext(), post);
            intent.setClass(requireContext(), PostDetailActivity.class);

            // 创建共享元素选项
            Bundle options = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                options = ActivityOptions.makeSceneTransitionAnimation(
                        requireActivity(),
                        postImage,
                        "post_image_" + post.getPost_id()  + "_0"
                ).toBundle();
            }

            if (options != null) {
                startActivity(intent, options);
            } else {
                startActivity(intent);
            }
        } else {
            // 备用方案：直接启动Activity
            Intent intent = PostDetailActivity.newIntent(requireContext(), post);
            intent.setClass(requireContext(), PostDetailActivity.class);
            startActivity(intent);
        }
//        // 改为Activity跳转
//        Intent intent = PostDetailActivity.newIntent(requireContext(), post);
//        // 设置目标Activity
//        intent.setClass(requireContext(), PostDetailActivity.class);
//        startActivity(intent);
    }

    // 获取帖子在列表中的位置
    private int getPostPosition(Post post) {
        List<Post> currentPosts = postAdapter.getPosts();
        for (int i = 0; i < currentPosts.size(); i++) {
            if (currentPosts.get(i).getPost_id().equals(post.getPost_id())) {
                return i;
            }
        }
        return -1;
    }
}
