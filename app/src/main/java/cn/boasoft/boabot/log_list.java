package cn.boasoft.boabot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.yanzhenjie.recyclerview.OnItemClickListener;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import java.util.List;

import cn.boasoft.boabot.adapter.log;
import cn.boasoft.boabot.library.sqlite;

public class log_list extends base {
    private SwipeRefreshLayout refresh;
    private SwipeRecyclerView list;
    private sqlite db;
    private List<Bundle> dataset;
    private log adapter;
    private final int pageSzie = 20;
    private long pageNum = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_list);
        db = new sqlite(log_list.this);
        showList();
    }

    @Override
    protected void onDestroy(){
        if(db != null) db.close();
        super.onDestroy();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setData(){
        pageNum = 1;
        if(dataset != null) dataset.clear();
        dataset = db.getLogs(1, pageSzie);
        adapter.notifyDataSetChanged(dataset);

        refresh.setRefreshing(false); //首次必须，否则不会触发加载更多
        boolean isEmpty = dataset.size() <= 0;
        boolean hasMore = dataset.size() >= pageSzie;
        list.loadMoreFinish(isEmpty, hasMore);
    }

    private void showList() {
        refresh = findViewById(R.id.refresh);
        refresh.setOnRefreshListener(refreshListener); //刷新监听

        list = findViewById(R.id.list);
        adapter = new log(log_list.this);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(log_list.this);
        list.setLayoutManager(manager);

        int color = getResources().getColor(R.color.border);
        DefaultItemDecoration decoration = new DefaultItemDecoration(color, 1, 1);
        list.addItemDecoration(decoration);

        list.setOnItemClickListener(itemClickListener);
        list.useDefaultLoadMore(); //加载更多
        list.setLoadMoreListener(loadMoreListener); //加载更多监听

        list.setAdapter(adapter);
        setData();
    }

    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            setData();
        }
    };

    private OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(View itemView, int position) {
            doLogMore(position);
        }
    };

    private SwipeRecyclerView.LoadMoreListener loadMoreListener = new SwipeRecyclerView.LoadMoreListener() {
        @Override
        public void onLoadMore() {
            pageNum++;
            List<Bundle> arr = db.getLogs(pageNum, pageSzie);
            dataset.addAll(arr);
            int num = arr.size();
            adapter.notifyDataSetChanged();

            boolean isEmpty = num <= 0;
            boolean hasMore = num >= pageSzie;
            list.loadMoreFinish(isEmpty, hasMore);
            //如果加载失败调用下面的方法，传入errorCode和errorMessage。
            //mRecyclerView.loadMoreError(0, "请求网络失败");
        }
    };

    private void doLogMore(int position) {
        Intent intent = new Intent(log_list.this, log_do.class);
        intent.putExtra("data", dataset.get(position));
        startActivity(intent);
    }
}