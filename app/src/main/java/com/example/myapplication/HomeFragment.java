package com.example.myapplication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.os.Build;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


public class HomeFragment extends Fragment {
    // 有一个点赞状态的常量
    private static final String PREF_LIKED_POSTS = "liked_posts";

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;

    private List<Post> posts = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化数据列表
        posts = new ArrayList<>();
        // 在Fragement创建时获取数据，而不是每次创建视图时获取
        fetchDataFromApi();
    }
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
//        fetchDataFromApi();

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
        Log.d("HomeFragment", "开始获取网络数据...");
        new AsyncTask<Void, Void, List<Post>>() {
            @Override
            protected List<Post> doInBackground(Void... voids) {
                try {
                    // 直接使用URL和HttpURLConnection，不进行复杂的SSL配置
                    URL url = new URL("https://college-training-camp.bytedance.com/feed/?count=10&accept_video_clip=false");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Content-Type", "application/json");
                    // 允许重定向
                    connection.setInstanceFollowRedirects(true);

                    int responseCode = connection.getResponseCode();
                    Log.d("HomeFragment", "响应码: " + responseCode);

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        inputStream.close();

                        String responseString = response.toString();
                        Log.d("HomeFragment", "响应数据: " + responseString);

                        // 解析JSON数据
                        return parseJsonResponse(responseString);
                    } else {
                        Log.e("HomeFragment", "网络请求失败，响应码: " + responseCode);
                        // 尝试获取错误流
                        try {
                            InputStream errorStream = connection.getErrorStream();
                            if (errorStream != null) {
                                BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"));
                                StringBuilder errorResponse = new StringBuilder();
                                String errorLine;
                                while ((errorLine = errorReader.readLine()) != null) {
                                    errorResponse.append(errorLine);
                                }
                                errorReader.close();
                                errorStream.close();
                                Log.e("HomeFragment", "错误响应: " + errorResponse.toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    Log.e("HomeFragment", "网络请求异常", e);
                }

                return null;
            }

            @Override
            protected void onPostExecute(List<Post> result) {
                if (isAdded() && getActivity() != null) {
                    if (result != null && !result.isEmpty()) {
                        posts.clear();
                        posts.addAll(result);

                        // 应用本地存储的点赞状态到新获取的数据
                        applyLocalLikeStatus();

                        postAdapter.notifyDataSetChanged();
                    } else {
                        // 如果获取数据失败，显示测试数据
                        posts.clear();
                        posts.addAll(generateTestPosts(10));
                        postAdapter.notifyDataSetChanged();
                    }
                }
            }
        }.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 从详情页返回时，应用本地存储的点赞状态
        applyLocalLikeStatus();
        // 如果适配器存在，通知数据更新
        if (postAdapter != null) {
            postAdapter.notifyDataSetChanged();
        }
    }
    // 添加方法应用本地点赞状态
    private void applyLocalLikeStatus() {
        if (getActivity() == null || posts.isEmpty()) return;

        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_LIKED_POSTS, Context.MODE_PRIVATE);

        for (Post post : posts) {
            boolean isLikedLocally = prefs.getBoolean(post.getPost_id(), post.isLiked());

            // 如果本地存储的点赞状态与当前状态不一致，更新点赞数
            if (isLikedLocally != post.isLiked()) {
                post.setLiked(isLikedLocally);
                // 调整点赞数
                post.setLikeCount(post.getLikeCount() + (isLikedLocally ? 1 : -1));
            }
        }
    }
//    private void fetchDataFromApi() {
//        new AsyncTask<Void, Void, List<Post>>() {
//            @Override
//            protected List<Post> doInBackground(Void... voids) {
//                try {
//                    URL url = new URL("https://college-training-camp.bytedance.com/feed/?count=10&accept_video_clip=false");
//                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                    connection.setRequestMethod("GET");
//                    connection.setConnectTimeout(10000);
//                    connection.setReadTimeout(10000);
//
//                    int responseCode = connection.getResponseCode();
//                    if (responseCode == HttpURLConnection.HTTP_OK) {
//                        InputStream inputStream = connection.getInputStream();
//                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//                        StringBuilder response = new StringBuilder();
//                        String line;
//                        while ((line = reader.readLine()) != null) {
//                            response.append(line);
//                        }
//                        reader.close();
//                        inputStream.close();
//
//                        // 解析JSON数据
//                        return parseJsonResponse(response.toString());
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(List<Post> result) {
//                if (result != null && !result.isEmpty()) {
//                    posts.clear();
//                    posts.addAll(result);
//                    postAdapter.notifyDataSetChanged();
//                } else {
//                    Toast.makeText(getContext(), "获取数据失败，显示测试数据", Toast.LENGTH_SHORT).show();
//                    posts.clear();
//                    posts.addAll(generateTestPosts(10));
//                    postAdapter.notifyDataSetChanged();
//                }
//            }
//        }.execute();
//    }

    // 解析JSON响应
    // 修改parseJsonResponse方法，添加更严格的null检查
    private List<Post> parseJsonResponse(String jsonResponse) {
//        Log.d("HomeFragment", "开始解析JSON响应: " + jsonResponse);
        List<Post> postList = new ArrayList<>();
        try {
            Gson gson = new Gson();
            JsonObject rootObject = gson.fromJson(jsonResponse, JsonObject.class);

            // 检查状态码
            if (rootObject.has("status_code") && rootObject.get("status_code").getAsInt() == 0) {
                // 安全地获取post_list数组
                if (rootObject.has("post_list") && !rootObject.get("post_list").isJsonNull()) {
                    JsonArray postsArray = rootObject.getAsJsonArray("post_list");
                    Log.d("HomeFragment", "解析到的帖子数量: " + postsArray.size());

                    for (int i = 0; i < postsArray.size(); i++) {
                        JsonObject postObj = postsArray.get(i).getAsJsonObject();

                        // 解析作者信息
                        Post.Author author = new Post.Author();
                        if (postObj.has("author") && !postObj.get("author").isJsonNull()) {
                            JsonObject authorObj = postObj.getAsJsonObject("author");
                            if (authorObj.has("user_id") && !authorObj.get("user_id").isJsonNull())
                                author.setUser_id(authorObj.get("user_id").getAsString());
                            if (authorObj.has("nickname") && !authorObj.get("nickname").isJsonNull())
                                author.setNickname(authorObj.get("nickname").getAsString());
                            if (authorObj.has("avatar") && !authorObj.get("avatar").isJsonNull())
                                author.setAvatar(authorObj.get("avatar").getAsString());
                        }

                        // 解析clips，添加严格的null检查
                        List<Post.Clip> clips = new ArrayList<>();
                        if (postObj.has("clips") && !postObj.get("clips").isJsonNull() && postObj.get("clips").isJsonArray()) {
                            JsonArray clipsArray = postObj.getAsJsonArray("clips");
                            for (int j = 0; j < clipsArray.size(); j++) {
                                if (!clipsArray.get(j).isJsonNull()) {
                                    JsonObject clipObj = clipsArray.get(j).getAsJsonObject();
                                    Post.Clip clip = new Post.Clip();
                                    if (clipObj.has("type") && !clipObj.get("type").isJsonNull())
                                        clip.setType(clipObj.get("type").getAsInt());
                                    if (clipObj.has("width") && !clipObj.get("width").isJsonNull())
                                        clip.setWidth(clipObj.get("width").getAsInt());
                                    if (clipObj.has("height") && !clipObj.get("height").isJsonNull())
                                        clip.setHeight(clipObj.get("height").getAsInt());
                                    if (clipObj.has("url") && !clipObj.get("url").isJsonNull())
                                        clip.setUrl(clipObj.get("url").getAsString());
                                    clips.add(clip);
                                }
                            }
                        }

                        // 解析music
                        Post.Music music = new Post.Music();
                        if (postObj.has("music") && !postObj.get("music").isJsonNull()) {
                            JsonObject musicObj = postObj.getAsJsonObject("music");
                            if (musicObj.has("volume") && !musicObj.get("volume").isJsonNull())
                                music.setVolume(musicObj.get("volume").getAsInt());
                            if (musicObj.has("seek_time") && !musicObj.get("seek_time").isJsonNull())
                                music.setSeek_time(musicObj.get("seek_time").getAsInt());
                            if (musicObj.has("url") && !musicObj.get("url").isJsonNull())
                                music.setUrl(musicObj.get("url").getAsString());
                        }

                        // 解析hashtag，添加严格的null检查
                        List<Post.Hashtag> hashtags = new ArrayList<>();
                        if (postObj.has("hashtag") && !postObj.get("hashtag").isJsonNull() && postObj.get("hashtag").isJsonArray()) {
                            JsonArray hashtagArray = postObj.getAsJsonArray("hashtag");
                            for (int j = 0; j < hashtagArray.size(); j++) {
                                if (!hashtagArray.get(j).isJsonNull()) {
                                    JsonObject hashtagObj = hashtagArray.get(j).getAsJsonObject();
                                    Post.Hashtag hashtag = new Post.Hashtag();
                                    if (hashtagObj.has("start") && !hashtagObj.get("start").isJsonNull())
                                        hashtag.setStart(hashtagObj.get("start").getAsInt());
                                    if (hashtagObj.has("end") && !hashtagObj.get("end").isJsonNull())
                                        hashtag.setEnd(hashtagObj.get("end").getAsInt());
                                    hashtags.add(hashtag);
                                }
                            }
                        }

                        // 创建Post对象，为所有字段添加null检查
                        String postId = postObj.has("post_id") && !postObj.get("post_id").isJsonNull() ? postObj.get("post_id").getAsString() : "";
                        String title = postObj.has("title") && !postObj.get("title").isJsonNull() ? postObj.get("title").getAsString() : "";
                        String content = postObj.has("content") && !postObj.get("content").isJsonNull() ? postObj.get("content").getAsString() : "";
                        long createTime = postObj.has("create_time") && !postObj.get("create_time").isJsonNull() ? postObj.get("create_time").getAsLong() : 0;

                        Post post = new Post(postId, title, content, hashtags, createTime, author, clips, music);

                        // 添加解析like_count和is_liked字段，带null检查
                        if (postObj.has("like_count") && !postObj.get("like_count").isJsonNull()) {
                            post.setLikeCount(postObj.get("like_count").getAsInt());
                        }
                        if (postObj.has("is_liked") && !postObj.get("is_liked").isJsonNull()) {
                            post.setLiked(postObj.get("is_liked").getAsBoolean());
                        }

                        postList.add(post);
                        Log.d("HomeFragment", "成功创建帖子对象: " + post.getTitle());
                    }
                }
            } else {
                int statusCode = rootObject.has("status_code") && !rootObject.get("status_code").isJsonNull() ?
                        rootObject.get("status_code").getAsInt() : -1;
                Log.e("HomeFragment", "API返回错误状态码: " + statusCode);
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "JSON解析异常", e);
        }
        Log.d("HomeFragment", "解析完成，返回帖子列表大小: " + postList.size());
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

    /**
     * 帖子卡片视图持有者类，负责绑定数据到视图
     *
     * 记得添加author中的author_avatar的引用和加载逻辑
     */
    private class PostViewHolder extends RecyclerView.ViewHolder {
        private ImageView likeIcon;
        private TextView likeCount;
        private TextView postTitle;
        /**
         * 帖子作者相关信息
         */
        private TextView authorName;
        private ImageView authorAvatar;
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
            // 在item_post_card.xml中添加author_avatar的引用
            authorAvatar = itemView.findViewById(R.id.author_avatar);
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

            // 增加日志记录
//            Log.d("HomeFragment", "加载用户头像，作者信息: " + (post.getAuthor() != null ? post.getAuthor().getNickname() : "null"));
//            Log.d("HomeFragment", "头像URL: " + (post.getAuthor() != null ? post.getAuthor().getAvatar() : "null"));

            // 加载用户图像
            if (post.getAuthor() != null && post.getAuthor().getAvatar() != null && !post.getAuthor().getAvatar().isEmpty()) {
                try {
//                    Log.d("HomeFragment", "使用实际用户头像，开始加载URL: " + post.getAuthor().getAvatar());
                    Context context = itemView.getContext();
                    if (context != null) {
                        Glide.with(context)
                                .load(post.getAuthor().getAvatar())
                                .placeholder(R.drawable.user_avatar)
                                .error(R.drawable.user_avatar)
                                .override(64, 64) // 强制设置尺寸
                                .circleCrop()     // 圆形裁剪
                                .into(authorAvatar);
                    }
                } catch (Exception e) {
                    Log.e("HomeFragment", "加载头像异常", e);
                    authorAvatar.setImageResource(R.drawable.user_avatar);
                }
            } else {
                // 如果没有头像，使用默认头像
                Log.d("HomeFragment", "使用默认测试头像");
                authorAvatar.setImageResource(R.drawable.user_avatar);
            }
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
            if (currentPost == null || getContext() == null) return;

            boolean isLiked = !currentPost.isLiked();
            currentPost.setLiked(isLiked);
            // 保存点赞状态到本地存储
            SharedPreferences prefs = getContext().getSharedPreferences(PREF_LIKED_POSTS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(currentPost.getPost_id(), isLiked);
            editor.apply();

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