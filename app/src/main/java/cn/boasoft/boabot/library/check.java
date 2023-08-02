package cn.boasoft.boabot.library;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class check {
    private final String api = "http://bot.boasoft.cn/?m=api&c=check";
    private final Context context;
    private final http http;
    private final file file;
    private final long now;

    public check(Context context){
        this.context = context;
        http = new http();
        file = new file(context);
        now = System.currentTimeMillis() / 1000;

        new Thread(
                () -> {
                    ads();
                    app();
                }
        ).start();
    }

    private void ads(){
        long ads = file.getLongKV("ads");
        if(now - ads > 86400) {
            try {
                String str;
                String url;
                Bitmap img;

                str = http.get(api +"&a=ads&time="+ ads);
                if (str.length() > 0) {
                    JSONArray arr = new JSONArray(str);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject v = arr.getJSONObject(i);
                        url = v.getString("url");
                        img = http.downImg(url);
                        file.saveImg(v.getString("img"), img);
                    }
                    file.write("ads.json", str);
                }

                str = http.get(api +"&a=full&time="+ ads);
                if (str.length() > 0) {
                    JSONObject v = new JSONObject(str);
                    url = v.getString("url");
                    if(url.length() > 0) {
                        img = http.downImg(url);
                        file.saveImg(v.getString("img"), img);
                    }
                    file.write("full.json", str);
                }

                file.setLongKV("ads", now);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void app(){
        if(file.path("update.txt", false).exists()) return;

        long app = file.getLongKV("app");
        if(now - app > 86400 * 7) {
            int ver = 0;
            try {
                ver = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            String url = http.get(api +"&a=app&ver="+ ver).trim();
            if (!url.isEmpty()) {
                file.write("update.txt", url);
            }
            file.setLongKV("app", now);
        }
    }
}
