package cn.boasoft.boabot;

import static cn.boasoft.boabot.library.tool._socket;
import static cn.boasoft.boabot.library.tool.time2str;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import java.net.URI;
import java.net.URISyntaxException;

import cn.boasoft.boabot.library.client;
import cn.boasoft.boabot.library.notice;

public class worker extends Service {
    private ServiceHandler serviceHandler;
    private client ws;

    private class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if(ws == null) {
                URI uri = null;
                try {
                    uri = new URI("ws://bot.boasoft.cn:8080");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                ws = new client(worker.this, uri);
                ws.connect();
            }else{
                ws.ping();
            }
            super.handleMessage(msg);
        }
    }

    public worker() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("socket", Process.THREAD_PRIORITY_FOREGROUND);
        thread.start();

        Looper serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = serviceHandler.obtainMessage();
        msg.what = 1;
        serviceHandler.sendMessage(msg);

        if(ws == null) {
            notice nc = new notice(worker.this);
            String now = time2str("yyyy-MM-dd HH:mm:ss", 0);
            startForeground(1, nc.build(now + " boa运行中["+ _socket(0) +"]", null));
        }

        //super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }
}