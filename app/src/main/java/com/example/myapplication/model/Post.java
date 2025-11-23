package com.example.myapplication.model;

/**
 * 作品模型类，用于表示瀑布流中的单条作品信息
 */
public class Post {
    /**
     * 作品ID
     */
    private String id;
    /**
     * 作品标题
     */
    private String title;
    /**
     * 作品内容
     */
    private String content;

    /**
     * 作品封面URL
     */
    private String coverUrl;

    /**
     * 作者ID
     */
    private String authorId;

    /**
     * 作者名称
     */
    private String authorName;

    /**
     * 作者头像URL
     */
    private String authorAvatar;

    /**
     * 点赞数量
     */
    private int likesCount;

    /**
     * 是否点赞
     */
    private boolean isLiked;

    /**
     * 封面图片宽高比
     */
    private float aspectRatio;

    public Post(String id, String title, String content, String coverUrl,
                String authorId, String authorName, String authorAvatar,
                int likesCount, boolean isLiked, float aspectRatio) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.coverUrl = coverUrl;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorAvatar = authorAvatar;
        this.likesCount = likesCount;
        this.isLiked = isLiked;
        // 确保宽高比在3:4 ~ 4:3之间
        if (aspectRatio < 0.75f) {
            this.aspectRatio = 0.75f; // 3:4
        } else if (aspectRatio > 1.33f) {
            this.aspectRatio = 1.33f; // 4:3
        } else {
            this.aspectRatio = aspectRatio;
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(float aspectRatio) {
        // 确保宽高比在3:4 ~ 4:3之间
        if (aspectRatio < 0.75f) {
            this.aspectRatio = 0.75f; // 3:4
        } else if (aspectRatio > 1.33f) {
            this.aspectRatio = 1.33f; // 4:3
        } else {
            this.aspectRatio = aspectRatio;
        }
    }

    // 切换点赞状态并更新点赞数
    public void toggleLike() {
        isLiked = !isLiked;
        likesCount += isLiked ? 1 : -1;
    }
}
