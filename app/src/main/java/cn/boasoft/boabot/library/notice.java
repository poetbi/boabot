package cn.boasoft.boabot.library;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import cn.boasoft.boabot.R;
import cn.boasoft.boabot.start;

public class notice {
    private final String channelId = "cn.boasoft";
    private Context context;
    private NotificationManager manager;
    private Notification notify;

    public notice(Context context){
        this.context = context;

        if(!isEnabled()){
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());
            context.startActivity(intent);
            Toast.makeText(context, "请开启通知权限", Toast.LENGTH_SHORT).show();
        }
		
		file file = new file(context);
		int notice = file.getIntKV("notice");

        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "boabot";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
			if(notice != 1){
				channel.shouldVibrate();
				channel.enableVibration(true);
				channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
			}
			channel.enableLights(true);
			channel.setLightColor(Color.RED);
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);
        }
    }

    public Notification build(String content, Bundle value){
        Intent intent = new Intent(context, start.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder builder = new Notification.Builder(context, channelId);
            builder.setContentIntent(pi) //设置PendingIntent
                    .setLargeIcon(icon) //设置下拉列表中的图标(大图标)
                    .setSmallIcon(R.mipmap.icon) //设置状态栏内的小图标
                    .setContentTitle(context.getString(R.string.app_name)) //设置下拉列表里的标题
                    .setContentText(content) //设置上下文内容
                    .setExtras(value)
                    .setWhen(System.currentTimeMillis()); //设置该通知发生的时间
            notify = builder.build();
        }else{
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
            builder.setContentIntent(pi) //设置PendingIntent
                    .setLargeIcon(icon) //设置下拉列表中的图标(大图标)
                    .setSmallIcon(R.mipmap.icon) //设置状态栏内的小图标
                    .setContentTitle(context.getString(R.string.app_name)) //设置下拉列表里的标题
                    .setContentText(content) //设置上下文内容
                    .setExtras(value)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setWhen(System.currentTimeMillis()); //设置该通知发生的时间
            notify = builder.build();
        }
        return notify;
    }

    public void close(int id){
        manager.cancel(id);
    }

    /* socket状态id=1 其他id都是2开始 */
    public void run(int id){
        manager.notify(id, notify);
    }

    private boolean isEnabled(){
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        return manager.areNotificationsEnabled();
    }
}
