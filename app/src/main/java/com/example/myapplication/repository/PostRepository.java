package com.example.myapplication.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.example.myapplication.model.Post;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PostRepository {
    private static final String TAG = "PostRepository";
    public static final String PREF_LIKED_POSTS = "like_posts";

    public static final String PREF_FOLLOWED_AUTHORS = "followed_authors";
    public static final String API_URL = "https://college-training-camp.bytedance.com/feed/?count=10&accept_video_clip=false";
    private SharedPreferences prefs;
    private Context context;

    // 添加缓存相关变量
    public static final String CACHE_FILE_NAME = "posts_cache.json";
    public static final long CACHE_EXPIRY_TIME = 30 * 60 * 1000; // 30分钟

    public PostRepository(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_LIKED_POSTS, Context.MODE_PRIVATE);
    }

    public interface PostCallback {
        void onSuccess(List<Post> posts);

        void onFailure(String error);
    }

    // 获取网络数据
    public void fetchPosts(boolean isRefresh, PostCallback callback) {
        new FetchPostTask(callback, isRefresh).execute();
    }

    // 加载本地点赞状态
    public void applyLocalLikeStatus(List<Post> posts) {
        if (context == null || posts.isEmpty()) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_LIKED_POSTS, Context.MODE_PRIVATE);
        for (Post post : posts) {
            boolean isLikedLocally = prefs.getBoolean(post.getPost_id(), post.isLiked());
            if (isLikedLocally != post.isLiked()) {
                post.setLiked(isLikedLocally);
                post.setLikeCount(post.getLikeCount() + (isLikedLocally ? 1 : -1));
            }
        }
    }

    // 加载本地关注状态
    public void applyLocalFollowStatus(List<Post> posts) {
        if (context == null || posts.isEmpty()) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_FOLLOWED_AUTHORS, Context.MODE_PRIVATE);
        for (Post post : posts) {
            if (post.getAuthor() != null && post.getAuthor().getUser_id() != null) {
                // 检查作者是否被关注
                boolean isFollowing = prefs.getBoolean(post.getAuthor().getUser_id(), false);
                // 由于Post类中没有直接存储关注状态的字段
                // 这个方法主要用于确保关注状态的本地存储被正确访问
                // 实际的关注状态使用在PostDetailViewModel中单独管理
            }
        }
    }

    // 保存点赞状态到本地
    public void saveLikeStatus(String postId, boolean isLiked) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_LIKED_POSTS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(postId, isLiked);
        editor.apply();
    }

    // 生成测试数据
    public List<Post> generateTestPosts(int count) {
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

    private class FetchPostTask extends AsyncTask<Void, Void, List<Post>> {
        private PostCallback callback;
        private boolean isRefresh;
        private String errorMessage;

        public FetchPostTask(PostCallback callback, boolean isRefresh) {
            this.callback = callback;
            this.isRefresh = isRefresh;
        }

        @Override
        protected List<Post> doInBackground(Void... voids) {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setInstanceFollowRedirects(true);

                int responseCode = connection.getResponseCode();

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

                    return parseJsonResponse(response.toString());
                } else {
                    errorMessage = "网络请求失败，响应码: " + responseCode;
                    return null;
                }
            } catch (Exception e) {
                Log.e(TAG, "网络请求异常", e);
                errorMessage = "网络请求异常: " + e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Post> result) {
            if (callback != null) {
                if (result != null && !result.isEmpty()) {
                    callback.onSuccess(result);
                } else {
                    callback.onFailure(errorMessage);
                }
            }
        }

    }


    // 解析JSON响应
    private List<Post> parseJsonResponse(String jsonResponse) {
        List<Post> postList = new ArrayList<>();
        try {
            Gson gson = new Gson();
            JsonObject rootObject = gson.fromJson(jsonResponse, JsonObject.class);

            if (rootObject.has("status_code") && rootObject.get("status_code").getAsInt() == 0) {
                if (rootObject.has("post_list") && !rootObject.get("post_list").isJsonNull()) {
                    JsonArray postsArray = rootObject.getAsJsonArray("post_list");
                    // 打印原始JSON数组
                    Log.d(TAG, "原始JSON数组: " + postsArray.toString());

                    for (int i = 0; i < postsArray.size(); i++) {
                        JsonObject postObj = postsArray.get(i).getAsJsonObject();
                        // 先检查clips是否为空
                        if (postObj.has("clips") && postObj.get("clips").isJsonNull()) {
                            Log.w(TAG, "第" + i + "个post的clips为null，跳过该帖子");
                            continue; // 跳过当前帖子，不解析也不展示
                        }
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

                        // 解析clips
                        List<Post.Clip> clips = new ArrayList<>();
                        try {
                            if (postObj.has("clips") && !postObj.get("clips").isJsonNull()) {
                                // 检查是否为JSONArray
                                if (postObj.get("clips").isJsonArray()) {
                                    JsonArray clipsArray = postObj.getAsJsonArray("clips");
//                                    Log.d(TAG, "第" + i + "个post的clips数组大小: " + clipsArray.size());

                                    for (int j = 0; j < clipsArray.size(); j++) {
                                        try {
                                            if (!clipsArray.get(j).isJsonNull()) {
                                                JsonObject clipObj = clipsArray.get(j).getAsJsonObject();
                                                Post.Clip clip = new Post.Clip();

                                                // 必须确保type为0（图片类型）
                                                if (clipObj.has("type") && !clipObj.get("type").isJsonNull()) {
                                                    clip.setType(clipObj.get("type").getAsInt());
                                                }

                                                if (clipObj.has("width") && !clipObj.get("width").isJsonNull()) {
                                                    clip.setWidth(clipObj.get("width").getAsInt());
                                                }

                                                if (clipObj.has("height") && !clipObj.get("height").isJsonNull()) {
                                                    clip.setHeight(clipObj.get("height").getAsInt());
                                                }

                                                // 确保url不为空且有效
                                                if (clipObj.has("url") && !clipObj.get("url").isJsonNull()) {
                                                    String url = clipObj.get("url").getAsString();
                                                    if (url != null && !url.isEmpty()) {
                                                        clip.setUrl(url);
                                                        clips.add(clip);
//                                                        Log.d(TAG, "成功解析clip[" + j + "]: type=" + clip.getType() + ", url=" + url);
                                                    } else {
                                                        Log.w(TAG, "clip[" + j + "]的URL为空");
                                                    }
                                                } else {
                                                    Log.w(TAG, "clip[" + j + "]缺少URL字段");
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "解析单个clip时出错: " + e.getMessage());
                                        }
                                    }
                                } else {
                                    Log.w(TAG, "第" + i + "个post的clips不是JSONArray类型");
                                }
                            } else {
                                Log.w(TAG, "第" + i + "个post没有clips字段或clips为null");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "解析clips数组时出错: " + e.getMessage());
                        }
                        // 如果没有解析到图片，尝试从其他可能的字段获取
                        if (clips.isEmpty()) {
                            Log.d(TAG, "尝试从其他字段查找图片...");
                            // 这里可以添加逻辑，从其他可能包含图片URL的字段获取
                            // 例如：postObj.has("image_url") 或其他自定义字段
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

                        // 解析hashtag
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

                        // 创建Post对象
                        String postId = postObj.has("post_id") && !postObj.get("post_id").isJsonNull() ? postObj.get("post_id").getAsString() : "";
                        String title = postObj.has("title") && !postObj.get("title").isJsonNull() ? postObj.get("title").getAsString() : "";
                        String content = postObj.has("content") && !postObj.get("content").isJsonNull() ? postObj.get("content").getAsString() : "";
                        long createTime = postObj.has("create_time") && !postObj.get("create_time").isJsonNull() ? postObj.get("create_time").getAsLong() : 0;

                        Post post = new Post(postId, title, content, hashtags, createTime, author, clips, music);

                        // 解析点赞相关字段
                        if (postObj.has("like_count") && !postObj.get("like_count").isJsonNull()) {
                            post.setLikeCount(postObj.get("like_count").getAsInt());
                        }
                        if (postObj.has("is_liked") && !postObj.get("is_liked").isJsonNull()) {
                            post.setLiked(postObj.get("is_liked").getAsBoolean());
                        }
                        // 解析
                        if (postObj.has("comment_count") && !postObj.get("comment_count").isJsonNull()) {
                            post.setCommentCount(postObj.get("comment_count").getAsInt());
                        }
                        if (postObj.has("collect_count") && !postObj.get("collect_count").isJsonNull()) {
                            post.setCollectCount(postObj.get("collect_count").getAsInt());
                        }
                        if (postObj.has("share_count") && !postObj.get("share_count").isJsonNull()) {
                            post.setShareCount(postObj.get("share_count").getAsInt());
                        }


                        postList.add(post);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "JSON解析异常", e);
        }
        return postList;
    }

    // 保存数据到本地缓存
    public void savePostsToCache(List<Post> posts) {
        try {
            File cacheFile = new File(context.getCacheDir(), CACHE_FILE_NAME);
            FileWriter writer = new FileWriter(cacheFile);
            Gson gson = new Gson();
            JsonObject cacheObject = new JsonObject();
            cacheObject.addProperty("timestamp", System.currentTimeMillis());
            cacheObject.add("posts", gson.toJsonTree(posts));
            writer.write(cacheObject.toString());
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "保存缓存失败", e);
        }
    }

    // 从本地缓存读取数据
    public List<Post> readPostsFromCache() {
        try {
            File cacheFile = new File(context.getCacheDir(), CACHE_FILE_NAME);
            if (!cacheFile.exists()) return null;

            FileReader reader = new FileReader(cacheFile);
            Gson gson = new Gson();
            JsonObject cacheObject = gson.fromJson(reader, JsonObject.class);
            reader.close();

            // 检查缓存是否过期
            long timestamp = cacheObject.get("timestamp").getAsLong();
            if (System.currentTimeMillis() - timestamp > CACHE_EXPIRY_TIME) {
                return null;
            }

            JsonArray postsArray = cacheObject.getAsJsonArray("posts");
            // 转换为List<Post>
            List<Post> cachedPosts = gson.fromJson(postsArray, new TypeToken<List<Post>>() {}.getType());

            return cachedPosts;
        } catch (Exception e) {
            Log.e(TAG, "读取缓存失败", e);
            return null;
        }
    }
}
