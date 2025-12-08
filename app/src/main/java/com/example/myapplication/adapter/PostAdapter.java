package com.example.myapplication.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.Post;
import com.example.myapplication.viewmodel.PostViewModel;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    private Context context;
    private List<Post> posts;
    private PostViewModel viewModel;
    private OnPostClickListener onPostClickListener;
    private boolean showFooter = false;

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }


    public PostAdapter(Context context, List<Post> posts, PostViewModel viewModel, OnPostClickListener listener) {
        this.context = context;
        this.posts = posts;
        this.viewModel = viewModel;
        this.onPostClickListener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == TYPE_FOOTER){
            return RecyclerView.NO_ID;
        }
        // 使用帖子的唯一ID作为稳定ID
        return posts.get(position).getPost_id().hashCode();
    }
    public void updateData(List<Post> newPosts) {
        this.posts = newPosts;
        notifyDataSetChanged();
    }
    public List<Post> getPosts() {
        return posts;
    }
    public void showLoadingFooter(boolean show) {
        if (showFooter != show) {
            showFooter = show;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showFooter && position == getItemCount() - 1) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_FOOTER) {
            View footerView = inflater.inflate(R.layout.item_loading_footer, parent, false);
            return new FooterViewHolder(footerView);
        }

        View itemView = inflater.inflate(R.layout.item_post_card, parent, false);
        return new PostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PostViewHolder) {
            PostViewHolder postViewHolder = (PostViewHolder) holder;
            Post post = posts.get(position);
            postViewHolder.bind(post, position);

            // 处理点击事件，
            postViewHolder.itemView.setOnClickListener(v -> {
                if (onPostClickListener != null) {
                    onPostClickListener.onPostClick(post);
                }
            });
        }

        // FooterViewHolder不需要绑定数据
    }

    @Override
    public int getItemCount() {
        return posts.size() + (showFooter ? 1 : 0);
    }


    // 普通帖子视图持有者
    public class PostViewHolder extends RecyclerView.ViewHolder {
        private ImageView likeIcon;
        private TextView likeCount;
        private TextView postTitle;
        private TextView authorName;
        private ImageView authorAvatar;
        public  ImageView postImage;
        private View likeButton;
        private int position;
        private Post currentPost;

        private String boundPostId; // 添加此字段跟踪当前绑定的帖子ID
        private AnimatorSet currentAnimator; // 添加此字段跟踪当前执行的动画

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            // 初始化所有视图引用
            likeButton = itemView.findViewById(R.id.like_button);
            likeIcon = itemView.findViewById(R.id.like_icon);
            likeCount = itemView.findViewById(R.id.like_count);
            postTitle = itemView.findViewById(R.id.post_title);
            authorName = itemView.findViewById(R.id.author_name);
            authorAvatar = itemView.findViewById(R.id.author_avatar);
            postImage = itemView.findViewById(R.id.post_image);

            // 设置点赞按钮点击事件
            likeButton.setOnClickListener(v -> handleLikeClick());

            // 设置卡片点击事件
            itemView.setOnClickListener(v -> {
                if (onPostClickListener != null && currentPost != null) {
                    onPostClickListener.onPostClick(currentPost);
                }
            });
        }

        public void bind(Post post, int pos) {
            this.currentPost = post;
            this.position = pos;
            this.boundPostId = post.getPost_id(); // 绑定帖子ID
            // 取消之前可能正在执行的动画
            if (currentAnimator != null) {
                currentAnimator.cancel();
                currentAnimator = null;
            }

            // 设置内容
            postTitle.setText(post.getTitle());
            authorName.setText(post.getAuthorName());
            likeCount.setText(String.valueOf(post.getLikeCount()));
            // 设置图片过渡名称
            postImage.setTransitionName("post_image_" + post.getPost_id());

            // 加载用户头像
            if (post.getAuthor() != null && post.getAuthor().getAvatar() != null && !post.getAuthor().getAvatar().isEmpty()) {
                try {
                    Glide.with(context)
                            .load(post.getAuthor().getAvatar())
                            .placeholder(R.drawable.user_avatar)
                            .error(R.drawable.user_avatar)
                            .override(64, 64)
                            .circleCrop()
                            .into(authorAvatar);
                } catch (Exception e) {
                    authorAvatar.setImageResource(R.drawable.user_avatar);
                }
            } else {
                authorAvatar.setImageResource(R.drawable.user_avatar);
            }

            // 加载帖子图片
            if (post.getClips() != null && !post.getClips().isEmpty()) {
                // 加载第一个图片
                Post.Clip firstClip = post.getClips().get(0);
                if (firstClip.getType() == 0 && firstClip.getUrl() != null) {
                    // 设置宽高比限制：3:4 到 4:3
                    if (firstClip.getWidth() > 0 && firstClip.getHeight() > 0) {
                        // 计算屏幕宽度的一半作为图片宽度（考虑双列布局）
                        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                        int imageWidth = (screenWidth - 16) / 2; // 减去间距

                        // 计算原始宽高比
                        float rawAspectRatio = (float) firstClip.getWidth() / firstClip.getHeight();

                        // 限制宽高比在3:4 (0.75) 到 4:3 (1.333) 之间
                        float clampedAspectRatio = Math.max(0.75f, Math.min(1.333f, rawAspectRatio));

                        // 根据限制后的宽高比计算高度
                        int imageHeight = (int) (imageWidth / clampedAspectRatio);

                        // 设置图片布局参数
                        ViewGroup.LayoutParams layoutParams = postImage.getLayoutParams();
                        layoutParams.width = imageWidth;
                        layoutParams.height = imageHeight;
                        postImage.setLayoutParams(layoutParams);
                    }

                    Glide.with(context)
                            .load(firstClip.getUrl())
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_foreground)
                            .centerCrop()
                            .into(postImage);
                }
            }

            // 设置初始点赞状态
            updateLikeUI(post.isLiked());
        }

        private void handleLikeClick() {
            if (currentPost == null || viewModel == null) return;

            boolean isLiked = !currentPost.isLiked();

            // 先保存点赞状态到ViewModel
            viewModel.saveLikeStatus(currentPost.getPost_id(), isLiked);

            // 更新UI状态
            updateLikeUI(isLiked);

            // 执行点赞动画
            animateLike(isLiked);
        }

        private void updateLikeUI(boolean isLiked) {
            if (currentPost == null) return;

            // 更新点赞图标状态
            if (isLiked) {
                likeIcon.setImageResource(R.drawable.ic_s_s_heart_outlined_16);
                likeIcon.setColorFilter(Color.RED);
            } else {
                likeIcon.setImageResource(R.drawable.ic_s_s_heart_outlined_16);
                likeIcon.clearColorFilter();
            }

            // 直接从currentPost获取最新的点赞数
            likeCount.setText(String.valueOf(currentPost.getLikeCount()));
        }
        // 保留原有的updateLikeUI方法，用于bind时调用

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
        }}

    // Footer视图持有者
    class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


}