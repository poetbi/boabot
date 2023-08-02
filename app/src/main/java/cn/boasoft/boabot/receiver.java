package cn.boasoft.boabot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import cn.boasoft.boabot.adapter.event;

public class receiver extends BroadcastReceiver {
    public receiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String act = intent.getAction();
        if (act.equals("android.intent.action.BOOT_COMPLETED")) {
            Intent i = new Intent(context, timer.class);
            context.startService(i);
        }else if(act.equals("android.intent.action.PHONE_STATE")){
            EventBus.getDefault().post(new event("phone", null, null));
        }
    }
}