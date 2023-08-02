package cn.boasoft.boabot;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cn.boasoft.boabot.library.app;

public class help extends base {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
    }

    public void doLinkBH(View view) {
        String text = ((TextView) view).getText().toString().trim();
        text = android.os.Build.MODEL +" "+ text;
        app app = new app(help.this);
        app.run("web", "https://www.baidu.com/s?wd="+ text);
    }

    public void doLinkQD(View view) {
        String text = "允许被第三方应用启动";
        text = android.os.Build.MODEL +" "+ text;
        app app = new app(help.this);
        app.run("web", "https://www.baidu.com/s?wd="+ text);
    }

    public void doLinkWZA(View view) {
        String text = "开启无障碍服务";
        text = android.os.Build.MODEL +" "+ text;
        app app = new app(help.this);
        app.run("web", "https://www.baidu.com/s?wd="+ text);
    }
}