package com.example.myapplication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.AsyncTask;
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

import com.bumptech.glide.Glide;
import com.example.myapplication.model.Post;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;

    private List<Post> posts = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 初始化RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.waterfall_recycler);

        // 设置瀑布流布局管理器，2列
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        // 创建测试数据
//        List<Post> posts = generateTestPosts(10);
        // 创建并设置适配器
//        recyclerView.setAdapter(new PostAdapter(posts));

        // 改为真实数据
        postAdapter = new PostAdapter(posts);
        recyclerView.setAdapter(postAdapter);
        // 获取网络数据
        fetchDataFromApi();

        return view;
    }

    // 以前旧的生成测试数据
//    private List<Post> generateTestPosts(int count) {
//        List<Post> posts = new ArrayList<>();
//        for (int i = 0; i < count; i++) {
//            posts.add(new Post("作品标题 " + (i + 1), "作者" + (i + 1), false, i * 10));
//        }
//        return posts;
//    }

    // 现在新的适配的生成测试数据
    private List<Post> generateTestPosts(int count) {
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Post.Author author = new Post.Author();
            author.setNickname("作者" + (i + 1));

            Post post = new Post(
                    "test_" + i,
                    "作品标题 " + (i + 1),
                    "测试内容 " + (i + 1),
                    new ArrayList<>(),
                    System.currentTimeMillis(),
                    author,
                    new ArrayList<>(),
                    new Post.Music()
            );
            post.setLikeCount(i * 10);
            posts.add(post);
        }
        return posts;
    }

    // 简单的Post数据模型类

    // 添加获取网络数据
    private void fetchDataFromApi() {
        new AsyncTask<Void, Void, List<Post>>() {
            @Override
            protected List<Post> doInBackground(Void... voids) {
                try {
                    URL url = new URL("https://college-training-camp.bytedance.com/feed/?count=10&accept_video_clip=false");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        inputStream.close();

                        // 解析JSON数据
                        return parseJsonResponse(response.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(List<Post> result) {
                if (result != null && !result.isEmpty()) {
                    posts.clear();
                    posts.addAll(result);
                    postAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "获取数据失败，显示测试数据", Toast.LENGTH_SHORT).show();
                    posts.clear();
                    posts.addAll(generateTestPosts(10));
                    postAdapter.notifyDataSetChanged();
                }
            }
        }.execute();
    }

    // 解析JSON响应
    private List<Post> parseJsonResponse(String jsonResponse) {
        List<Post> postList = new ArrayList<>();
        try {
            Gson gson = new Gson();
            JsonObject rootObject = gson.fromJson(jsonResponse, JsonObject.class);

            // 检查状态码
            if (rootObject.has("status_code") && rootObject.get("status_code").getAsInt() == 0) {
                JsonArray postsArray = rootObject.getAsJsonArray("post_list");

                for (int i = 0; i < postsArray.size(); i++) {
                    JsonObject postObj = postsArray.get(i).getAsJsonObject();

                    // 解析作者信息
                    Post.Author author = new Post.Author();
                    if (postObj.has("author")) {
                        JsonObject authorObj = postObj.getAsJsonObject("author");
                        if (authorObj.has("user_id"))
                            author.setUser_id(authorObj.get("user_id").getAsString());
                        if (authorObj.has("nickname"))
                            author.setNickname(authorObj.get("nickname").getAsString());
                        if (authorObj.has("avatar"))
                            author.setAvatar(authorObj.get("avatar").getAsString());
                    }

                    // 解析clips
                    List<Post.Clip> clips = new ArrayList<>();
                    if (postObj.has("clips")) {
                        JsonArray clipsArray = postObj.getAsJsonArray("clips");
                        for (int j = 0; j < clipsArray.size(); j++) {
                            JsonObject clipObj = clipsArray.get(j).getAsJsonObject();
                            Post.Clip clip = new Post.Clip();
                            if (clipObj.has("type")) clip.setType(clipObj.get("type").getAsInt());
                            if (clipObj.has("width"))
                                clip.setWidth(clipObj.get("width").getAsInt());
                            if (clipObj.has("height"))
                                clip.setHeight(clipObj.get("height").getAsInt());
                            if (clipObj.has("url")) clip.setUrl(clipObj.get("url").getAsString());
                            clips.add(clip);
                        }
                    }

                    // 解析music
                    Post.Music music = new Post.Music();
                    if (postObj.has("music")) {
                        JsonObject musicObj = postObj.getAsJsonObject("music");
                        if (musicObj.has("volume"))
                            music.setVolume(musicObj.get("volume").getAsInt());
                        if (musicObj.has("seek_time"))
                            music.setSeek_time(musicObj.get("seek_time").getAsInt());
                        if (musicObj.has("url")) music.setUrl(musicObj.get("url").getAsString());
                    }

                    // 解析hashtag
                    List<Post.Hashtag> hashtags = new ArrayList<>();
                    if (postObj.has("hashtag")) {
                        JsonArray hashtagArray = postObj.getAsJsonArray("hashtag");
                        for (int j = 0; j < hashtagArray.size(); j++) {
                            JsonObject hashtagObj = hashtagArray.get(j).getAsJsonObject();
                            Post.Hashtag hashtag = new Post.Hashtag();
                            if (hashtagObj.has("start"))
                                hashtag.setStart(hashtagObj.get("start").getAsInt());
                            if (hashtagObj.has("end"))
                                hashtag.setEnd(hashtagObj.get("end").getAsInt());
                            hashtags.add(hashtag);
                        }
                    }

                    // 创建Post对象
                    Post post = new Post(
                            postObj.has("post_id") ? postObj.get("post_id").getAsString() : "",
                            postObj.has("title") ? postObj.get("title").getAsString() : "",
                            postObj.has("content") ? postObj.get("content").getAsString() : "",
                            hashtags,
                            postObj.has("create_time") ? postObj.get("create_time").getAsLong() : 0,
                            author,
                            clips,
                            music
                    );

                    postList.add(post);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return postList;
    }

    // 适配器类，修改使其支持加载图片
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

        /**
         * 作品关联的图片/或视频片段实体类
         */
        private ImageView postImage;
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
            postImage = itemView.findViewById(R.id.post_image);

            // 设置点赞按钮点击事件
            likeButton.setOnClickListener(v -> handleLikeClick());
        }

        // 绑定数据到视图
        public void bind(Post post, int pos) {
            this.currentPost = post;
            this.position = pos;

            // 设置内容
            postTitle.setText(post.getTitle());
            authorName.setText(post.getAuthorName());
            likeCount.setText(String.valueOf(post.getLikeCount()));
            // 设置图片
            if (post.getClips() != null && !post.getClips().isEmpty()) {
                // 加载第一张图片
                Post.Clip firstClip = post.getClips().get(0);
                if (firstClip.getType() == 0 && firstClip.getUrl() != null) {
                    // 图片类型
                    if (getContext() != null) {
                        Glide.with(itemView.getContext())
                                .load(firstClip.getUrl())
                                .placeholder(R.drawable.ic_launcher_background) // 站位图
                                .error(R.drawable.ic_launcher_foreground) // 错位图
                                .centerCrop()
                                .into(postImage);
                    }

                }
            }
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