package com.example.myapplication.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

/**
 * 管理用户个人信息数据
 */
public class MineViewModel extends AndroidViewModel {
    private static final String TAG = "MineViewModel";

    // 用户信息相关数据
    private MutableLiveData<String> userAvatar = new MutableLiveData<>();
    private MutableLiveData<String> userNickname = new MutableLiveData<>();
    private MutableLiveData<String> userBio = new MutableLiveData<>();

    // 统计数据
    private MutableLiveData<Integer> followingCount = new MutableLiveData<>();
    private MutableLiveData<Integer> followersCount = new MutableLiveData<>();
    private MutableLiveData<Integer> likesCount = new MutableLiveData<>();

    // 页面状态
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);

    public MineViewModel(Application application) {
        super(application);
        // 初始化模拟数据
        initMockData();
    }

    private void initMockData() {
        // 模拟网络请求延迟
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 设置模拟数据
        userAvatar.setValue("https://randomuser.me/api/portraits/women/44.jpg");
        userNickname.setValue("摄影爱好者");
        userBio.setValue("热爱生活，记录美好瞬间");
        followingCount.setValue(256);
        followersCount.setValue(1289);
        likesCount.setValue(5678);

        // 设置加载完成
        isLoading.setValue(false);
    }

    // getter方法
    public MutableLiveData<String> getUserAvatar() {
        return userAvatar;
    }

    public MutableLiveData<String> getUserNickname() {
        return userNickname;
    }

    public MutableLiveData<String> getUserBio() {
        return userBio;
    }

    public MutableLiveData<Integer> getFollowingCount() {
        return followingCount;
    }

    public MutableLiveData<Integer> getFollowersCount() {
        return followersCount;
    }

    public MutableLiveData<Integer> getLikesCount() {
        return likesCount;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // 刷新用户数据
    public void refreshUserData() {
        isLoading.setValue(true);
        // 这里可以实现实际的网络请求逻辑
        initMockData();
    }
}
