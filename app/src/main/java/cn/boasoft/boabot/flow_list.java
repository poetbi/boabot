package cn.boasoft.boabot;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.yanzhenjie.recyclerview.OnItemClickListener;
import com.yanzhenjie.recyclerview.OnItemMenuClickListener;
import com.yanzhenjie.recyclerview.SwipeMenu;
import com.yanzhenjie.recyclerview.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;
import com.yanzhenjie.recyclerview.touch.OnItemMoveListener;

import java.util.Collections;
import java.util.List;

import cn.boasoft.boabot.adapter.flow;
import cn.boasoft.boabot.library.sqlite;

public class flow_list extends base {
    private sqlite db;
    private List<Bundle> dataset;
    private Bundle task;
    private flow adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_list);
        db = new sqlite(flow_list.this);
        initData();
        showList();
    }

    @Override
    protected void onDestroy(){
		if(db != null) db.close();
        super.onDestroy();
    }
	
    public void doAdd(View view) {
        Intent intent = new Intent(flow_list.this, flow_do.class);
        Bundle data = new Bundle();
        data.putLong("tid", task.getLong("id"));
        intent.putExtra("data", data);
        startActivityForResult(intent, 1);
    }

    private void initData(){
        Intent intent = getIntent();
        task = intent.getBundleExtra("data");

        TextView title = findViewById(R.id.title);
        title.setText(task.getString("name"));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setData(){
        if(dataset != null) dataset.clear();
        long tid = task.getLong("id");
        dataset = db.getFlowsByTid(tid);
        adapter.notifyDataSetChanged(dataset);
    }

    private void showList() {
        SwipeRecyclerView list = findViewById(R.id.list);
        adapter = new flow(flow_list.this);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(flow_list.this);
        list.setLayoutManager(manager);

        list.setOnItemClickListener(new OnItemClickListener(){ //列表项目点击监听
            @Override
            public void onItemClick(View view, int position) {
                doEdit(position);
            }
        });

        list.setSwipeMenuCreator(new SwipeMenuCreator() { //创建侧滑菜单
            @Override
            public void onCreateMenu(SwipeMenu leftMenu, SwipeMenu rightMenu, int position) {
                int number = (int) getResources().getDimension(R.dimen.item_flow_icon);
                SwipeMenuItem item = new SwipeMenuItem(flow_list.this)
                        .setImage(R.drawable.delete)
                        .setWidth(number)
                        .setHeight(number);
                rightMenu.addMenuItem(item);
                item = new SwipeMenuItem(flow_list.this)
                        .setImage(R.drawable.edit)
                        .setWidth(number)
                        .setHeight(number);
                rightMenu.addMenuItem(item);
            }
        });

        list.setOnItemMenuClickListener(new OnItemMenuClickListener() { //侧滑菜单点击监听
            @Override
            public void onItemClick(SwipeMenuBridge menuBridge, int position) {
                menuBridge.closeMenu(); //任何操作必须先关闭菜单
                int direction = menuBridge.getDirection(); //左侧还是右侧菜单
                int menuPosition = menuBridge.getPosition(); //菜单在Item中的Position
                if(direction == SwipeRecyclerView.RIGHT_DIRECTION){
                    switch(menuPosition){
                        case 0:
                            doDelete(position);
                            break;
                        case 1:
                            doEdit(position);
                            break;
                    }
                }
            }
        });

        list.setLongPressDragEnabled(true); //开启拖长按拽排序
        list.setOnItemMoveListener(new OnItemMoveListener() { //监听拖拽，更新UI
            @Override
            public boolean onItemMove(ViewHolder source, ViewHolder target) {
				if (source.getItemViewType() != target.getItemViewType()) return false;
                int fromPosition = source.getAdapterPosition() - list.getHeaderCount();
                int toPosition = target.getAdapterPosition() - list.getHeaderCount();
                Collections.swap(dataset, fromPosition, toPosition);
                adapter.notifyItemMoved(fromPosition, toPosition);
				
				int number = dataset.size();
                long[] ids = new long[number];
				for(int i = 0; i < number; i++){
					Bundle rs = dataset.get(i);
					ids[i] = rs.getLong("id");
				}
                db.editFlowSort(ids);
                return true;
            }

            @Override
            public void onItemDismiss(ViewHolder source) {
                
            }
        });

        list.setAdapter(adapter);
        setData();
    }

    private void doDelete(int position){
        new AlertDialog.Builder(flow_list.this)
                .setTitle(getString(R.string.dialog_tit_del))
                .setMessage(getString(R.string.dialog_msg_del))
                .setPositiveButton(getString(R.string.dialog_btn_y), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle v = dataset.get(position);
                        db.delFlow(v.getLong("id"));
                        setData();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_btn_n), null)
                .show();
    }

    private void doEdit(int position) {
        Intent intent = new Intent(flow_list.this, flow_do.class);
        Bundle data = dataset.get(position);
        intent.putExtra("data", data);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                setData();
            }
        }
    }
}