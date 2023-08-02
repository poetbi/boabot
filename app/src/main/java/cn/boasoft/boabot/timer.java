package cn.boasoft.boabot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import cn.boasoft.boabot.library.job;

public class timer extends Service {
    public timer() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> new job(timer.this).run()).start();

        long time = System.currentTimeMillis() + 60000;
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(timer.this, timer.class);
        PendingIntent pi = PendingIntent.getService(timer.this, 0, i, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi);
        } else {
            manager.setExact(AlarmManager.RTC_WAKEUP, time, pi);
        }

        return super.onStartCommand(intent, flags, startId);
    }
}