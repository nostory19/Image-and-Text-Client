package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.model.Post;
import com.example.myapplication.model.PostDetail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostDetailFragment extends Fragment {
    private static final String ARG_POST = "post";
    private static final String PREF_FOLLOWED_AUTHORS = "followed_authors";

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

        // 初始化视图组件
        initViews(view);

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
    private void setupData() {
        if (postDetail == null) return;

        // 设置作者信息
        authorName.setText(postDetail.getAuthorName());
        // 检查是否已关注该作者
        checkFollowStatus();

        // 设置图片适配器
        setupImageAdapter();

        // 设置帖子内容
        postTitle.setText(postDetail.getTitle());
        postContent.setText(postDetail.getContent());

        // 设置话题标签
        setupTopics();

        // 设置发布日期
        postDate.setText(formatDate(postDetail.getPublishDate()));

        // 设置点赞状态
        updateLikeStatus(isLiked);
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

    private void setupImageAdapter() {
        if (postDetail.getImages().isEmpty()) {
            // 无图片状态
            progressBar.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            return;
        }

        // 多图时显示进度条
        if (postDetail.getImages().size() > 1) {
            progressBar.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            updateProgress(0);
        }

        // 创建图片适配器
        ImagePagerAdapter adapter = new ImagePagerAdapter(postDetail.getImages());
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

    private void updateProgress(int position) {
        int total = postDetail.getImages().size();
        progressText.setText((position + 1) + "/" + total);
        progressBar.setProgress((position + 1) * 100 / total);
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
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_FOLLOWED_AUTHORS, Context.MODE_PRIVATE);
        isFollowing = prefs.getBoolean(postDetail.getAuthorId(), false);
        updateFollowButton();
    }

    private void toggleFollowStatus() {
        isFollowing = !isFollowing;

        // 保存关注状态到本地存储
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_FOLLOWED_AUTHORS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(postDetail.getAuthorId(), isFollowing);
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
        isLiked = !isLiked;
        currentLikeCount += isLiked ? 1 : -1;
        updateLikeStatus(isLiked);
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
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        // 24小时内
        if (diff < 24 * 60 * 60 * 1000) {
            if (diff < 60 * 60 * 1000) {
                // 今天
                return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
            } else {
                // 昨天
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
    }

    // 图片适配器
    private class ImagePagerAdapter extends RecyclerView.Adapter<ImageViewHolder> {
        private List<Integer> images;

        public ImagePagerAdapter(List<Integer> images) {
            this.images = images;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_page, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            holder.bind(images.get(position));
        }

        @Override
        public int getItemCount() {
            return images.size();
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

        public void bind(int imageResId) {
            // 模拟图片加载
            loadingProgress.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            errorImage.setVisibility(View.GONE);

            // 使用更安全的Handler方式
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (imageView != null) {
                    // 这里直接加载本地资源
                    imageView.setImageResource(imageResId);
                    imageView.setVisibility(View.VISIBLE);
                }
                if (loadingProgress != null) {
                    loadingProgress.setVisibility(View.GONE);
                }

                // 模拟加载失败的情况（暂时注释掉，避免随机崩溃）
                // if (Math.random() > 0.8) {
                //     if (imageView != null) imageView.setVisibility(View.GONE);
                //     if (errorImage != null) errorImage.setVisibility(View.VISIBLE);
                // }
            }, 1000);
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
                post.getAuthor(),
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
