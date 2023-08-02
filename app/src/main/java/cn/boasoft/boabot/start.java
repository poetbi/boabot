package cn.boasoft.boabot;

import static cn.boasoft.boabot.library.tool._error;
import static cn.boasoft.boabot.library.tool.time2str;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.king.app.dialog.AppDialog;
import com.king.app.dialog.AppDialogConfig;
import com.king.app.updater.AppUpdater;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import cn.boasoft.boabot.adapter.event;
import cn.boasoft.boabot.databinding.ActivityStartBinding;
import cn.boasoft.boabot.library.app;
import cn.boasoft.boabot.library.check;
import cn.boasoft.boabot.library.file;
import cn.boasoft.boabot.library.perm;
import cn.boasoft.boabot.library.sqlite;

public class start extends base {
    private ActivityStartBinding binding;
    private ArrayList<Bundle> tasks;
    private file file;
    private app app;
    private sqlite db;
    private perm perm;
    private boolean opened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        file = new file(start.this);
        app = new app(start.this);
        db = new sqlite(start.this);
        perm = new perm(start.this);

        setData();
        if(firstRun()) {
            Intent intent = new Intent(start.this, agree.class);
            startActivityForResult(intent, 1);
        } else {
            setTimer();
            checkRobot();
            checkPerm();
            execUpdate();
            new check(start.this);
        }

        int logs = file.getIntKV("logs");
        long time = System.currentTimeMillis() - (long) logs * 86400 * 1000;
        db.delLog(time);
    }

    @Override
    protected void onDestroy() {
        if(db != null) db.close();
        super.onDestroy();
    }

    private boolean firstRun(){
        int agree = file.getIntKV("agree");
        return agree != 1;
    }

    private void setData(){
        ArrayList<Bundle> taskList;
        Bundle task;
        StringBuilder text;
        int i;

        taskList = db.getTasksLast();
        if(taskList.size() > 0){
            text = new StringBuilder();
            for (i = 0; i < taskList.size(); i++) {
                task = taskList.get(i);
                text.append(i + 1).append(". ").append(task.getString("name")).append(" (").append(time2str("yyyy-MM-dd HH:mm:ss", task.getLong("last"))).append(")\n");
            }
        }else{
            text = new StringBuilder(getString(R.string.none));
        }
        binding.lastExecute.setText(text.toString());

        loadAds();

        tasks = db.getTasks();
        ArrayList<String> arr = new ArrayList<>();
        for (i = 0; i < tasks.size(); i++) {
            task = tasks.get(i);
            arr.add(task.getString("name"));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(start.this, R.layout.item_select, arr);
        binding.tasks.setAdapter(adapter);

        binding.tasks1.setAdapter(adapter);
    }

    private void loadAds(){
        ViewFlipper ads = binding.ads;
        try {
            String str = file.read("ads.json");
            JSONArray arr = new JSONArray(str);
            if(arr.length() > 0){
                ads.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            JSONObject item = arr.getJSONObject(ads.getDisplayedChild());
                            String link = item.getString("link");
                            app.run("web", link);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                JSONObject v;
                LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                for (int i = 0; i < arr.length(); i++) {
                    v = arr.getJSONObject(i);
                    File img = file.path(v.getString("img"), false);
                    ads.addView(getImageView(img), layoutParams);
                }

                ads.setInAnimation(start.this, R.anim.slide_in);
                ads.setOutAnimation(start.this, R.anim.slide_out);
            }else{
                loadAdsDefault();
            }
        } catch (JSONException e) {
            loadAdsDefault();
        }
    }

    private ImageView getImageView(File img){
        ImageView image = new ImageView(start.this);
        image.setImageURI(Uri.fromFile(img));
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        return image;
    }

    private void loadAdsDefault(){
        ImageView image = new ImageView(start.this);
        image.setBackgroundResource(R.drawable.boasoft);
        binding.ads.addView(image);

        binding.ads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                app.run("web", "http://boasoft.top");
            }
        });
    }

    private void setTimer() {
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        if(pm.isInteractive()) {
            File timer = file.path("timer", false);
            long time = System.currentTimeMillis() - timer.lastModified();
            if (time > 100 * 1000) {
                Intent i = new Intent(start.this, timer.class);
                startService(i);
            }
        }
    }

    private void checkRobot() {
        int robot = file.getIntKV("robot");
        if(robot == 1 && !robotRunning()){
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Toast.makeText(start.this, _error(8), Toast.LENGTH_LONG).show();
        }
    }

    private boolean robotRunning() {
        String service = new ComponentName(getPackageName(), robot.class.getName()).flattenToString();
        String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (settingValue != null) {
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
            splitter.setString(settingValue);
            while (splitter.hasNext()) {
                String accessibilityService = splitter.next();
                if (accessibilityService.equalsIgnoreCase(service)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void execUpdate(){
        File update = file.path("update.txt", false);
        boolean isShow = System.currentTimeMillis() >= (update.lastModified() + 86400 * 1000);
        String url = file.read("update.txt").trim();
        if(isShow && !url.isEmpty()){
            AppDialogConfig config = new AppDialogConfig(start.this);
            config.setTitle(getString(R.string.dialog_tit_app))
                    .setConfirm(getString(R.string.dialog_btn_up))
                    .setContent(getString(R.string.dialog_msg_app))
                    .setOnClickConfirm(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            file.delete("update.txt");
                            new AppUpdater.Builder().setUrl(url).build(start.this).start();
                            AppDialog.INSTANCE.dismissDialog();
                        }
                    })
                    .setOnClickCancel(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            file.touch("update.txt");
                        }
                    });
            AppDialog.INSTANCE.showDialog(start.this, config);
        }
    }

    private void checkPerm(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!perm.checkAll()){
                requestPermissions(perm.get(), 1);
            }
        }
    }

    private void initData(){
        file.setIntKV("robot", 1);

        try {
            String[] files = getAssets().list("demo");
            InputStream in = null;
            ByteArrayOutputStream bos = null;
            for(String path : files){
                in = getAssets().open("demo/"+ path);
                byte[] buff = new byte[1024];
                bos = new ByteArrayOutputStream();
                int len;
                while ((len = in.read(buff)) != -1){
                    bos.write(buff, 0, len);
                }
                db.importTask(bos.toString());
            }
            if(in != null) in.close();
            if(bos != null) bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                initData();
                setTimer();
                checkRobot();
            } else {
                finishAffinity();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (!perm.checkRes(grantResults)){
                Toast.makeText(start.this, R.string.permission, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void doTest(View view) {
        int position = binding.tasks.getSelectedItemPosition();
        if(tasks.size() > 0) {
            Bundle task = tasks.get(position);
            int auth = task.getInt("auth");
            if(auth < 1 && !opened) {
                long tid = task.getLong("id");
                String pkgName = db.getTaskApp(tid);
                if(pkgName != null && !pkgName.isEmpty()) {
                    app app = new app(start.this);
                    app.run("app", pkgName);
                    Toast.makeText(start.this, R.string.auth, Toast.LENGTH_LONG).show();

                    opened = true;
                    db.setTaskAuth(tid, 1);
                }else{
                    Toast.makeText(start.this, getString(R.string.flow_no_app) , Toast.LENGTH_SHORT).show();
                }
            }else{
                String tid = String.valueOf(task.getLong("id"));
                String val = binding.val.getText().toString();
                EventBus.getDefault().post(new event("test", tid, val));
                Toast.makeText(start.this, R.string.task_do_exec, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void doStop(View view) {
        EventBus.getDefault().post(new event("stop", null, null));
        Toast.makeText(start.this, R.string.task_do_stop, Toast.LENGTH_SHORT).show();
    }

    public void doEmail(View view) {
        Toast.makeText(start.this, "poetbi@163.com", Toast.LENGTH_LONG).show();
    }

    public void doWechat(View view) {
        Toast.makeText(start.this, "poetbi", Toast.LENGTH_SHORT).show();
    }

    public void doImport(View view) {
        binding.sqldiv.setTag("import");
        binding.sqldiv.setVisibility(View.VISIBLE);
    }

    public void doExport(View view) {
        binding.sqldiv.setTag("export");
        int position = binding.tasks1.getSelectedItemPosition();
        if(tasks.size() > 0) {
            Bundle task = tasks.get(position);
            long tid = task.getLong("id");
            String sql = db.exportTask(tid);
            binding.sqldiv.setVisibility(View.VISIBLE);
            binding.sql.setText(sql);
        }
    }

    public void doFinish(View view) {
        String tag = (String) binding.sqldiv.getTag();
        if(tag.equals("import")){
            String sql = binding.sql.getText().toString();
            db.importTask(sql);
            Toast.makeText(start.this, R.string.done, Toast.LENGTH_SHORT).show();
        }
        binding.sql.setText("");
        binding.sqldiv.setVisibility(View.GONE);
    }
}