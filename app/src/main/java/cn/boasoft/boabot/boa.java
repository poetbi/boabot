package cn.boasoft.boabot;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import cn.boasoft.boabot.adapter.event;
import cn.boasoft.boabot.library.app;
import cn.boasoft.boabot.library.file;

public class boa extends Activity {
    private file file;
    private String link;
    private boolean inAds = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wakeup();
        setContentView(R.layout.activity_boa);
        file = new file(boa.this);

        loadAds();
        parseUrl();
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

    private void loadAds(){
        try {
            String str = file.read("full.json");
            if (str != null && !str.isEmpty()) {
                JSONObject v = new JSONObject(str);
                String img = v.getString("img");

                File full = file.path("full.json", false);
                boolean isShow = System.currentTimeMillis() >= (full.lastModified() + 6 * 3600 * 1000);

                if(isShow && !img.isEmpty()){
                    findViewById(R.id.wellcom).setVisibility(View.GONE);
                    FrameLayout ads = findViewById(R.id.fullads);
                    ads.setVisibility(View.VISIBLE);
                    ads.setBackground(file.path2draw(img));
                    link = v.getString("link");
                    file.touch("full.json");

                    inAds = true;
                    CountDownTimer cdt = new CountDownTimer(5000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            TextView count_view = findViewById(R.id.counter);
                            count_view.setText(String.valueOf(millisUntilFinished / 1000));
                        }

                        @Override
                        public void onFinish() {
                            goStart();
                        }
                    };
                    cdt.start();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseUrl() {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            String tid = uri.getQueryParameter("tid");
            String val = uri.getQueryParameter("val");

            TextView tip = findViewById(R.id.wellcom);
            tip.setText(getString(R.string.task_do_exec));

            if(tid != null && tid.length() > 0){
                tip.setVisibility(View.VISIBLE);
                EventBus.getDefault().post(new event("execute", tid, val));
            }else{
                if(!inAds) goStart();
            }
        } else {
            if(!inAds) goStart();
        }
    }

    public void doLink(View view) {
        app app = new app(boa.this);
        app.run("web", link);
    }

    public void doNext(View view) {
        goStart();
    }

    public void goStart() {
        inAds = false;
        Intent intent = new Intent(boa.this, start.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }
}