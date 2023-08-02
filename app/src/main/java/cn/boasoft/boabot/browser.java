package cn.boasoft.boabot;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import java.util.ArrayList;

import cn.boasoft.boabot.library.sqlite;
import cn.boasoft.boabot.library.webview;

public class browser extends base {
    private WebView wv;
    private sqlite db;
    private webview browser;
    private ArrayList<Bundle> flows = new ArrayList<>(); //工作流内容

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        db = new sqlite(browser.this);

        Intent intent = getIntent();
        long tid = intent.getLongExtra("data", 0);
        flows = db.getFlowsByTid(tid);
        if(flows != null && flows.size() > 0) {
            wv = findViewById(R.id.webview);
            browser = new webview(browser.this, wv, "execute.js");
            execute();
        }
    }

    private void execute(){
        for(Bundle flow : flows){
            long fid = flow.getLong("id");
            String cmd = flow.getString("cmd");
            String obj = flow.getString("obj");
            String val = flow.getString("val");
            String ctl = flow.getString("ctl");
            String[] arr = ctl.split(",");
            int loop = Integer.parseInt(arr[0]);
            long wait = (long) Double.parseDouble(arr[1]) * 1000;
            int step = Integer.parseInt(arr[2]);
            //if(inTest) wait += 3000;
            boolean result = false;
            int res = -1;

            switch (cmd){
                case "start":
                    browser.open(val);
                    res = 0;
                    break;

                case "click":
                    wv.evaluateJavascript("boabot.click()", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {

                        }
                    });
                    break;

                case "input":

                    break;

                case "check":
                    break;

                case "goto":

                    res = 0;
                    break;

                case "close":

                    res = 0;
                    break;
            }
            db.setLog(fid, res, val);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && wv.canGoBack()) {
            wv.goBack();
            return true;
        }
        return false;
    }
}