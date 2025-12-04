package com.example.myapplication.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

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

public class PostRepository {
    private static final String TAG = "PostRepository";
    public static final String PREF_LIKED_POSTS = "like_posts";
    public static final String API_URL = "https://college-training-camp.bytedance.com/feed/?count=10&accept_video_clip=false";
    private SharedPreferences prefs;
    private Context context;

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

                        // 解析clips
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

                        postList.add(post);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "JSON解析异常", e);
        }
        return postList;
    }
}
