package com.example.myapplication.adapter;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.Post;

import java.util.List;

/**
 * 图片轮播适配器，用于在ViewPager2中显示帖子图片
 */
public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {
    private List<Post.Clip> clips;
    private Context context;
    private float aspectRatio = 1.0f;
    private String postId;

    public ImagePagerAdapter(Context context, List<Post.Clip> clips) {
        this(context, clips, null);
    }

    public ImagePagerAdapter(List<Post.Clip> clips, String postId) {
        // 这个构造函数用于兼容PostDetailFragment的调用方式
        // 注意：这里需要确保在使用时已经正确设置了context
        this.clips = clips;
        this.postId = postId;
        if (clips != null && !clips.isEmpty()) {
            Post.Clip firstClip = clips.get(0);
            if (firstClip != null && firstClip.getWidth() > 0 && firstClip.getHeight() > 0) {
                float rawRatio = (float) firstClip.getWidth() / firstClip.getHeight();
                aspectRatio = Math.max(0.75f, Math.min(1.7778f, rawRatio));
            }
        }
    }

    public ImagePagerAdapter(Context context, List<Post.Clip> clips, String postId) {
        this.context = context;
        this.clips = clips;
        this.postId = postId;
        if (clips != null && !clips.isEmpty()) {
            Post.Clip firstClip = clips.get(0);
            if (firstClip != null && firstClip.getWidth() > 0 && firstClip.getHeight() > 0) {
                float rawRatio = (float) firstClip.getWidth() / firstClip.getHeight();
                aspectRatio = Math.max(0.75f, Math.min(1.7778f, rawRatio));
            }
        }
    }

    public void updateClips(List<Post.Clip> newClips) {
        this.clips = newClips;
        if (newClips != null && !newClips.isEmpty()) {
            Post.Clip firstClip = newClips.get(0);
            if (firstClip != null && firstClip.getWidth() > 0 && firstClip.getHeight() > 0) {
                float rawRatio = (float) firstClip.getWidth() / firstClip.getHeight();
                aspectRatio = Math.max(0.75f, Math.min(1.7778f, rawRatio));
            }
        }
        notifyDataSetChanged();
    }

    public float getAspectRatio() {
        return aspectRatio;
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

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private ProgressBar loadingProgress;
        private ImageView errorImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.pager_image);
            loadingProgress = itemView.findViewById(R.id.loading_progress);
            errorImage = itemView.findViewById(R.id.error_image);
        }

        public void bind(Post.Clip clip, float aspectRatio, String postId) {
            if (clip == null) return;

            // 设置过渡名称，确保与HomeFragment中的图片过渡名称一致
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && postId != null) {
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
            }, 100);
        }
    }
}
