package com.example.myapplication.model;

import java.io.Serializable;
import java.util.List;

/**
 * 作品模型类，用于表示瀑布流中的单条作品信息
 */
public class Post implements Serializable {

    /**
     * 作品ID
     */
    private String post_id;
    /**
     * 作品标题
     */
    private String title;
    /**
     * 作品正文
     */
    private String content;

    private List<Hashtag> hashtag;
    /**
     * 作品创建时间戳
     */
    private long create_time;
    /**
     * 作品作者信息
     */
    private Author author;

    /**
     * 作品关联的图片/或视频片段数据
     */
    private List<Clip> clips;

    /**
     * 作品关联的音乐信息
     */
    private Music music;

    // 下面是本地存储的字段
    private boolean liked;
    private int likeCount;

    // 构造方法
    public Post(String post_id, String title, String content, List<Hashtag> hashtag, long create_time, Author author, List<Clip> clips, Music music) {
        this.post_id = post_id;
        this.title = title;
        this.content = content;
        this.hashtag = hashtag;
        this.create_time = create_time;
        this.author = author;
        this.clips = clips;
        this.music = music;
        this.liked = false; // 默认未点赞
        this.likeCount = 0; // 默认点赞数为0
    }

    // Getters and Setters
    public String getPost_id() { return post_id; }
    public void setPost_id(String post_id) { this.post_id = post_id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<Hashtag> getHashtag() { return hashtag; }
    public void setHashtag(List<Hashtag> hashtag) { this.hashtag = hashtag; }
    public long getCreate_time() { return create_time; }
    public void setCreate_time(long create_time) { this.create_time = create_time; }
    public Author getAuthor() { return author; }
    public void setAuthor(Author author) { this.author = author; }
    public List<Clip> getClips() { return clips; }
    public void setClips(List<Clip> clips) { this.clips = clips; }
    public Music getMusic() { return music; }
    public void setMusic(Music music) { this.music = music; }

    // 本地状态相关
    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    // 获取作者昵称（为兼容现有代码）
    public String getAuthorName() {
        return author != null ? author.getNickname() : "未知作者";
    }


    // 内部类

    /**
     * 作品中的标签实体类
     */
    public static class Hashtag implements Serializable {

        /**
         * 起始下标，闭区间
         */
        private int start;
        /**
         * 终止下标，开区间
         */
        private int end;

        public int getStart() { return start; }
        public void setStart(int start) { this.start = start; }
        public int getEnd() { return end; }
        public void setEnd(int end) { this.end = end; }
    }


    /**
     * 作品作者实体类
     */
    public static class Author implements Serializable {

        /**
         * 用户ID
         */
        private String user_id;

        /**
         * 用户昵称
         */
        private String nickname;

        /**
         * 用户头像URL
         */
        private String avatar;

        public String getUser_id() { return user_id; }
        public void setUser_id(String user_id) { this.user_id = user_id; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }


    /**
     * 作品关联的图片/或视频片段实体类
     */
    public static class Clip implements Serializable {
        /**
         * 片段类型：0-图片， 1-视频
         */
        private int type;

        /**
         * 宽度
         */
        private int width;
        /**
         * 高度
         */
        private int height;
        private String url;

        public int getType() { return type; }
        public void setType(int type) { this.type = type; }
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
    /**
     * 作品关联的音乐信息实体类
     */
    public static class Music implements Serializable {
        /**
         * 音量
         */
        private int volume;
        /**
         * 播放时间点
         */
        private int seek_time;
        /**
         * 音乐URL
         */
        private String url;

        public int getVolume() { return volume; }
        public void setVolume(int volume) { this.volume = volume; }
        public int getSeek_time() { return seek_time; }
        public void setSeek_time(int seek_time) { this.seek_time = seek_time; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }


}
