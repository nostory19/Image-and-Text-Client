package com.example.myapplication.model;

import java.io.Serializable;
import java.util.List;

/**
 * 作品详情模型类
 */
public class PostDetail implements Serializable {
    public static final long serialVersionUID = 1L;
    private String authorId;
    private String authorName;
    private String title;
    private String content;
    private long publishDate;
    private List<Integer> images;
    private List<String> topics;

    public PostDetail(String authorId, String authorName, String title, String content,
                      long publishDate, List<Integer> images, List<String> topics) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.title = title;
        this.content = content;
        this.publishDate = publishDate;
        this.images = images;
        this.topics = topics;
    }

    // Getters
    public String getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public long getPublishDate() { return publishDate; }
    public List<Integer> getImages() { return images; }
    public List<String> getTopics() { return topics; }
}
