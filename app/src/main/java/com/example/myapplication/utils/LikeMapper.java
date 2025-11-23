package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * 点赞管理器，负责处理点赞状态的本地持久化存储
 */
public class LikeMapper {
    private static final String PREF_NAME = "likes_prefs";
    private static final String LIKED_POSTS_KEY = "liked_posts";
    private SharedPreferences sharedPreferences;
    private static LikeMapper instance;

    private LikeMapper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // 单例模式
    public static synchronized LikeMapper getInstance(Context context) {
        if (instance == null) {
            instance = new LikeMapper(context.getApplicationContext());
        }
        return instance;
    }

    // 检查作品是否已点赞
    public boolean isPostLiked(String postId) {
        Set<String> likedPosts = sharedPreferences.getStringSet(LIKED_POSTS_KEY, new HashSet<>());
        return likedPosts.contains(postId);
    }

    // 切换作品的点赞状态
    public void togglePostLike(String postId) {
        Set<String> likedPosts = new HashSet<>(sharedPreferences.getStringSet(LIKED_POSTS_KEY, new HashSet<>()));
        if (likedPosts.contains(postId)) {
            likedPosts.remove(postId);
        } else {
            likedPosts.add(postId);
        }
        sharedPreferences.edit().putStringSet(LIKED_POSTS_KEY, likedPosts).apply();
    }

    // 获取所有已点赞的作品ID集合
    public Set<String> getAllLikedPosts() {
        return new HashSet<>(sharedPreferences.getStringSet(LIKED_POSTS_KEY, new HashSet<>()));
    }

    // 清除所有点赞记录
    public void clearAllLikes() {
        sharedPreferences.edit().remove(LIKED_POSTS_KEY).apply();
    }
}
