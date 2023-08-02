package cn.boasoft.boabot.library;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class http {
    private final int connTimeout = 15 * 1000;

    public String get(String url) {
        if(url.length() < 1) return "";

        String content = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setReadTimeout(30 * 1000);
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setConnectTimeout(connTimeout);
            if (conn.getResponseCode() == 200) {
                InputStream in = conn.getInputStream();
                int len;
                byte[] buffer = new byte[1024];
                while((len = in.read(buffer)) != -1){
                    bos.write(buffer, 0, len);
                }
                in.close();
            }
            bos.flush();
            content = bos.toString();
            conn.disconnect();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(content == null) content = "";
        }
        return content;
    }

    public Bitmap downImg(String src) {
        Bitmap bmp = null;
        try {
            URL url = new URL(src);
            InputStream in = url.openStream();
            bmp = BitmapFactory.decodeStream(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }

    public String post(String url, String data) throws IOException {
        if(url.length() < 1) return "";

        String content;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        String charset = "UTF-8";
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setReadTimeout(60 * 1000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json;charset="+ charset); //application/x-www-form-urlencoded
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setConnectTimeout(connTimeout);
            OutputStream out = conn.getOutputStream();
            out.write(data.getBytes());
            if (conn.getResponseCode() == 200) {
                InputStream in = conn.getInputStream();
                GZIPInputStream ungzip = new GZIPInputStream(in);
                int len;
                byte[] buffer = new byte[1024];
                while((len = ungzip.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }
                in.close();
            }
            bos.flush();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            content = bos.toString(charset);
            bos.close();
        }
        return content;
    }
}
