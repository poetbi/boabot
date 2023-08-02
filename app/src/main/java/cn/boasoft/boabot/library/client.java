package cn.boasoft.boabot.library;

import static cn.boasoft.boabot.library.tool._socket;
import static cn.boasoft.boabot.library.tool.md5;
import static cn.boasoft.boabot.library.tool.time2str;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.net.URI;
import java.security.NoSuchAlgorithmException;

import cn.boasoft.boabot.R;

public class client extends WebSocketClient{
    private Context context;
    private file file;
    private String user;
    private String pass;

    public client(Context context, URI serverUri) {
        super(serverUri);
        this.context = context;
        addHeader("Origin", "http://"+ serverUri.getHost());

        file = new file(context);
        user = file.getKV("user", "");
        pass = file.getKV("pass", "");
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        notice(1);
        file.write("worker", System.currentTimeMillis() + "");

        String today = time2str("yyyyMMdd", 0);
        try {
            pass = md5(pass + today);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        pass = pass.substring(8, 8 + 16);
        send("$"+ pass + user);
    }

    @Override
    public void onMessage(String msg) {
        if(msg.length() < 2) return;

        char type = msg.charAt(0);
        String data = msg.substring(1);

        switch (type){
            case '*': //任务
                String[] arr = data.split("~", 2);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("boa://bot/?tid="+ arr[0] +"&val="+ arr[1]));
                context.startActivity(intent);
                break;

            case '#': //通知
                notice nc = new notice(context);
                nc.build(data, null);
                nc.run(2);
                break;

            case '$': //登录
                if(data.equals("SUCC")){
                    notice(2);
                }
                break;
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        notice(4);
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }

    public void ping(){
        if (isClosed()){
            reconnect();
        } else {
            send("0");
        }
        file.touch("worker");
    }

    private void notice(int code){
        notice nc = new notice(context);
        String now = time2str("yyyy-MM-dd HH:mm:ss", 0);
        nc.build(now + " boa运行中["+ _socket(code) +"]", null);
        nc.run(1);
    }
}