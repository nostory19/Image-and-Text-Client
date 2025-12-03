package com.example.myapplication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.myapplication.model.Post;
import com.example.myapplication.model.PostDetail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostDetailFragment extends Fragment {
    private static final String ARG_POST = "post";
    private static final String PREF_FOLLOWED_AUTHORS = "followed_authors";
    private static final String PREF_LIKED_POSTS = "liked_posts";

    private PostDetail postDetail;
    private ViewPager2 imageViewPager;
    private TextView progressText;
    private ProgressBar progressBar;
    private ImageView backButton;
    private ImageView authorAvatar;
    private TextView authorName;
    private Button followButton;
    private TextView postTitle;
    private TextView postContent;
    private LinearLayout topicContainer;
    private TextView postDate;
    private EditText quickCommentEdit;
//    private ImageView likeButton;

    private LinearLayout likeButton;

    private ImageView likeIcon;
    private LinearLayout commentButton;

    private ImageView commentIcon;
    private LinearLayout collectButton;

    private ImageView collectIcon;
    private LinearLayout shareButton;

    private ImageView shareIcon;
    private TextView likeCount;

    private boolean isFollowing = false;
    private boolean isLiked = false;
    private boolean isCollected = false;
    private int currentLikeCount;

    public static PostDetailFragment newInstance(Post post) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_POST, post);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // 从HomeFragment传递过来的Post对象
            Post post = (Post) getArguments().getSerializable(ARG_POST);
            if (post != null){
                // 模拟构建完整的详情数据
                postDetail = getPostDetailFromServer(post);
                isLiked = post.isLiked();
                currentLikeCount = post.getLikeCount();
            }else {
                postDetail = createMockPostDetail(post);
                isLiked = post.isLiked();
                currentLikeCount = post.getLikeCount();
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 加载详情页布局
        return inflater.inflate(R.layout.fragment_post_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 确保头像显示为圆形


        // 初始化视图组件
        initViews(view);
        if (authorAvatar != null) {
            // 创建圆形背景
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
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.user_avatar);
            if (drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                roundedBitmapDrawable.setCircular(true);
                authorAvatar.setImageDrawable(roundedBitmapDrawable);
            }

            // 添加额外的null检查
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                authorAvatar.setClipToOutline(true);
            }
        }
        // 设置数据
        setupData();

        // 设置监听器
        setupListeners();
    }

    private void initViews(View view) {
        // 顶部作者区
        backButton = view.findViewById(R.id.back_button);
        authorAvatar = view.findViewById(R.id.author_avatar);
        authorName = view.findViewById(R.id.author_name);
        followButton = view.findViewById(R.id.follow_button);

        // 图片容器
        imageViewPager = view.findViewById(R.id.image_view_pager);
        progressText = view.findViewById(R.id.progress_text);
        progressBar = view.findViewById(R.id.progress_bar);

        // 内容区
        postTitle = view.findViewById(R.id.post_title);
        postContent = view.findViewById(R.id.post_content);
        topicContainer = view.findViewById(R.id.topic_container);
        postDate = view.findViewById(R.id.post_date);

        // 底部交互区
        quickCommentEdit = view.findViewById(R.id.quick_comment_edit);
        likeButton = view.findViewById(R.id.like_button);
        likeIcon = findFirstImageView(likeButton);
        commentButton = view.findViewById(R.id.comment_button);
        commentIcon = findFirstImageView(commentButton);
        collectButton = view.findViewById(R.id.collect_button);
        collectIcon = findFirstImageView(collectButton);
        shareButton = view.findViewById(R.id.share_button);
        shareIcon = findFirstImageView(shareButton);
        likeCount = view.findViewById(R.id.like_count);
    }
    // 添加辅助方法查找LinearLayout中的第一个ImageView
    private ImageView findFirstImageView(LinearLayout linearLayout) {
        if (linearLayout != null && linearLayout.getChildCount() > 0) {
            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                if (linearLayout.getChildAt(i) instanceof ImageView) {
                    return (ImageView) linearLayout.getChildAt(i);
                }
            }
        }
        return null;
    }
//    private void setupData() {
//        if (postDetail == null) return;
//
//        // 设置作者信息
//        authorName.setText(postDetail.getAuthorName());
//        // 设置作者头像
//        loadAuthorAvatar();
//
//        // 检查是否已关注该作者
//        checkFollowStatus();
//
//        // 设置图片适配器
//        setupImageAdapter();
//
//        // 设置帖子内容
//        postTitle.setText(postDetail.getTitle());
//        postContent.setText(postDetail.getContent());
//
//        // 设置话题标签
//        setupTopics();
//
//        // 设置发布日期
//        postDate.setText(formatDate(postDetail.getPublishDate()));
//
//        // 设置点赞状态
//        updateLikeStatus(isLiked);
//    }

    private void setupData() {
        try {
            Post post = (Post) getArguments().getSerializable(ARG_POST);
            if (post == null) {
                Log.e("PostDetailFragment", "post为null");
                return;
            }

            if (post.getAuthor() != null){
                // 设置作者信息
                authorName.setText(post.getAuthor().getNickname());
                // 设置作者头像 TODO 需要传入avatar头像吗
                loadAuthorAvatar();
            }

            // 检查是否已关注该作者
//            String authorId = post.getAuthor() != null ? post.getAuthor().getUser_id() : "unknown_author";
//            SharedPreferences prefs = getActivity().getSharedPreferences(PREF_FOLLOWED_AUTHORS, Context.MODE_PRIVATE);
//            isFollowing = prefs.getBoolean(authorId, false);
//            updateFollowButton();

            // 设置图片适配器
            setupImageAdapter();

            // 设置帖子内容
            postTitle.setText(post.getTitle());
            postContent.setText(post.getContent() != null ? post.getContent() : "暂无内容");

            // 设置话题标签
            // 简单实现话题标签显示
//        topicContainer.removeAllViews();
//        if (post.getHashtag() != null && !post.getHashtag().isEmpty()) {
//            TextView topicView = (TextView) LayoutInflater.from(getContext())
//                    .inflate(R.layout.item_topic, topicContainer, false);
//            topicView.setText("#" + post.getHashtag());
//            topicView.setOnClickListener(v -> handleTopicClick(post.getHashtag()));
//            topicContainer.addView(topicView);
//        } else {
//            TextView topicView = (TextView) LayoutInflater.from(getContext())
//                    .inflate(R.layout.item_topic, topicContainer, false);
//            topicView.setText("#生活");
//            topicView.setOnClickListener(v -> handleTopicClick("生活"));
//            topicContainer.addView(topicView);
//        }
            // TODO 如何利用网络中的hashtag
            setupTopics();

            // 设置发布日期
            // 设置发布日期
            long createTime = post.getCreate_time();
            Log.d("time", "setupData中的create_time: " + createTime);
            postDate.setText(formatDate(createTime));
//            postDate.setText(formatDate(post.getCreate_time()));
            // 检查并设置关注状态、点赞状态
            checkFollowStatus();
            checkLikeStatus();
            // 设置点赞状态和数量
//            updateLikeStatus(isLiked);
//            likeCount.setText(String.valueOf(currentLikeCount));
        } catch (Exception e) {
            Log.e("PostDetailFragment", "Error setting up data", e);

        }
    }

    // 添加加载作者头像的方法
    private void loadAuthorAvatar() {
        if (authorAvatar == null || getArguments() == null) return;

        // 获取post对象及其作者头像url
        Post post = (Post) getArguments().getSerializable(ARG_POST);
        if (post != null && post.getAuthor() != null && post.getAuthor().getAvatar() != null) {
            String avatarUrl = post.getAuthor().getAvatar();
            try {
                // 使用Glide加载头像
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.user_avatar)
                        .error(R.drawable.user_avatar)
                        .circleCrop()
                        .into(authorAvatar);
            }catch (Exception e) {
                setupDefaultAvatar();
            }
        }else {
            setupDefaultAvatar();
        }
    }

    // 提取默认头像设置为单独方法
    private void setupDefaultAvatar() {
        if (authorAvatar == null || getContext() == null) return;

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
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.user_avatar);
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
    private void setupListeners() {
        // 返回按钮
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // 关注按钮
        followButton.setOnClickListener(v -> toggleFollowStatus());

        // 点赞按钮
        likeButton.setOnClickListener(v -> toggleLikeStatus());

        // 收藏按钮
        collectButton.setOnClickListener(v -> toggleCollectStatus());

        // 分享按钮
        shareButton.setOnClickListener(v -> handleShare());
    }

    // 修改setupImageAdapter方法以使用Post.Clip
    private void setupImageAdapter() {
        Post post = (Post) getArguments().getSerializable(ARG_POST);
        if (post == null || post.getClips() == null || post.getClips().isEmpty()) {
            // 无图片状态
            progressBar.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            return;
        }

        // 多图时显示进度条
        if (post.getClips().size() > 1) {
            progressBar.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            updateProgress(0);
        }

        // 创建图片适配器
        ImagePagerAdapter adapter = new ImagePagerAdapter(post.getClips());
        imageViewPager.setAdapter(adapter);

        // 监听ViewPager页面变化
        imageViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateProgress(position);
            }
        });
    }

    // 修改updateProgress方法以适应实际的clips数量
    private void updateProgress(int position) {
        Post post = (Post) getArguments().getSerializable(ARG_POST);
        if (post != null && post.getClips() != null) {
            int total = post.getClips().size();
            progressText.setText((position + 1) + "/" + total);
            progressBar.setProgress((position + 1) * 100 / total);
        }
    }

    private void setupTopics() {
        topicContainer.removeAllViews();
        for (String topic : postDetail.getTopics()) {
            TextView topicView = (TextView) LayoutInflater.from(getContext())
                    .inflate(R.layout.item_topic, topicContainer, false);
            topicView.setText("#" + topic);
            topicView.setOnClickListener(v -> handleTopicClick(topic));
            topicContainer.addView(topicView);
        }
    }

    private void checkFollowStatus() {
        Post post = (Post) getArguments().getSerializable(ARG_POST);
        if (post == null) return;

        String authorId = post.getAuthor() != null ? post.getAuthor().getUser_id() : "unknown_author";
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_FOLLOWED_AUTHORS, Context.MODE_PRIVATE);
        isFollowing = prefs.getBoolean(authorId, false);
        updateFollowButton();
    }

    private void toggleFollowStatus() {
        Post post = (Post) getArguments().getSerializable(ARG_POST);
        if (post == null) return;
        isFollowing = !isFollowing;

        // 保存关注状态到本地存储
        String authorId = post.getAuthor() != null ? post.getAuthor().getUser_id() : "unknown_author";
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_FOLLOWED_AUTHORS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(authorId, isFollowing);
        editor.apply();

        // 更新按钮UI
        updateFollowButton();

        // 显示提示
        Toast.makeText(getContext(), isFollowing ? "已关注" : "已取消关注", Toast.LENGTH_SHORT).show();
    }

    private void updateFollowButton() {
        if (isFollowing) {
            followButton.setText("已关注");
            followButton.setBackgroundResource(R.drawable.followed_button_bg);
        } else {
            followButton.setText("关注");
            followButton.setBackgroundResource(R.drawable.follow_button_bg);
        }
    }

    private void toggleLikeStatus() {
        Post post = (Post) getArguments().getSerializable(ARG_POST);
        if (post == null) return;

        boolean oldLiked = isLiked;
        isLiked = !isLiked;
        currentLikeCount += isLiked ? 1 : -1;
        // 保存点赞状态到本地
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_LIKED_POSTS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(post.getPost_id(), isLiked);
        editor.apply();

        // 更新UI
        updateLikeStatus(isLiked);
        if (likeCount != null) {
            likeCount.setText(String.valueOf(currentLikeCount));
        }
        // 添加点赞动画
        animateLike(isLiked);
    }
    // 添加点赞动画方法
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

    private void updateLikeStatus(boolean liked) {
        if (likeIcon != null) {
            if (liked) {
                likeIcon.setImageResource(R.drawable.ic_s_s_heart_outlined_16);
                likeIcon.setColorFilter(Color.RED);
            } else {
                likeIcon.setImageResource(R.drawable.ic_s_s_heart_outlined_16);
                likeIcon.clearColorFilter();
            }
        }
    }


    // 读取点赞状态的方法
    private void checkLikeStatus() {
        Post post = (Post) getArguments().getSerializable(ARG_POST);
        if (post == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_LIKED_POSTS, Context.MODE_PRIVATE);
        // 如果本地没有记录，使用post对象的原始状态
        isLiked = prefs.getBoolean(post.getPost_id(), post.isLiked());
        currentLikeCount = post.getLikeCount();
        if (isLiked && !post.isLiked()) {
            currentLikeCount++;
        }else if (!isLiked && post.isLiked()){
            currentLikeCount--;
        }
        updateLikeStatus(isLiked);
        if (likeCount != null) {
            likeCount.setText(String.valueOf(currentLikeCount));
        }
    }
    private void toggleCollectStatus() {
        isCollected = !isCollected;
        collectIcon.setImageResource(isCollected ? R.drawable.ic_collected : R.drawable.ic_collect);
        Toast.makeText(getContext(), isCollected ? "已收藏" : "已取消收藏", Toast.LENGTH_SHORT).show();
    }

    private void handleShare() {
        // 模拟分享功能
        Toast.makeText(getContext(), "分享成功", Toast.LENGTH_SHORT).show();
    }

    private void handleTopicClick(String topic) {
        // 跳转到话题页面（这里简单显示Toast）
        Toast.makeText(getContext(), "查看话题：" + topic, Toast.LENGTH_SHORT).show();
    }

    private String formatDate(long timestamp) {
        Log.d("time", "原始timestamp: " + timestamp);

        // 检查时间戳是否可能是秒级别的（如果值太小）
        // 秒级时间戳通常在10^9左右，毫秒级在10^12左右
        if (timestamp < 10000000000L) {
            Log.d("time", "检测到可能的秒级时间戳，转换为毫秒");
            timestamp = timestamp * 1000; // 将秒转换为毫秒
        }

        Log.d("time", "处理后的timestamp: " + timestamp);

        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        Log.d("time", "当前时间: " + now);
        Log.d("time", "时间差: " + diff);

        // 24小时内
        if (diff < 24 * 60 * 60 * 1000) {
            // 获取当前日期的起始时间（今天0点）
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            long todayStartTime = today.getTimeInMillis();

            // 获取昨天的起始时间
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_MONTH, -1);
            yesterday.set(Calendar.HOUR_OF_DAY, 0);
            yesterday.set(Calendar.MINUTE, 0);
            yesterday.set(Calendar.SECOND, 0);
            yesterday.set(Calendar.MILLISECOND, 0);
            long yesterdayStartTime = yesterday.getTimeInMillis();

            if (timestamp >= todayStartTime) {
                // 今天
                String result = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
                Log.d("time", "返回今天格式: " + result);
                return result;
            } else if (timestamp >= yesterdayStartTime) {
                // 昨天
                String result = "昨天 " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
                Log.d("time", "返回昨天格式: " + result);
                return result;
            }
        }
        // 7天内
        else if (diff < 7 * 24 * 60 * 60 * 1000) {
            long days = diff / (24 * 60 * 60 * 1000);
            String result = days + "天前";
            Log.d("time", "返回几天前格式: " + result);
            return result;
        }
        // 其他情况
        else {
            String result = new SimpleDateFormat("MM-dd", Locale.getDefault()).format(new Date(timestamp));
            Log.d("time", "返回日期格式: " + result);
            return result;
        }
        // 默认情况（应该不会执行到这里）
        String result = new SimpleDateFormat("MM-dd", Locale.getDefault()).format(new Date(timestamp));
        Log.d("time", "返回默认日期格式: " + result);
        return result;
    }


    /**
     * 图片分页适配器
     * 用于显示帖子中的图片，支持分页查看
     * 修改，用于支持URL加载和比例控制
     */
    private class ImagePagerAdapter extends RecyclerView.Adapter<ImageViewHolder> {
        private List<Post.Clip> clips;
        // 默认比例
        private float aspectRatio = 1.0f;
        public ImagePagerAdapter(List<Post.Clip> clips) {
            this.clips = clips;
            // 计算首图的比例，并确保在3:4- 16:9之间
            if (clips != null && !clips.isEmpty()) {
                Post.Clip firstClip = clips.get(0);
                if (firstClip != null && firstClip.getWidth() > 0 && firstClip.getHeight() > 0) {
                    // 计算宽高比
                    float rawRatio = (float) firstClip.getWidth() / firstClip.getHeight();
                    // 限制在3:4(0.75)-16:9(1.777)之间
                    aspectRatio = Math.max(0.75f, Math.min(1.7778f, rawRatio));
                    // 应用比例到图片容器
                    updateImageContainerSize();
                }
            }
        }

        private void updateImageContainerSize() {
            if (getContext() == null) return;

            // 获取屏幕宽度
            DisplayMetrics displayMetrics = new DisplayMetrics();
            if (getActivity() != null) {
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            }
            int screenWidth = displayMetrics.widthPixels;

            // 根据比例计算高度
            int calculatedHeight = (int) (screenWidth / aspectRatio);

            // 找到图片容器并设置高度
            ViewGroup.LayoutParams layoutParams = imageViewPager.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.height = calculatedHeight;
                imageViewPager.setLayoutParams(layoutParams);
            }
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_page, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            if (clips != null && position < clips.size()) {
                holder.bind(clips.get(position), aspectRatio);
            }
        }

        @Override
        public int getItemCount() {
            return clips != null ? clips.size() : 0;
        }
    }

    private class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private ProgressBar loadingProgress;
        private ImageView errorImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.pager_image);
            loadingProgress = itemView.findViewById(R.id.loading_progress);
            errorImage = itemView.findViewById(R.id.error_image);
        }

        public void bind(Post.Clip clip, float aspectRatio) {
            if (clip == null) return;

            // 模拟图片加载
            loadingProgress.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            errorImage.setVisibility(View.GONE);

            // 使用更安全的Handler方式
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (imageView != null) {
                    // 使用Glide加载网络图片
                    if (clip.getUrl() != null && !clip.getUrl().isEmpty()) {
                        try {
                            Glide.with(itemView.getContext())
                                    .load(clip.getUrl())
                                    .placeholder(android.R.drawable.ic_dialog_info) // 加载占位图
                                    .error(android.R.drawable.ic_dialog_alert) // 错误占位图
                                    .centerCrop() // 确保图片内容充满容器
                                    .into(imageView);
                        } catch (Exception e) {
                            // 加载失败
                            if (imageView != null) imageView.setVisibility(View.GONE);
                            if (errorImage != null) errorImage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // 无URL时使用占位图
                        imageView.setImageResource(android.R.drawable.ic_dialog_info);
                    }

                    imageView.setVisibility(View.VISIBLE);
                }
                if (loadingProgress != null) {
                    loadingProgress.setVisibility(View.GONE);
                }
            }, 500);
        }
    }


    // 创建模拟的详情数据
    private PostDetail createMockPostDetail(Post post) {
        List<Integer> images = new ArrayList<>();
        images.add(android.R.drawable.ic_dialog_info); // 使用占位图
        images.add(android.R.drawable.ic_dialog_info);

        List<String> topics = new ArrayList<>();
        topics.add("摄影");
        topics.add("旅行");
        topics.add("生活");

        return new PostDetail(
                "author_" + post.getAuthor(),
                post.getAuthorName(),
                post.getTitle(),
                "这是作品的详细内容，这里可以展示完整的作品描述信息。" +
                        "用户可以在这里看到作者对作品的详细介绍。这是一个很长的文本，" +
                        "用于测试文本的完整展示不截断的效果。 " +
                        "这里是第二段内容，可以有多行文本，系统会自动处理换行。",
                System.currentTimeMillis() - (long)(Math.random() * 10 * 24 * 60 * 60 * 1000),
                images,
                topics
        );
    }

    // 获取真实数据
    private PostDetail getPostDetailFromServer(Post post) {
        // 使用实际作者id
        String authorId = post.getAuthor() != null ? post.getAuthor().getUser_id() : "unknown_author";
        // 使用post对象中的实际内容
        String actualContent = post.getContent();
        if (actualContent == null || actualContent.isEmpty()) {
            actualContent = "暂无内容";
        }
        List<String> topics = new ArrayList<>();
        if (post.getHashtag() != null && !post.getHashtag().isEmpty()) {
            // 有内容则提取文本标签
            // hashtag是start, end, 对应了什么？
            // TODO 先暂时写一个
            topics.add("摄影");
            topics.add("旅行");
        }else {
            topics.add("生活");
        }

        // 发布时间
        long publishDate = post.getCreate_time();

        // 由于PostDetail的images是int类型列表（资源ID），而Post的clips包含URL，这里需要适配
        // 我们暂时使用占位图，但在实际应用中可以考虑预加载图片或修改数据模型
        List<Integer> images = new ArrayList<>();
        if (post.getClips() != null && !post.getClips().isEmpty()) {
            // 为每个clip添加一个占位图
            for (int i = 0; i < post.getClips().size() && i < 3; i++) { // 最多显示3张图片
                images.add(android.R.drawable.ic_dialog_info);
            }
        } else {
            images.add(android.R.drawable.ic_dialog_info);
        }

        return new PostDetail(
                authorId,
                post.getAuthorName(),
                post.getTitle(),
                actualContent,
                publishDate,
                images,
                topics
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        // 隐藏底部导航栏
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNavigation(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // 显示底部导航栏
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNavigation(true);
        }
    }
}
