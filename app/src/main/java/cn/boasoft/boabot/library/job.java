package cn.boasoft.boabot.library;

import static cn.boasoft.boabot.library.tool.str2time;
import static cn.boasoft.boabot.library.tool.time2str;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import cn.boasoft.boabot.adapter.event;
import cn.boasoft.boabot.worker;

public class job {
    private final Context context;
    private sqlite db;
    private file file;

    public job(Context context){
        this.context = context;
        file = new file(context);
    }

    public void run(){
        db = new sqlite(context);
        File timer = file.path("timer", false);
        if(timer.exists()){
            file.touch("timer");
        }else{
            file.write("timer", System.currentTimeMillis() + "");
        }

        if(file.getIntKV("remote") == 1){
            worker();
        }

        long last = Long.parseLong(file.read("timer").trim());
        String date = time2str("yyyyMMdd 0:0:0", 0);
        long now = str2time(date, "yyyyMMdd H:m:s");
        if(last < now){
            //db.setLog(0, 0, "计划次日任务");
            calculateTasks();
            file.write("timer", System.currentTimeMillis() + "");
        }

        execute();
        db.close();
    }

    private void execute(){
        ArrayList<Bundle> tasks = db.getTimersCurrent();
        for(Bundle task : tasks){
            long id = task.getLong("id");
            long tid = task.getLong("tid");

            if (tid > 0) {
                EventBus.getDefault().post(new event("execute", String.valueOf(tid), null));
            }

            db.delTimer(tid, id);
        }
    }

    private void worker(){
        Intent intent = new Intent(context, worker.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        }else{
            context.startService(intent);
        }
    }

    @SuppressLint("DefaultLocale")
    private void calculateTasks(){
        ArrayList<Bundle> tasks = db.getTasksOn();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        long nextDay = System.currentTimeMillis() + 86400 * 1000;
        long time;
        Bundle data = new Bundle();

        for(Bundle task : tasks){
            if(task.getInt("type") != 1) continue;
            data.putLong("tid", task.getLong("id"));

            String[] arr = task.getString("exec").split(",");
            int month = Integer.parseInt(arr[4]);
            int week = Integer.parseInt(arr[3]);
            int day = Integer.parseInt(arr[2]);
            int hour = Integer.parseInt(arr[1]);
            int minute = Integer.parseInt(arr[0]);

            String date = time2str("yyyy", nextDay);
            String strMonth = time2str("MM", nextDay);
            int intMonth = Integer.parseInt(strMonth);
            if (month < 0 || (month + 1) == intMonth){
                date += strMonth;
            }else{
                continue;
            }

            String strDay = time2str("dd", nextDay);
            int intDay = Integer.parseInt(strDay);
            if (week < 0 && day < 0){
                date += strDay;
            }else{
                int week_now = calendar.get(Calendar.DAY_OF_WEEK) % 7;
                int week_dis = week - week_now;
                if(
                    (week >= 0 && week_dis == 0) ||
                            (day >= 0 && (day + 1) == intDay)
                ){
                    date += strDay;
                }else {
                    continue;
                }
            }

            if(hour < 0){
                for(int i = 0; i < 24; i++){
                    time = str2time(date + String.format(" %d:%d:0", i, minute), null);
                    data.putLong("time", time);
                    db.addTimer(data);
                }
            }else{
                time = str2time(date + String.format(" %d:%d:0", hour, minute), null);
                data.putLong("time", time);
                db.addTimer(data);
            }
        }
    }
}
