package com.example.myapplication;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        // 初始化RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.waterfall_recycler);

        // 设置瀑布流布局管理器，2列
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        // 创建并设置适配器
        recyclerView.setAdapter(new PostAdapter());

        return view;
    }

    // 简单的适配器类，用于测试显示
    private class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {

        @Override
        public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_card, parent, false);
            return new PostViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(PostViewHolder holder, int position) {
            // 这里可以设置数据，但由于是测试，可以暂时使用布局中的默认值
        }

        @Override
        public int getItemCount() {
            // 返回一些测试数据项，比如10个
            return 10;
        }
    }

    private class PostViewHolder extends RecyclerView.ViewHolder {

        public PostViewHolder(View itemView) {
            super(itemView);
        }
    }
}