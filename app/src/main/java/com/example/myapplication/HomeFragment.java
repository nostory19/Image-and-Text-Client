package com.example.myapplication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.myapplication.model.Post;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 初始化RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.waterfall_recycler);

        // 设置瀑布流布局管理器，2列
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        // 创建测试数据
        List<Post> posts = generateTestPosts(10);

        // 创建并设置适配器
        recyclerView.setAdapter(new PostAdapter(posts));

        return view;
    }

    // 生成测试数据
    private List<Post> generateTestPosts(int count) {
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            posts.add(new Post("作品标题 " + (i + 1), "作者" + (i + 1), false, i * 10));
        }
        return posts;
    }

    // 简单的Post数据模型类


    // 适配器类
    private class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {
        private List<Post> posts;

        public PostAdapter(List<Post> posts) {
            this.posts = posts;
        }

        @Override
        public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_card, parent, false);
            return new PostViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(PostViewHolder holder, int position) {
            Post post = posts.get(position);
            holder.bind(post, position);
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }
    }

    private class PostViewHolder extends RecyclerView.ViewHolder {
        private ImageView likeIcon;
        private TextView likeCount;
        private TextView postTitle;
        private TextView authorName;
        private View likeButton;
        private int position;
        private Post currentPost;

        public PostViewHolder(View itemView) {
            super(itemView);
            // 初始化所有视图引用
            likeButton = itemView.findViewById(R.id.like_button);
            likeIcon = itemView.findViewById(R.id.like_icon);
            likeCount = itemView.findViewById(R.id.like_count);
            postTitle = itemView.findViewById(R.id.post_title);
            authorName = itemView.findViewById(R.id.author_name);

            // 设置点赞按钮点击事件
            likeButton.setOnClickListener(v -> handleLikeClick());
        }

        // 绑定数据到视图
        public void bind(Post post, int pos) {
            this.currentPost = post;
            this.position = pos;
            
            // 设置内容
            postTitle.setText(post.getTitle());
            authorName.setText(post.getAuthor());
            likeCount.setText(String.valueOf(post.getLikeCount()));
            
            // 设置初始点赞状态
            updateLikeUI(post.isLiked());

            // 添加卡片跳转点击事件，跳转到详情页
            itemView.setOnClickListener(v -> {
                try {
                    // 使用getParentFragmentManager代替getFragmentManager
                    FragmentManager fragmentManager = getParentFragmentManager();
                    if (fragmentManager != null && currentPost != null) {
                        // 替换当前Fragment为详情页
                        PostDetailFragment fragment = PostDetailFragment.newInstance(currentPost);
                        fragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // 显示错误提示
                    Toast.makeText(getContext(), "跳转失败，请重试", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 处理点赞点击
        private void handleLikeClick() {
            if (currentPost == null) return;
            
            boolean isLiked = !currentPost.isLiked();
            currentPost.setLiked(isLiked);
            
            // 更新点赞数
            int newCount = currentPost.getLikeCount() + (isLiked ? 1 : -1);
            currentPost.setLikeCount(newCount);
            likeCount.setText(String.valueOf(newCount));
            
            // 更新UI和动画
            updateLikeUI(isLiked);
            animateLike(isLiked);
        }

        // 更新点赞UI状态
        private void updateLikeUI(boolean isLiked) {
            if (isLiked) {
                // 使用系统图标作为爱心图标，设置为红色
                likeIcon.setImageResource(R.drawable.ic_s_s_heart_outlined_16);
                likeIcon.setColorFilter(Color.RED);
            } else {
                // 未点赞状态，设置为灰色
                // 引入drawable资源
                likeIcon.setImageResource(R.drawable.ic_s_s_heart_outlined_16);
                likeIcon.setColorFilter(Color.GRAY);
            }
        }

        // 点赞动画效果
        private void animateLike(boolean isLiked) {
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
}