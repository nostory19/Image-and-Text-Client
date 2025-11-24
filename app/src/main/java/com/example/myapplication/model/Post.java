package com.example.myapplication.model;

import java.io.Serializable;

/**
 * 作品模型类，用于表示瀑布流中的单条作品信息
 */
public class Post implements Serializable {
    private String title;
    private String author;
    private boolean liked;
    private int likeCount;

    public Post(String title, String author, boolean liked, int likeCount) {
        this.title = title;
        this.author = author;
        this.liked = liked;
        this.likeCount = likeCount;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isLiked() { return liked; }
    public int getLikeCount() { return likeCount; }

    public void setLiked(boolean liked) { this.liked = liked; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
}
