package cn.boasoft.boabot.library;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;

public class perm {
    private final Context context;
    private final ArrayList<String> perms = new ArrayList<String>(){{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            add(Manifest.permission.ANSWER_PHONE_CALLS);   //应答/挂断电话
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.SCHEDULE_EXACT_ALARM); //精确闹钟
        }
        //add(Manifest.permission.CALL_PHONE); //自动拨打电话
    }};

    /*查看权限 Protection level: dangerous 需要申请权限
    https://developer.android.google.cn/reference/android/Manifest.permission#FORCE_STOP_PACKAGES
    */

    public perm(Context context){
        this.context = context;
    }

    public String[] get(){
        int len = perms.size();
        String[] arr = new String[len];
        for(int i = 0; i < len; i++){
            arr[i] = perms.get(i);
        }
        return arr;
    }

    public boolean checkRes(int[] result){
        if (result.length > 0) {
            for (int res : result){
                if(res != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }else{
            return false;
        }
        return true;
    }

    public boolean checkAll(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for(String perm : perms) {
                if(context.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }

    public boolean check(String perm){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(perms.contains(perm)){
                return context.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
            }
        }
        return true;
    }
}