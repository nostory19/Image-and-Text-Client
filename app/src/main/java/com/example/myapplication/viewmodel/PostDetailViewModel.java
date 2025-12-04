package com.example.myapplication.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.model.Post;

public class PostDetailViewModel extends AndroidViewModel {
    private static final String PREF_FOLLOWED_AUTHORS = "followed_authors";
    private static final String PREF_LIKED_POSTS = "liked_posts";

    private MutableLiveData<Post> postLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isFollowingLiveData = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isLikedLiveData = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isCollectedLiveData = new MutableLiveData<>(false);
    private MutableLiveData<Integer> likeCountLiveData = new MutableLiveData<>(0);

    private SharedPreferences followedPrefs;
    private SharedPreferences likedPrefs;

    public PostDetailViewModel(@NonNull Application application) {
        super(application);
        followedPrefs = application.getSharedPreferences(PREF_FOLLOWED_AUTHORS, Context.MODE_PRIVATE);
        likedPrefs = application.getSharedPreferences(PREF_LIKED_POSTS, Context.MODE_PRIVATE);
    }

    public void setPost(Post post) {
        postLiveData.setValue(post);
        if (post != null) {
            // 初始化状态
            initStates(post);
        }
    }

    private void initStates(Post post) {
        // 初始化关注状态
        if (post.getAuthor() != null) {
            String authorId = post.getAuthor().getUser_id();
            boolean isFollowing = followedPrefs.getBoolean(authorId, false);
            isFollowingLiveData.setValue(isFollowing);
        }

        // 初始化点赞状态
        boolean isLiked = likedPrefs.getBoolean(post.getPost_id(), post.isLiked());
        isLikedLiveData.setValue(isLiked);

        // 初始化点赞数
        int likeCount = post.getLikeCount();
        if (isLiked && !post.isLiked()) {
            likeCount++;
        } else if (!isLiked && post.isLiked()) {
            likeCount--;
        }
        likeCountLiveData.setValue(likeCount);

        // 初始化收藏状态（这里暂时设为false，可根据实际需求调整）
        isCollectedLiveData.setValue(false);
    }

    public LiveData<Post> getPostLiveData() {
        return postLiveData;
    }

    public LiveData<Boolean> getIsFollowingLiveData() {
        return isFollowingLiveData;
    }

    public LiveData<Boolean> getIsLikedLiveData() {
        return isLikedLiveData;
    }

    public LiveData<Boolean> getIsCollectedLiveData() {
        return isCollectedLiveData;
    }

    public LiveData<Integer> getLikeCountLiveData() {
        return likeCountLiveData;
    }

    // 处理关注/取消关注
    public void toggleFollowStatus() {
        Post post = postLiveData.getValue();
        if (post == null || post.getAuthor() == null) return;

        boolean newFollowingStatus = !isFollowingLiveData.getValue();
        String authorId = post.getAuthor().getUser_id();

        // 保存到本地
        SharedPreferences.Editor editor = followedPrefs.edit();
        editor.putBoolean(authorId, newFollowingStatus);
        editor.apply();

        // 更新LiveData
        isFollowingLiveData.setValue(newFollowingStatus);
    }

    // 处理点赞/取消点赞
    public void toggleLikeStatus() {
        Post post = postLiveData.getValue();
        if (post == null) return;

        boolean newLikedStatus = !isLikedLiveData.getValue();
        int currentCount = likeCountLiveData.getValue();
        int newCount = newLikedStatus ? currentCount + 1 : currentCount - 1;

        // 保存到本地
        SharedPreferences.Editor editor = likedPrefs.edit();
        editor.putBoolean(post.getPost_id(), newLikedStatus);
        editor.apply();

        // 更新LiveData
        isLikedLiveData.setValue(newLikedStatus);
        likeCountLiveData.setValue(newCount);
    }

    // 处理收藏/取消收藏
    public void toggleCollectStatus() {
        boolean newCollectedStatus = !isCollectedLiveData.getValue();
        isCollectedLiveData.setValue(newCollectedStatus);
        // 这里可以添加收藏状态的本地存储逻辑
    }
}
