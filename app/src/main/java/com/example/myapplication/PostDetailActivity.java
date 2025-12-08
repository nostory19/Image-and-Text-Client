package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.lifecycle.ViewModelProvider;
//import androidx.vectordrawable.graphics.drawable.RoundedBitmapDrawable;
//import androidx.vectordrawable.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.myapplication.adapter.ImagePagerAdapter;
import com.example.myapplication.model.Post;
import com.example.myapplication.viewmodel.PostDetailViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.animation.DecelerateInterpolator;

public class PostDetailActivity extends AppCompatActivity {

    private static final String EXTRA_POST = "extra_post";

    private PostDetailViewModel postDetailViewModel;
    private ViewPager2 imageViewPager;
    private ImagePagerAdapter imageAdapter;
    private View rootView;

    // 视图组件
    private ImageView backButton;
    private ImageView authorAvatar;
    private TextView authorName;
    private Button followButton;
    private TextView postTitle;
    private TextView postContent;
    private LinearLayout topicContainer;
    private TextView postDate;
    private LinearLayout likeButton;
    private ImageView likeIcon;
    private LinearLayout commentButton;
    private ImageView commentIcon;
    private TextView commentCount;
    private LinearLayout collectButton;


    private ImageView collectIcon;

    private TextView collectCount;
    private LinearLayout shareButton;

    private ImageView shareIcon;

    private TextView shareCount;
    private TextView likeCount;
    private LinearLayout progressDotsContainer;

    // 音乐播放相关
    private MusicPlayerManager musicPlayer;
    private ImageView muteButton;
    private boolean isActivityResumed = false;

    // 静态方法，用于创建带有Post参数的Intent
    public static Intent newIntent(Context context, Post post) {
        Intent intent = new Intent(context, PostDetailActivity.class);
        intent.putExtra(EXTRA_POST, post);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置音频集点策略
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // 设置进入和退出转场动画
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
            getWindow().setEnterTransition(new Fade());
            getWindow().setExitTransition(new Fade());
        }
        // 加载布局
        rootView = getLayoutInflater().inflate(R.layout.fragment_post_detail, null);
        setContentView(rootView);

        // 从Intent获取Post对象
        Post post = (Post) getIntent().getSerializableExtra(EXTRA_POST);
        if (post == null) {
            Toast.makeText(this, "无法加载帖子数据", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化ViewModel
        postDetailViewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);
        postDetailViewModel.setPost(post);

        // 初始化视图组件
        initViews(rootView);
        // 初始化音乐播放器
        musicPlayer = MusicPlayerManager.getInstance(this);
        // 观察数据
        observeViewModel();

        // 设置事件监听
        setupEventListeners();

        // 延迟设置共享元素转场
        setupSharedElementTransition(post);
    }

    // 自定义缩放动画
    private void setupCustomScaleAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = getWindow().getSharedElementEnterTransition();
            if (transition != null) {
                // 添加缩放变换，使放大效果更明显
                transition.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {
                        // 在转场开始时应用初始缩放
                        if (imageViewPager != null) {
                            imageViewPager.setScaleX(0.8f);
                            imageViewPager.setScaleY(0.8f);
                        }
                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {
                        // 转场结束时恢复正常大小
                        if (imageViewPager != null) {
                            imageViewPager.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(200)
                                    .start();
                        }
                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {
                        if (imageViewPager != null) {
                            imageViewPager.setScaleX(1f);
                            imageViewPager.setScaleY(1f);
                        }
                    }

                    @Override
                    public void onTransitionPause(Transition transition) {
                    }

                    @Override
                    public void onTransitionResume(Transition transition) {
                    }
                });
            }
        }
    }
    // 设置共享元素转场
    // 设置共享元素转场
//    private void setupSharedElementTransition(Post post) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            // 延迟执行，确保ViewPager已经初始化
//            imageViewPager.post(() -> {
//                if (imageAdapter != null && imageAdapter.getItemCount() > 0) {
//                    // 设置ViewPager的过渡名称

    /// /                    imageViewPager.setTransitionName("post_image_" + post.getPost_id());
//
//                    // 设置ViewPager中第一个图片的过渡名称
//                    View firstImage = imageViewPager.getChildAt(0);
//                    if (firstImage != null) {
//                        firstImage.setTransitionName("post_image_" + post.getPost_id() + "_0");
//                    }
//                    // 设置进入动画
//                    Transition transition = getWindow().getSharedElementEnterTransition();
//                    if (transition != null) {
//                        transition.setDuration(400);
//                        transition.setInterpolator(new DecelerateInterpolator());
//                        transition.addListener(new Transition.TransitionListener() {
//                            @Override
//                            public void onTransitionStart(Transition transition) {
//                                // 转场开始时只隐藏非图片内容，保持图片可见
//                                View contentContainer = rootView.findViewById(R.id.content_container);
//                                View bottomSection = rootView.findViewById(R.id.interaction_section);
//
//                                if (contentContainer != null) {
//                                    contentContainer.setAlpha(0f);
//                                }
//                                if (bottomSection != null) {
//                                    bottomSection.setAlpha(0f);
//                                }
//                                // 确保图片容器可见
//                                if (imageViewPager != null) {
//                                    imageViewPager.setAlpha(1f);
//                                }
//                            }
//
//                            @Override
//                            public void onTransitionEnd(Transition transition) {
//                                // 转场结束时渐显其他内容
//                                animateContentAppearance();
//                            }
//
//                            @Override
//                            public void onTransitionCancel(Transition transition) {
//                                // 转场取消时确保所有内容可见
//                                View contentContainer = rootView.findViewById(R.id.content_container);
//                                View bottomSection = rootView.findViewById(R.id.interaction_section);
//                                if (contentContainer != null) contentContainer.setAlpha(1f);
//                                if (bottomSection != null) bottomSection.setAlpha(1f);
//                            }
//
//                            @Override
//                            public void onTransitionPause(Transition transition) {}
//
//                            @Override
//                            public void onTransitionResume(Transition transition) {}
//                        });
//                    }
//                }
//            });
//        }
//    }
    private void setupSharedElementTransition(Post post) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 延迟执行，确保ViewPager已经初始化
            imageViewPager.post(() -> {
                if (imageAdapter != null && imageAdapter.getItemCount() > 0) {
                    // 设置ViewPager中第一个图片的过渡名称
                    View firstImage = imageViewPager.getChildAt(0);
                    if (firstImage != null) {
                        firstImage.setTransitionName("post_image_" + post.getPost_id() + "_0");
                    }

                    // 增强进入动画效果
                    setupCustomScaleAnimation();

                    // ... 其余代码保持不变 ...
                }
            });
        }
    }

    // 动画内容渐显
    // 动画内容渐显
    private void animateContentAppearance() {
        View contentContainer = rootView.findViewById(R.id.content_container);
        View bottomSection = rootView.findViewById(R.id.interaction_section);

        if (contentContainer != null && bottomSection != null) {
            ObjectAnimator contentAlpha = ObjectAnimator.ofFloat(contentContainer, "alpha", 0f, 1f);
            ObjectAnimator bottomAlpha = ObjectAnimator.ofFloat(bottomSection, "alpha", 0f, 1f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(300);
            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.playTogether(contentAlpha, bottomAlpha);
            animatorSet.start();
        }
    }

    private void initViews(View view) {
        // 顶部作者区
        backButton = view.findViewById(R.id.back_button);
        authorAvatar = view.findViewById(R.id.author_avatar);
        authorName = view.findViewById(R.id.author_name);
        followButton = view.findViewById(R.id.follow_button);
        // 图片容器
        imageViewPager = view.findViewById(R.id.image_view_pager);
        progressDotsContainer = view.findViewById(R.id.progress_dots_container);
        // 静音按钮
        muteButton = view.findViewById(R.id.mute_button);
        // 内容区
        postTitle = view.findViewById(R.id.post_title);
        postContent = view.findViewById(R.id.post_content);
        topicContainer = view.findViewById(R.id.topic_container);
        postDate = view.findViewById(R.id.post_date);

        // 底部交互区
        likeButton = view.findViewById(R.id.like_button);
        likeIcon = findFirstImageView(likeButton);
        likeCount = view.findViewById(R.id.like_count);
        commentButton = view.findViewById(R.id.comment_button);
        commentIcon = findFirstImageView(commentButton);
        commentCount = view.findViewById(R.id.comment_count);
        collectButton = view.findViewById(R.id.collect_button);
        collectIcon = findFirstImageView(collectButton);
        collectCount = view.findViewById(R.id.collect_count);
        shareButton = view.findViewById(R.id.share_button);
        shareIcon = findFirstImageView(shareButton);
        shareCount = view.findViewById(R.id.share_count);
    }

    private void observeViewModel() {
        // 观察帖子数据变化
        postDetailViewModel.getPostLiveData().observe(this, post -> {
            if (post != null) {
                // 设置作者信息
                if (post.getAuthor() != null) {
                    authorName.setText(post.getAuthor().getNickname());
                    loadAuthorAvatar(post);
                }

                // 设置图片适配器
                setupImageAdapter(post);

                // 设置帖子内容
                postTitle.setText(post.getTitle());
                postContent.setText(post.getContent() != null ? post.getContent() : "暂无内容");

                // 设置话题标签
                setupTopics();

                // 设置发布日期
                postDate.setText(formatDate(post.getCreate_time()));
            }
        });

        // 观察关注状态变化
        postDetailViewModel.getIsFollowingLiveData().observe(this, isFollowing -> {
            updateFollowButton(isFollowing);
        });


        // 观察点赞状态变化
        postDetailViewModel.getIsLikedLiveData().observe(this, isLiked -> {
            updateLikeStatus(isLiked);
            // 添加点赞动画
            animateLike(isLiked);
        });

        // 观察点赞数变化
        postDetailViewModel.getLikeCountLiveData().observe(this, count -> {
            likeCount.setText(String.valueOf(count));
        });

        // 观察收藏状态变化
        postDetailViewModel.getIsCollectedLiveData().observe(this, isCollected -> {
//            collectIcon.setImageResource(isCollected ? R.drawable.ic_collected : R.drawable.ic_collect);
            collectIcon.setColorFilter(isCollected ? Color.RED : Color.TRANSPARENT);
            collectCount.setText(String.valueOf(postDetailViewModel.getCollectCountLiveData().getValue()));

        });
        // 观察评论状态
        postDetailViewModel.getCommentCountLiveData().observe(this, count -> {
            commentCount.setText(String.valueOf(postDetailViewModel.getCommentCountLiveData().getValue()));
        });
        // 观察分享状态变化
        postDetailViewModel.getShareCountLiveData().observe(this, count -> {
            shareCount.setText(String.valueOf(postDetailViewModel.getShareCountLiveData().getValue()));
        });

    }

    // 在setupImageAdapter方法中调用updateImageContainerSize
    private void setupImageAdapter(Post post) {
        if (post == null || post.getClips() == null || post.getClips().isEmpty()) {
            // 无图片状态
            progressDotsContainer.setVisibility(View.GONE);
            imageViewPager.setVisibility(View.GONE);
            return;
        }

        imageViewPager.setVisibility(View.VISIBLE);

        // 多图时显示进度条
        if (post.getClips().size() > 1) {
            progressDotsContainer.setVisibility(View.VISIBLE);
            // 初始化小圆点指示器
            initProgressDots(post.getClips().size());
            updateProgress(0);
        } else {
            progressDotsContainer.setVisibility(View.GONE);
        }

        // 创建图片适配器
        imageAdapter = new ImagePagerAdapter(this, post.getClips(), post.getPost_id());
        imageViewPager.setAdapter(imageAdapter);

        // 更新图片容器尺寸
        updateImageContainerSize();

        // 监听ViewPager页面变化
        imageViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateProgress(position);
            }
        });
    }

    // 添加updateImageContainerSize方法，根据aspectRatio计算并设置图片容器尺寸
    private void updateImageContainerSize() {
        if (imageAdapter == null) return;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int calculatedHeight = (int) (screenWidth / imageAdapter.getAspectRatio());

        ViewGroup.LayoutParams layoutParams = imageViewPager.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.height = calculatedHeight;
            imageViewPager.setLayoutParams(layoutParams);
        }
    }

    // 添加一个新方法来初始化小圆点指示器
    private void initProgressDots(int count) {
        // 清除现有指示器
        progressDotsContainer.removeAllViews();

        // 设置weightSum，以便平均分配空间
        progressDotsContainer.setWeightSum(count);

        // 根据图片数量创建条状物指示器
        for (int i = 0; i < count; i++) {
            View dotView = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, // 使用weight分配宽度
                    8   // 保持高度不变
            );

            // 使用weight平均分配空间
            params.weight = 1;

            // 设置条状物之间的间距
            if (i > 0 && i < count - 1) {
                params.leftMargin = 4;
                params.rightMargin = 4;
            } else if (i > 0) {
                // 最后一个只设置左边距
                params.leftMargin = 4;
            }

            // 设置条状物样式
            dotView.setLayoutParams(params);
            dotView.setBackgroundResource(R.drawable.dot_drawable);

            // 默认设置为非选中状态
            dotView.setSelected(false);

            // 添加到容器
            progressDotsContainer.addView(dotView);
        }
    }

    private void updateProgress(int position) {
        Post post = postDetailViewModel.getPostLiveData().getValue();
        if (post != null && post.getClips() != null) {
            int total = post.getClips().size();
            // 更新小圆点选中状态
            for (int i = 0; i < progressDotsContainer.getChildCount(); i++) {
                progressDotsContainer.getChildAt(i).setSelected(i == position);
            }
        }
    }

    private void setupTopics() {
        topicContainer.removeAllViews();
        // 这里可以从ViewModel获取话题数据，暂时使用模拟数据
        List<String> topics = new ArrayList<>();
        topics.add("摄影");
        topics.add("旅行");

        for (String topic : topics) {
            TextView topicView = (TextView) LayoutInflater.from(this)
                    .inflate(R.layout.item_topic, topicContainer, false);
            topicView.setText("#" + topic);
            topicView.setOnClickListener(v -> handleTopicClick(topic));
            topicContainer.addView(topicView);
        }
    }

    private void handleTopicClick(String topic) {
        // 处理话题点击事件，这里可以跳转到相关话题页面
        Toast.makeText(this, "点击了话题: " + topic, Toast.LENGTH_SHORT).show();
    }

    private void loadAuthorAvatar(Post post) {
        if (post == null || post.getAuthor() == null) return;

        String avatarUrl = post.getAuthor().getAvatar();
        if (avatarUrl != null) {
            try {
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.user_avatar)
                        .error(R.drawable.user_avatar)
                        .circleCrop()
                        .into(authorAvatar);
            } catch (Exception e) {
                setupDefaultAvatar();
            }
        } else {
            setupDefaultAvatar();
        }
    }

    private void setupDefaultAvatar() {
        // 确保头像显示为圆形
        GradientDrawable circleDrawable = new GradientDrawable();
        circleDrawable.setShape(GradientDrawable.OVAL);
        circleDrawable.setColor(Color.TRANSPARENT);
        circleDrawable.setStroke(1, Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            authorAvatar.setBackground(circleDrawable);
        } else {
            authorAvatar.setBackgroundDrawable(circleDrawable);
        }

        // 使用RoundedBitmapDrawable确保图片也是圆形的
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.user_avatar);
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            roundedBitmapDrawable.setCircular(true);
            authorAvatar.setImageDrawable(roundedBitmapDrawable);
        } else {
            authorAvatar.setImageResource(R.drawable.user_avatar);
        }

        // 添加额外的null检查
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            authorAvatar.setClipToOutline(true);
        }
    }

    private String formatDate(long timestamp) {
        // 时间格式化逻辑保持不变
        if (timestamp < 10000000000L) {
            timestamp = timestamp * 1000; // 将秒转换为毫秒
        }

        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        // 24小时内
        if (diff < 24 * 60 * 60 * 1000) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            long todayStartTime = today.getTimeInMillis();

            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_MONTH, -1);
            yesterday.set(Calendar.HOUR_OF_DAY, 0);
            yesterday.set(Calendar.MINUTE, 0);
            yesterday.set(Calendar.SECOND, 0);
            yesterday.set(Calendar.MILLISECOND, 0);
            long yesterdayStartTime = yesterday.getTimeInMillis();

            if (timestamp >= todayStartTime) {
                return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
            } else if (timestamp >= yesterdayStartTime) {
                return "昨天 " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
            }
        }
        // 7天内
        else if (diff < 7 * 24 * 60 * 60 * 1000) {
            long days = diff / (24 * 60 * 60 * 1000);
            return days + "天前";
        }
        // 其他情况
        else {
            return new SimpleDateFormat("MM-dd", Locale.getDefault()).format(new Date(timestamp));
        }
        return new SimpleDateFormat("MM-dd", Locale.getDefault()).format(new Date(timestamp));
    }

    private void updateLikeStatus(boolean liked) {
        if (likeIcon != null) {
            if (liked) {
                likeIcon.setImageResource(R.drawable.ic_like);
                likeIcon.setColorFilter(Color.RED);
            } else {
                likeIcon.setImageResource(R.drawable.ic_like);
                likeIcon.clearColorFilter();
            }
        }
    }


    private void animateLike(boolean isLiked) {
        if (likeIcon != null) {
            // 放大再缩小的动画
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(likeIcon, "scaleX", 1f, 1.3f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(likeIcon, "scaleY", 1f, 1.3f, 1f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(300);
            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.start();
        }
    }

    private void updateFollowButton(boolean isFollowing) {
        if (isFollowing) {
            followButton.setText("已关注");
//            followButton.setBackgroundResource(R.drawable.followed_button_bg);
            // 改变边框颜色为灰色
            updateButtonBorderColor(Color.parseColor("#999999"));
        } else {
            followButton.setText("关注");
//            followButton.setBackgroundResource(R.drawable.follow_button_bg);
            updateButtonBorderColor(Color.parseColor("#fe2c55"));
        }
    }

    // 动态更新边框颜色的辅助方法
    private void updateButtonBorderColor(int color) {
        // 方法1：直接操作GradientDrawable（如果背景是Shape）
        Drawable background = followButton.getBackground();
        if (background instanceof GradientDrawable) {
            GradientDrawable drawable = (GradientDrawable) background;
            drawable.setStroke(2, color); // 2px边框
        } else {
            // 方法2：重新设置背景
            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(19); // 圆角
            drawable.setStroke(2, color); // 边框
            drawable.setColor(Color.TRANSPARENT); // 透明背景
            followButton.setBackground(drawable);
        }
    }

    private void setupEventListeners() {
        // 返回按钮
//        backButton.setOnClickListener(v -> onBackPressed());
        backButton.setOnClickListener(v -> {
            // 执行退出动画
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition();
            } else {
                finish();
            }
        });
        // 关注按钮
        // 如果布局中存在follow_button，可以添加以下代码

        followButton.setOnClickListener(v -> {
            postDetailViewModel.toggleFollowStatus();
            Toast.makeText(this, postDetailViewModel.getIsFollowingLiveData().getValue() ? "已关注" : "已取消关注", Toast.LENGTH_SHORT).show();
        });


        // 点赞按钮
        likeButton.setOnClickListener(v -> {
            postDetailViewModel.toggleLikeStatus();
        });

        // 评论按钮
        commentButton.setOnClickListener(v -> Toast.makeText(this, "打开评论", Toast.LENGTH_SHORT).show());

        // 收藏按钮
        collectButton.setOnClickListener(v -> {
            postDetailViewModel.toggleCollectStatus();
            Toast.makeText(this, postDetailViewModel.getIsCollectedLiveData().getValue() ? "已收藏" : "已取消收藏", Toast.LENGTH_SHORT).show();
        });

        // 分享按钮
        shareButton.setOnClickListener(v -> handleShare());

        // 静音按钮点击
        muteButton.setOnClickListener(v -> {
            musicPlayer.toggleMute();
            updateMuteButton();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityResumed = true;

        // 恢复音乐播放
        if (musicPlayer != null) {
            musicPlayer.resume();
        }

        // 更新静音按钮状态
        updateMuteButton();

        // 开始播放音乐（如果有）
        Post post = postDetailViewModel.getPostLiveData().getValue();
        if (post != null && post.getMusic() != null) {
            musicPlayer.playMusic(post);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityResumed = false;

        // 暂停音乐播放（但不释放资源，以便返回时恢复）
        if (musicPlayer != null) {
            musicPlayer.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 当Activity完全不可见时暂停播放
        if (musicPlayer != null) {
            musicPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 只有在Activity被销毁时才释放播放器
        if (isFinishing() && musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.release();
        }
    }

    private void handleShare() {
        Toast.makeText(this, "分享成功", Toast.LENGTH_SHORT).show();
    }

    private ImageView findFirstImageView(LinearLayout layout) {
        if (layout != null && layout.getChildCount() > 0) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                if (layout.getChildAt(i) instanceof ImageView) {
                    return (ImageView) layout.getChildAt(i);
                }
            }
        }
        return null;
    }

    private void updateMuteButton() {
        if (musicPlayer != null) {
            if (musicPlayer.isMuted()) {
                muteButton.setImageResource(R.drawable.ic_volume_off);
            } else {
                muteButton.setImageResource(R.drawable.ic_volume_up);
            }
        }
    }
}