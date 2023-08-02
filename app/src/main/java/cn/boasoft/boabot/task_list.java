package cn.boasoft.boabot;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yanzhenjie.recyclerview.OnItemClickListener;
import com.yanzhenjie.recyclerview.OnItemMenuClickListener;
import com.yanzhenjie.recyclerview.SwipeMenu;
import com.yanzhenjie.recyclerview.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import cn.boasoft.boabot.adapter.event;
import cn.boasoft.boabot.adapter.task;
import cn.boasoft.boabot.library.sqlite;

public class task_list extends base {
    private sqlite db;
    private List<Bundle> dataset;
    private task adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        db = new sqlite(task_list.this);
        showList();
    }
	
	@Override
    protected void onDestroy(){
		if(db != null) db.close();
        super.onDestroy();
    }

    public void doAdd(View view) {
        Intent intent = new Intent(task_list.this, task_do.class);
        startActivityForResult(intent, 1);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setData(){
        if(dataset != null) dataset.clear();
        dataset = db.getTasks();
        adapter.notifyDataSetChanged(dataset);
    }

    @SuppressLint("ResourceAsColor")
    private void showList() {
        SwipeRecyclerView list = findViewById(R.id.list);
        adapter = new task(task_list.this);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(task_list.this);
        list.setLayoutManager(manager);

        list.setOnItemClickListener(new OnItemClickListener(){ //列表项目点击监听
            @Override
            public void onItemClick(View view, int position) {
                doFlow(position);
            }
        });

        list.setSwipeMenuCreator(new SwipeMenuCreator() { //创建侧滑菜单
            @Override
            public void onCreateMenu(SwipeMenu leftMenu, SwipeMenu rightMenu, int position) {
                int number = (int) getResources().getDimension(R.dimen.item_task_icon);
                SwipeMenuItem item;
                item = new SwipeMenuItem(task_list.this)
                        .setImage(R.drawable.delete)
                        .setWidth(number)
                        .setHeight(number);
                rightMenu.addMenuItem(item);
                item = new SwipeMenuItem(task_list.this)
                        .setImage(R.drawable.edit)
                        .setWidth(number)
                        .setHeight(number);
                rightMenu.addMenuItem(item);
                item = new SwipeMenuItem(task_list.this)
                        .setImage(R.drawable.copy)
                        .setWidth(number)
                        .setHeight(number);
                rightMenu.addMenuItem(item);
                item = new SwipeMenuItem(task_list.this)
                        .setImage(R.drawable.exec)
                        .setWidth(number)
                        .setHeight(number);
                rightMenu.addMenuItem(item);
                item = new SwipeMenuItem(task_list.this)
                        .setImage(R.drawable.flow)
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
                        case 2:
                            doCopy(position);
                            break;
                        case 3:
                            doExec(position);
                            break;
                        case 4:
                            doFlow(position);
                            break;
                    }
                }
            }
        });

        list.setAdapter(adapter);
        setData();
    }

    private void doDelete(int position){
        new AlertDialog.Builder(task_list.this)
            .setTitle(getString(R.string.dialog_tit_del))
            .setMessage(getString(R.string.dialog_msg_del))
            .setPositiveButton(getString(R.string.dialog_btn_y), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Bundle v = dataset.get(position);
                    db.delTask(v.getLong("id"));
                    setData();
                }
            })
            .setNegativeButton(getString(R.string.dialog_btn_n), null)
            .show();
    }

    private void doEdit(int position) {
        Intent intent = new Intent(task_list.this, task_do.class);
        intent.putExtra("data", dataset.get(position));
        startActivityForResult(intent, 1);
    }

    private void doCopy(int position) {
        Bundle task = dataset.get(position);
        long id = task.getLong("id");

        long tid = db.addTask(task);
        ArrayList<Bundle> list = db.getFlowsByTid(id);
        for(Bundle data : list) {
            data.putLong("tid", tid);
            db.addFlow(data);
        }
        setData();

        Toast.makeText(task_list.this, R.string.task_do_copy, Toast.LENGTH_SHORT).show();
    }

    private void doExec(int position) {
        Bundle task = dataset.get(position);
        long tid = task.getLong("id");
        EventBus.getDefault().post(new event("execute", String.valueOf(tid), null));
        Toast.makeText(task_list.this, R.string.task_do_exec, Toast.LENGTH_SHORT).show();
    }

    private void doFlow(int position) {
        Intent intent = new Intent(task_list.this, flow_list.class);
        intent.putExtra("data", dataset.get(position));
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) setData();
        }
    }
}