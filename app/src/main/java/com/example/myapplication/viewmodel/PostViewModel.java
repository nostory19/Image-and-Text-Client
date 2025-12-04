package com.example.myapplication.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.model.Post;
import com.example.myapplication.repository.PostRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostViewModel extends AndroidViewModel {
    private static final String TAG = "PostViewModel";
    private PostRepository repository;
    private MutableLiveData<List<Post>> postsLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isRefreshingLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> hasMoreDataLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    private List<Post> posts = new ArrayList<>();
    private boolean isLoading = false;
    private boolean hasMoreData = true;

    public PostViewModel(Application application) {
        super(application);
        repository = new PostRepository(application.getApplicationContext());
        hasMoreDataLiveData.setValue(true);
    }

    public MutableLiveData<List<Post>> getPostsLiveData() {
        return postsLiveData;
    }

    public MutableLiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public MutableLiveData<Boolean> getIsRefreshingLiveData() {
        return isRefreshingLiveData;
    }

    public MutableLiveData<Boolean> getHasMoreDataLiveData() {
        return hasMoreDataLiveData;
    }

    public MutableLiveData<Boolean> getIsErrorLiveData() {
        return isErrorLiveData;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }


    // 刷新数据
    public void refreshData() {
        if (isLoading) return;

        isLoading = true;
        isLoadingLiveData.setValue(true);
        isRefreshingLiveData.setValue(true);
        isErrorLiveData.setValue(false);
        // 先尝试从缓存加载数据，立即展示给用户
        List<Post> cachedPosts = repository.readPostsFromCache();
        if (cachedPosts != null && !cachedPosts.isEmpty()) {
            // 应用本地存储的点赞状态
            repository.applyLocalLikeStatus(cachedPosts);

            posts.clear();
            posts.addAll(cachedPosts);
            postsLiveData.setValue(new ArrayList<>(posts));

            // 通知UI缓存已加载，正在刷新最新数据
            hasMoreDataLiveData.setValue(true);
        }
        // 异步请求最新数据
        repository.fetchPosts(true, new PostRepository.PostCallback() {
            @Override
            public void onSuccess(List<Post> result) {
                repository.savePostsToCache(result);
                // 应用本地存储的点赞状态
                repository.applyLocalLikeStatus(result);

                posts.clear();
                posts.addAll(result);
                postsLiveData.setValue(new ArrayList<>(posts));

                hasMoreData = result.size() >= 5;
                hasMoreDataLiveData.setValue(hasMoreData);

                isLoading = false;
                isLoadingLiveData.setValue(false);
                isRefreshingLiveData.setValue(false);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "刷新失败: " + error);

                // 加载失败时显示测试数据
                posts.clear();
                List<Post> testPosts = repository.generateTestPosts(10);
                posts.addAll(testPosts);
                postsLiveData.setValue(new ArrayList<>(posts));

                hasMoreData = true;
                hasMoreDataLiveData.setValue(true);

                isLoading = false;
                isLoadingLiveData.setValue(false);
                isRefreshingLiveData.setValue(false);
                isErrorLiveData.setValue(false); // 不显示错误状态，因为使用了测试数据
            }
        });
    }

    // 加载更多数据
    public void loadMoreData() {
        if (isLoading || !hasMoreData) return;
// 移除isLoading状态的代码，避免触发加载动画
//        isLoading = true;
//        isLoadingLiveData.setValue(true);

        repository.fetchPosts(false, new PostRepository.PostCallback() {
            @Override
            public void onSuccess(List<Post> result) {
                // 为了避免重复数据，进行去重处理
                Set<String> existingIds = new HashSet<>();
                for (Post post : posts) {
                    if (post.getPost_id() != null) {
                        existingIds.add(post.getPost_id());
                    }
                }

                List<Post> uniquePosts = new ArrayList<>();
                for (Post newPost : result) {
                    if (newPost.getPost_id() == null || !existingIds.contains(newPost.getPost_id())) {
                        uniquePosts.add(newPost);
                    }
                }

                // 应用本地存储的点赞状态
                repository.applyLocalLikeStatus(uniquePosts);

                posts.addAll(uniquePosts);
                postsLiveData.setValue(new ArrayList<>(posts));

                hasMoreData = result.size() >= 5;
                hasMoreDataLiveData.setValue(hasMoreData);

//                isLoading = false;
//                isLoadingLiveData.setValue(false);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "加载更多失败: " + error);

//                isLoading = false;
//                isLoadingLiveData.setValue(false);
                isErrorLiveData.setValue(true);
                errorMessageLiveData.setValue("加载更多失败");
            }
        });
    }

    // 保存点赞状态
    public void saveLikeStatus(String postId, boolean isLiked) {
        repository.saveLikeStatus(postId, isLiked);

        // 更新内存中的点赞状态
        for (Post post : posts) {
            if (post.getPost_id().equals(postId)) {
                post.setLiked(isLiked);
                post.setLikeCount(post.getLikeCount() + (isLiked ? 1 : -1));
                break;
            }
        }

        // 通知UI更新
        postsLiveData.setValue(new ArrayList<>(posts));
    }

    // 应用本地存储的点赞状态（用于从详情页返回时）
    public void applyLocalLikeStatus() {
        repository.applyLocalLikeStatus(posts);
        postsLiveData.setValue(new ArrayList<>(posts));
    }

    public List<Post> getPosts() {
        return new ArrayList<>(posts);
    }
}
