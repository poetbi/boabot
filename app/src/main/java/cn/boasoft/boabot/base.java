package cn.boasoft.boabot;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class base extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wakeup();
    }

    private void wakeup(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    public void doMenuStart(View view) {
        Intent intent = new Intent(getApplicationContext(), start.class);
        startActivity(intent);
    }

    public void doMenuTask(View view) {
        Intent intent = new Intent(getApplicationContext(), task_list.class);
        startActivity(intent);
    }

    public void doMenuConfig(View view) {
        Intent intent = new Intent(getApplicationContext(), config.class);
        startActivity(intent);
    }

    public void doMenuLog(View view) {
        Intent intent = new Intent(getApplicationContext(), log_list.class);
        startActivity(intent);
    }

    public void doMenuHelp(View view) {
        Intent intent = new Intent(getApplicationContext(), help.class);
        startActivity(intent);
    }
}