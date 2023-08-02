package cn.boasoft.boabot.library;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class file {
    private final String charset = "UTF-8";
    private final Context context;
    private final String name = "BOA";

    public file(Context context){
        this.context = context;
    }

    public boolean delete(String path){
        File file = path(path, false);
        if(file.exists()) {
            return file.delete();
        }
        return false;
    }

    public boolean touch(String path){
        File file = path(path, false);
        long time = System.currentTimeMillis();
        return file.setLastModified(time);
    }

    public void saveImg(String path, Bitmap data) {
        File file = path(path, true);
        String ext = path.substring(path.length() - 3).toLowerCase();
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            if(ext.equals("jpg")) {
                data.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            }else{
                data.compress(Bitmap.CompressFormat.PNG, 80, bos);
            }
            bos.close();
            data.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String path, String data) {
        File file = path(path, true);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter os = new OutputStreamWriter(fos, charset);
            os.append(data);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String read(String path) {
        String content = null;
        File file = path(path, false);
        if(!file.exists()) return "";

        try {
            FileInputStream fis = new FileInputStream(file);
            StringBuilder sb = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(fis, charset);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            content = sb.toString();
            reader.close();
            br.close();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(content == null) content = "";
        }
        return content;
    }

    public void setKV(String k, String v){
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(k, v);
        editor.apply();
    }

    public void setIntKV(String k, int v){
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(k, v);
        editor.apply();
    }

    public void setLongKV(String k, long v){
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(k, v);
        editor.apply();
    }

    public String getKV(String k, String def){
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return sp.getString(k, def);
    }

    public int getIntKV(String k){
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return sp.getInt(k, 0);
    }

    public long getLongKV(String k){
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return sp.getLong(k, 0);
    }

    public File path(String path, boolean make){
        File file;
        File root = null;
        if(path.charAt(0) == '@'){
            file = new File(path.substring(1));
        } else {
            root = context.getFilesDir();
            file = new File(root, path);
        }

        if(make && !file.exists()){
            int pos = path.lastIndexOf('/');
            File dir = new File(root, path.substring(0, pos + 1));
            if(!dir.exists()){
                dir.mkdirs();
            }
        }
        return file;
    }

    public Drawable path2draw(String path) {
        Drawable drawable = null;
        File file = path(path, false);
        if(!file.exists()) return null;

        try {
            FileInputStream fis = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            drawable = new BitmapDrawable(context.getResources(), bitmap);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return drawable;
    }
}
