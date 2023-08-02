package cn.boasoft.boabot.library;

import static cn.boasoft.boabot.library.tool._error;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.List;

public class app {
    private Context context;

    public app(Context context) {
        this.context = context;
    }

    public int run(String obj, String val) {
        Intent intent = null;
        Uri uri;
        String[] arr;

        switch(obj){
            case "app": //启动或激活应用
                if(!isInstalled(val)){
                    Toast.makeText(context, _error(1), Toast.LENGTH_SHORT).show();
                    return 1;
                }
                intent = getIntent(val);
                break;

            case "tel": //自动拨打电话，需要CALL_PHONE权限
                uri = Uri.parse("tel:"+ val);
                intent = new Intent(Intent.ACTION_CALL, uri);
                break;

            case "sms": //自动发送短信，需要SEND_SMS、READ_SMS、WRITE_SMS权限
                arr = val.split("\\|", 2);
                ContentValues values = new ContentValues(); //可以看到已发的信息
                values.put("address", arr[0]);
                values.put("body", arr[1]);
                ContentResolver contentResolver = context.getContentResolver();
                contentResolver.insert(Uri.parse("content://sms/sent"), values);
                //contentResolver.insert(Uri.parse("content://sms/inbox"), values);
                break;

            case "mail": //发送邮件
                arr = val.split("\\|", 3);
                uri = Uri.parse("mailto:"+ arr[0]);
                intent = new Intent(Intent.ACTION_SENDTO, uri);
                intent.putExtra(Intent.EXTRA_SUBJECT, arr[1]);
                intent.putExtra(Intent.EXTRA_TEXT, arr[2]);
                break;

            case "web": //访问网页
                uri = Uri.parse(val);
                intent = new Intent(Intent.ACTION_VIEW, uri);
                break;
        }

        try {
            if (intent != null) context.startActivity(intent);
        }catch (ActivityNotFoundException e){
            return 2;
        }

        return  0;
    }

    private boolean isInstalled(String packageName){
        PackageInfo pi;
        try {
            pi = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return pi != null;
    }

    @SuppressLint({"WrongConstant", "QueryPermissionsNeeded"})
    private Intent getIntent(String packageName) {
        String mainAct = null;
        PackageManager pkgMag = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        List<ResolveInfo> list = pkgMag.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo.packageName.equals(packageName)) {
                mainAct = info.activityInfo.name;
                break;
            }
        }

        if (TextUtils.isEmpty(mainAct)) {
            return null;
        }

        intent.setComponent(new ComponentName(packageName, mainAct));
        return intent;
    }
}
