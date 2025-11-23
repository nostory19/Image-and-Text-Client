package com.example.myapplication.utils;

import android.content.Context;

import com.example.myapplication.model.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 数据生成器，负责生成模拟数据
 */
public class DataGenerator {

    private static final String[] TITLES = {
            "美丽的日落风景",
            "城市夜景随拍",
            "山间小溪流水",
            "海边度假胜地",
            "城市建筑之美",
            "自然风光摄影",
            "街头美食探店",
            "咖啡时光",
            "艺术展览记录",
            "生活小确幸"
    };

    private static final String[] CONTENTS = {
            "今天傍晚在海边拍到了美丽的日落，分享给大家。",
            "城市的夜晚总是那么迷人，灯光璀璨。",
            "周末去爬山，发现了这条清澈的小溪。",
            "度假胜地的海滩真的太美了，海水清澈见底。",
            "城市建筑的线条和光影构成了独特的美感。",
            "大自然的鬼斧神工总是让人惊叹不已。",
            "这家小店的美食真的很棒，推荐大家来尝试。",
            "一个悠闲的下午，一杯咖啡，一本书。",
            "参观了这个艺术展览，收获很多灵感。",
            "生活中的小确幸，值得被记录和分享。"
    };

    private static final String[] AUTHOR_NAMES = {
            "摄影爱好者",
            "旅行达人",
            "美食博主",
            "生活记录者",
            "艺术创作者"
    };

    private static final Random RANDOM = new Random();

    /**
     * 生成模拟的作品数据列表
     */
    public static List<Post> generatePosts(Context context, int count) {
        List<Post> posts = new ArrayList<>();
        LikeMapper likeMapper = LikeMapper.getInstance(context);

        for (int i = 0; i < count; i++) {
            String id = "post_" + i;
            String title = TITLES[RANDOM.nextInt(TITLES.length)];
            String content = CONTENTS[RANDOM.nextInt(CONTENTS.length)];
            // 使用随机图片作为封面
            String coverUrl = "https://picsum.photos/400/" + (400 + RANDOM.nextInt(300));
            String authorId = "author_" + RANDOM.nextInt(10);
            String authorName = AUTHOR_NAMES[RANDOM.nextInt(AUTHOR_NAMES.length)];
            String authorAvatar = "https://picsum.photos/100/100?random=" + i;
            int likesCount = RANDOM.nextInt(1000);
            boolean isLiked = likeMapper.isPostLiked(id);
            // 生成3:4到4:3之间的随机宽高比
            float aspectRatio = 0.75f + RANDOM.nextFloat() * 0.58f; // 0.75到1.33之间

            posts.add(new Post(id, title, content, coverUrl, authorId, authorName,
                    authorAvatar, likesCount, isLiked, aspectRatio));
        }

        return posts;
    }
}
