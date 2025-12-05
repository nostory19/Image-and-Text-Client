package com.example.myapplication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.myapplication.model.Post;
import com.example.myapplication.viewmodel.PostDetailViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import androidx.lifecycle.LiveData;

// 废弃代码
@Deprecated
public class PostDetailFragment extends Fragment {
    private static final String ARG_POST = "post";

    private PostDetailViewModel viewModel;
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
    private LinearLayout likeButton;
    private ImageView likeIcon;
    private LinearLayout commentButton;
    private ImageView commentIcon;
    private LinearLayout collectButton;
    private ImageView collectIcon;
    private LinearLayout shareButton;
    private ImageView shareIcon;
    private TextView likeCount;

    private LinearLayout progressDotsContainer;

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
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);

        // 从参数中获取Post对象并设置给ViewModel
        if (getArguments() != null) {
            Post post = (Post) getArguments().getSerializable(ARG_POST);
            if (post != null) {
                viewModel.setPost(post);
            }
        }

        // 添加入场动画配置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(new DetailsTransition());
            setSharedElementReturnTransition(new DetailsTransition());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        // 设置退出时的转场动画
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            setSharedElementReturnTransition(new DetailsTransition());
//        }
        return inflater.inflate(R.layout.fragment_post_detail, container, false);
    }
    // 添加DetailsTransition内部类
    public static class DetailsTransition extends TransitionSet {
        public DetailsTransition() {
            setOrdering(ORDERING_TOGETHER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                addTransition(new ChangeBounds()).
                        addTransition(new ChangeTransform()).
                        addTransition(new ChangeImageTransform());
                setDuration(500);
                setInterpolator(new AccelerateDecelerateInterpolator()); // 平滑的加速减速插值器
            }
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化视图组件
        initViews(view);
// 设置imageViewPager的过渡名称，确保与HomeFragment中的一致
        if (getArguments() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Post post = (Post) getArguments().getSerializable(ARG_POST);
            if (post != null) {
                imageViewPager.setTransitionName("shared_image_" + post.getPost_id());
            }
        }
        // 让其他元素（作者信息、内容等）开始时透明，然后淡入
        authorName.setAlpha(0f);
        postTitle.setAlpha(0f);
        postContent.setAlpha(0f);
        likeButton.setAlpha(0f);
        commentButton.setAlpha(0f);
        // 延迟显示其他元素，突出图片的转场动画
        // 延迟显示其他元素，突出图片的转场动画
        new Handler().postDelayed(() -> {
            // 创建淡入动画
            AnimatorSet animatorSet = new AnimatorSet();

            // 直接使用可变参数方式添加动画，避免类型转换问题
            animatorSet.playTogether(
                    ObjectAnimator.ofFloat(authorName, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(postTitle, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(postContent, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(likeButton, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(commentButton, "alpha", 0f, 1f)
            );

            // 设置动画时间和启动
            animatorSet.setDuration(500);
            animatorSet.start();
        }, 300);
        // 设置观察者，监听数据变化
        observeViewModel();

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
//        progressText = view.findViewById(R.id.progress_text);
//        progressBar = view.findViewById(R.id.progress_bar);
        progressDotsContainer = view.findViewById(R.id.progress_dots_container);

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

    private void observeViewModel() {
        // 观察帖子数据变化
        viewModel.getPostLiveData().observe(getViewLifecycleOwner(), post -> {
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
        viewModel.getIsFollowingLiveData().observe(getViewLifecycleOwner(), isFollowing -> {
            updateFollowButton(isFollowing);
        });

        // 观察点赞状态变化
        viewModel.getIsLikedLiveData().observe(getViewLifecycleOwner(), isLiked -> {
            updateLikeStatus(isLiked);
            // 添加点赞动画
            animateLike(isLiked);
        });

        // 观察点赞数变化
        viewModel.getLikeCountLiveData().observe(getViewLifecycleOwner(), count -> {
            likeCount.setText(String.valueOf(count));
        });

        // 观察收藏状态变化
        viewModel.getIsCollectedLiveData().observe(getViewLifecycleOwner(), isCollected -> {
            collectIcon.setImageResource(isCollected ? R.drawable.ic_collected : R.drawable.ic_collect);
        });
    }

    private void setupListeners() {
        // 返回按钮
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // 关注按钮
        followButton.setOnClickListener(v -> {
            viewModel.toggleFollowStatus();
            Toast.makeText(getContext(), viewModel.getIsFollowingLiveData().getValue() ? "已关注" : "已取消关注", Toast.LENGTH_SHORT).show();
        });

        // 点赞按钮
        likeButton.setOnClickListener(v -> {
            viewModel.toggleLikeStatus();
        });

        // 收藏按钮
        collectButton.setOnClickListener(v -> {
            viewModel.toggleCollectStatus();
            Toast.makeText(getContext(), viewModel.getIsCollectedLiveData().getValue() ? "已收藏" : "已取消收藏", Toast.LENGTH_SHORT).show();
        });

        // 分享按钮
        shareButton.setOnClickListener(v -> handleShare());
    }

    // 其他辅助方法（保持不变）
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

    private void loadAuthorAvatar(Post post) {
        if (authorAvatar == null || post == null || post.getAuthor() == null) return;

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

    private void setupImageAdapter(Post post) {
        if (post == null || post.getClips() == null || post.getClips().isEmpty()) {
            // 无图片状态
//            progressBar.setVisibility(View.GONE);
            progressDotsContainer.setVisibility(View.GONE);
//            progressText.setVisibility(View.GONE);
            return;
        }

        // 多图时显示进度条
        if (post.getClips().size() > 1) {
//            progressBar.setVisibility(View.VISIBLE);
            progressDotsContainer.setVisibility(View.VISIBLE);
//            progressText.setVisibility(View.VISIBLE);

            // 初始化小圆点指示器
            initProgressDots(post.getClips().size());
            updateProgress(0);
        }

        // 创建图片适配器
        ImagePagerAdapter adapter = new ImagePagerAdapter(post.getClips(), post.getPost_id());
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


    // 添加一个新方法来初始化小圆点指示器
    private void initProgressDots(int count) {
        // 清除现有指示器
        progressDotsContainer.removeAllViews();

        // 设置weightSum，以便平均分配空间
        progressDotsContainer.setWeightSum(count);

        // 根据图片数量创建条状物指示器
        for (int i = 0; i < count; i++) {
            View dotView = new View(getContext());
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
        Post post = viewModel.getPostLiveData().getValue();
        if (post != null && post.getClips() != null) {
            int total = post.getClips().size();
//            progressText.setText((position + 1) + "/" + total);
//            progressBar.setProgress((position + 1) * 100 / total);
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
            TextView topicView = (TextView) LayoutInflater.from(getContext())
                    .inflate(R.layout.item_topic, topicContainer, false);
            topicView.setText("#" + topic);
            topicView.setOnClickListener(v -> handleTopicClick(topic));
            topicContainer.addView(topicView);
        }
    }

    private void updateFollowButton(boolean isFollowing) {
        if (isFollowing) {
            followButton.setText("已关注");
//            followButton.setBackgroundResource(R.drawable.followed_button_bg);
        } else {
            followButton.setText("关注");
//            followButton.setBackgroundResource(R.drawable.follow_button_bg);
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

    private void handleShare() {
        Toast.makeText(getContext(), "分享成功", Toast.LENGTH_SHORT).show();
    }

    private void handleTopicClick(String topic) {
        Toast.makeText(getContext(), "查看话题：" + topic, Toast.LENGTH_SHORT).show();
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

    // ImagePagerAdapter和ImageViewHolder类保持不变
    private class ImagePagerAdapter extends RecyclerView.Adapter<ImageViewHolder> {
        private List<Post.Clip> clips;
        private float aspectRatio = 1.0f;

        private String postId;

        public ImagePagerAdapter(List<Post.Clip> clips, String postId) {
            this.clips = clips;
            this.postId = postId;

            if (clips != null && !clips.isEmpty()) {
                Post.Clip firstClip = clips.get(0);
                if (firstClip != null && firstClip.getWidth() > 0 && firstClip.getHeight() > 0){
                    float rawRatio = (float) firstClip.getWidth() / firstClip.getHeight();
                    aspectRatio = Math.max(0.75f, Math.min(1.7778f, rawRatio));
                    updateImageContainerSize();
                }
            }
        }

        private void updateImageContainerSize() {
            if (getContext() == null) return;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            if (getActivity() != null) {
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            }
            int screenWidth = displayMetrics.widthPixels;
            int calculatedHeight = (int) (screenWidth / aspectRatio);

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
                holder.bind(clips.get(position), aspectRatio, postId);
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
        private static final String TAG = "ImageViewHolder";
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.pager_image);
            loadingProgress = itemView.findViewById(R.id.loading_progress);
            errorImage = itemView.findViewById(R.id.error_image);
        }

        public void bind(Post.Clip clip, float aspectRatio, String postId) {
            if (clip == null) return;
// 设置过渡名称，确保与HomeFragment中的图片过渡名称一致
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView.setTransitionName("shared_image_" + postId);
            }
            loadingProgress.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            errorImage.setVisibility(View.GONE);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (imageView != null) {
                    if (clip.getUrl() != null && !clip.getUrl().isEmpty()) {
                        try {
                            Glide.with(itemView.getContext())
                                    .load(clip.getUrl())
                                    .placeholder(android.R.drawable.ic_dialog_info)
                                    .error(android.R.drawable.ic_dialog_alert)
                                    .centerCrop()
                                    .into(imageView);
                        } catch (Exception e) {
                            if (imageView != null) imageView.setVisibility(View.GONE);
                            if (errorImage != null) errorImage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        imageView.setImageResource(android.R.drawable.ic_dialog_info);
                    }
                    imageView.setVisibility(View.VISIBLE);
                }
                if (loadingProgress != null) {
                    loadingProgress.setVisibility(View.GONE);
                }
            }, 500);


            // 设置过渡名称
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView.setTransitionName("shared_image_" + postId);
            }
        }
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