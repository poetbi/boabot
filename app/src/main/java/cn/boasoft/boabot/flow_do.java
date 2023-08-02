package cn.boasoft.boabot;

import static cn.boasoft.boabot.library.tool.indexof;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cn.boasoft.boabot.adapter.event;
import cn.boasoft.boabot.library.app;
import cn.boasoft.boabot.library.sqlite;
import cn.boasoft.boabot.library.webview;

public class flow_do extends base {
    private Bundle data;
    private Bundle start;
    private ArrayAdapter<String> adapter;
    private ArrayList<HashMap<String, String>> apps;
    private int index = 1;
    private sqlite db;
    private String[] memo = new String[3];
    private int update = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_do);
        db = new sqlite(flow_do.this);
        EventBus.getDefault().register(this);
        initData();
        setData();
    }

    @Override
    protected void onDestroy(){
		if(db != null) db.close();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void doSave(View view) {
        Spinner cmd_view = findViewById(R.id.cmd);
        int cmd_pos = cmd_view.getSelectedItemPosition();
        String[] cmd_k = getResources().getStringArray(R.array.cmd_k);
        String cmd = cmd_k[cmd_pos];
        data.putString("cmd", cmd);

        String obj = getText("obj");
        String val = getText("val");

        if(cmd.equals("start") && start.getLong("id") != data.getLong("id")){
            Toast.makeText(flow_do.this, R.string.flow_start_have, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean ctl_for = ((CheckBox) findViewById(R.id.ctl_for)).isChecked();

        if(cmd.equals("click") && start.getString("obj").equals("app")){
            obj = handle_obj(obj, R.id.obj1_type, R.id.obj1_type1);
        }

        if(cmd.equals("input") && start.getString("obj").equals("app")){
            obj = handle_obj(obj, R.id.obj2_type, R.id.obj2_type1);
        }

        if(cmd.equals("check") && start.getString("obj").equals("app")){
            obj = handle_obj(obj, R.id.obj3_type, R.id.obj3_type1);
        }

        int click_val = 0;
        if(cmd.equals("click")){
            try {
                click_val = Integer.parseInt(val);
            }catch (Exception ignored){

            }
            if(click_val > 1) obj = ""; //点击系统按键无需对象
        }
        if(obj.isEmpty() && click_val <= 1){
            Toast.makeText(flow_do.this, getString(R.string.flow_obj_required), Toast.LENGTH_SHORT).show();
            return;
        }else{
            data.putString("obj", obj);
        }

        if(cmd.equals("input")){
            val = ((EditText) findViewById(R.id.val_2)).getText().toString();
        }
        if(val.isEmpty()){
            Toast.makeText(flow_do.this, getString(R.string.flow_val_required), Toast.LENGTH_SHORT).show();
            return;
        }else{
            data.putString("val", val);
        }

        int ctl_loop = 0;
        int ctl_wait = 1;
        int ctl_step = ((Spinner) findViewById(R.id.ctl_step)).getSelectedItemPosition();
        try {
            if(cmd.equals("click") && click_val != 2){ //返回支持循环
                ctl_loop = ctl_for ? 1 : 0;
            }else{
                ctl_loop = Integer.parseInt(getText("ctl_loop"));
                if(ctl_loop < 1) ctl_loop = 1;
            }
            ctl_wait = Integer.parseInt(getText("ctl_wait"));
        }catch (Exception ignored){

        }
        if (ctl_wait < 1) ctl_wait = 1;
        if(!obj.isEmpty() && obj.charAt(0) == '*' && ctl_wait < 3){
            ctl_wait = 3;
            Toast.makeText(flow_do.this, getString(R.string.flow_wait_min), Toast.LENGTH_SHORT).show();
        }
        String ctl = ctl_loop +","+ ctl_wait +","+ ctl_step;
        data.putString("ctl", ctl);

        val = getText("memo");
        if(val.isEmpty()){
            Toast.makeText(flow_do.this, getString(R.string.flow_memo_required), Toast.LENGTH_SHORT).show();
            return;
        }else{
            data.putString("memo", val);
        }

        if(data.getLong("id") > 0) {
            db.editFlow(data);
        }else{
            db.addFlow(data);
        }

        Intent intent = new Intent();
        if(cmd.equals("start")) {
            long tid = data.getLong("tid");
            if(obj.equals("app")) {
                db.setTaskAuth(tid, 0);
            }else{
                db.setTaskAuth(tid, 1);
            }
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    private void initData(){
        Intent intent = getIntent();
        data = intent.getBundleExtra("data");

        start = db.getTaskStart(data.getLong("tid"));

        apps = new ArrayList<>();
        PackageManager pm = getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for(PackageInfo pi : packages) {
            if((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 &&
                    (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0){ //用户APP
                HashMap<String, String> app = new HashMap<>();
                String appName = pi.applicationInfo.loadLabel(pm).toString();
                if(pi.applicationInfo.packageName.equals(getPackageName())) continue;
                if(!appName.equals(pi.applicationInfo.packageName)) {
                    app.put("appName", appName); //应用程序名称
                    app.put("packageName", pi.applicationInfo.packageName); //应用程序包名
                    apps.add(app);
                }
            }
        }

        notifyRobot();
    }

    @SuppressLint("DiscouragedApi")
    private String getText(String id){
        int vId = getResources().getIdentifier(id, "id", getPackageName());
        EditText view = findViewById(vId);
        return view.getText().toString();
    }

    private void setData(){
        int ctl_step = 0;
        if(data != null && data.getLong("id") > 0){
            String cmd = data.getString("cmd");
            String[] cmd_k = getResources().getStringArray(R.array.cmd_k);
            setCmd(indexof(cmd, cmd_k));

            setText("obj", data.getString("obj"));
            setText("val", data.getString("val"));

            String[] ctl = data.getString("ctl").split(",");
            if(cmd.equals("click") && ctl[0].equals("1")){
                ((CheckBox) findViewById(R.id.ctl_for)).setChecked(true);
            }
            setText("ctl_loop", ctl[0]);

            setText("ctl_wait", ctl[1]);
            ctl_step = Integer.parseInt(ctl[2]);

            setText("memo", data.getString("memo"));
        }else{
            setCmd(0);
            setMemo();
        }

        Spinner spinner = findViewById(R.id.ctl_step);
        String[] step_v = getResources().getStringArray(R.array.step_v);
        ArrayList<String> arr = new ArrayList<>(Arrays.asList(step_v));
        adapter = new ArrayAdapter<>(flow_do.this, R.layout.item_select, arr);
        spinner.setAdapter(adapter);
        spinner.setSelection(ctl_step, true);
    }

    @SuppressLint("DiscouragedApi")
    private void setText(String id, String v) {
        int vId = getResources().getIdentifier(id, "id", getPackageName());
        EditText view = findViewById(vId);
        if(!v.isEmpty()) view.setText(v);
    }

    private void setCmd(int v){
        Spinner spinner = findViewById(R.id.cmd);
        String[] cmd_v = getResources().getStringArray(R.array.cmd_v);
        ArrayList<String> arr = new ArrayList<>(Arrays.asList(cmd_v));
        adapter = new ArrayAdapter<>(flow_do.this, R.layout.item_select, arr);
        spinner.setAdapter(adapter);

        CheckBox ctl_for = findViewById(R.id.ctl_for);
        LinearLayout ctl_loop = findViewById(R.id.ctl_loop0);
        Spinner ctl_step = findViewById(R.id.ctl_step);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            @SuppressLint("DiscouragedApi")
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                memo[0] = spinner.getSelectedItem().toString();

                findViewById(R.id.obj).setVisibility(View.GONE);
                findViewById(R.id.val).setVisibility(View.GONE);

                if(position == 1){ //点击
                    ctl_for.setVisibility(View.VISIBLE);
                    ctl_step.setVisibility(View.VISIBLE);
                }else if(position == 4){ //跳转
                    ctl_loop.setVisibility(View.VISIBLE);
                }else{
                    ctl_for.setVisibility(View.GONE);
                    ctl_loop.setVisibility(View.GONE);
                    ctl_step.setVisibility(View.GONE);
                }

                int objId, valId;
                View obj, val;
                for(int i = 0; i < spinner.getCount(); i++) {
                    objId = getResources().getIdentifier("obj_"+ i, "id", getPackageName());
                    valId = getResources().getIdentifier("val_"+ i, "id", getPackageName());
                    obj = findViewById(objId);
                    val = findViewById(valId);
                    if(obj != null){
                        if(i == position) {
                            obj.setVisibility(View.VISIBLE);
                            val.setVisibility(View.VISIBLE);
                            switch (position){
                                case 0: setObjStart(); break;
                                case 1: setObjClick(); break;
                                case 2: setObjInput(); break;
                                case 3: setObjCheck(); break;
                                case 4: setObjGoto(); break;
                                case 5: setObjClose(); break;
                            }
                        }else{
                            obj.setVisibility(View.GONE);
                            val.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> spinner) {

            }
        });
		
		spinner.setSelection(v, true);
    }

    private void setObjStart(){
        EditText obj_view = findViewById(R.id.obj);
        String obj = obj_view.getText().toString();
        String[] start_k = getResources().getStringArray(R.array.start_k);
        int v = indexof(obj, start_k);

        Spinner spinner = findViewById(R.id.obj_0);
        String[] start_v = getResources().getStringArray(R.array.start_v);
        ArrayList<String> arr = new ArrayList<>(Arrays.asList(start_v));
        adapter = new ArrayAdapter<>(flow_do.this, R.layout.item_select, arr);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                memo[1] = spinner.getSelectedItem().toString();
                obj_view.setText(start_k[position]);
                setValStart(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> spinner) {

            }
        });
		
        if(v >= arr.size()) v = 0;
		spinner.setSelection(v, true);
    }

    private void setObjClick(){
        memo[1] = "?";
        index = 1;
        obj_chooser(R.id.obj1_type, R.id.obj1_type0, R.id.obj1_type1);
    }

    private void setObjInput(){
        memo[1] = "?";
        index = 2;
        obj_chooser(R.id.obj2_type, R.id.obj2_type0, R.id.obj2_type1);
    }

    private void setObjCheck(){
        memo[1] = "?";
        index = 3;
        obj_chooser(R.id.obj3_type, R.id.obj3_type0, R.id.obj3_type1);
    }

    private void setObjGoto(){
        EditText obj_view = findViewById(R.id.obj);
        String obj = obj_view.getText().toString();
        int v = 0;
        try {
            v = Integer.parseInt(obj);
        }catch (Exception ignored){

        }

        Spinner spinner = findViewById(R.id.obj_4);
        String[] goto_v = getResources().getStringArray(R.array.goto_v);
        ArrayList<String> arr = new ArrayList<>(Arrays.asList(goto_v));
        adapter = new ArrayAdapter<>(flow_do.this, R.layout.item_select, arr);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                memo[1] = spinner.getSelectedItem().toString();
                obj_view.setText(String.valueOf(position));
                setValGoto(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> spinner) {

            }
        });

        if(v >= arr.size()) v = 0;
        spinner.setSelection(v, true);
    }

    private void setObjClose(){
        memo[1] = memo[2] = "";
        EditText view = findViewById(R.id.obj);
        view.setText(" ");
        view = findViewById(R.id.val);
        view.setText(" ");
    }

    private void setValStart(int position) {
        EditText val_view = findViewById(R.id.val);
        Spinner spinner = findViewById(R.id.val_0);
        if(position == 0) {
            spinner.setVisibility(View.VISIBLE);
            val_view.setVisibility(View.GONE);
            int v = 0;
            String val = val_view.getText().toString();

            ArrayList<String> arr = new ArrayList<>();
            for (int i = 0; i < apps.size(); i++) {
                HashMap<String, String> app = apps.get(i);
                arr.add(app.get("appName"));
                if (val.equals(app.get("packageName"))) {
                    v = i;
                }
            }

            adapter = new ArrayAdapter<>(flow_do.this, R.layout.item_select, arr);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                    memo[2] = spinner.getSelectedItem().toString();
                    HashMap<String, String> app = apps.get(position);
                    val_view.setText(app.get("packageName"));
                }

                @Override
                public void onNothingSelected(AdapterView<?> spinner) {

                }
            });
			
            if(v >= arr.size()) v = 0;
			spinner.setSelection(v, true);			
        }else{
            spinner.setVisibility(View.GONE);
            val_view.setVisibility(View.VISIBLE);
            String val = val_view.getText().toString();
            if(!val.equals(data.getString("val"))) {
                val_view.setText("");
            }
            String[] tips = getResources().getStringArray(R.array.start_tip);
            val_view.setHint(tips[position]);
            val_view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    memo[2] = val_view.getText().toString();
                }
            });
        }
    }

    private void setValClick(){
        EditText val_view = findViewById(R.id.val);
        Spinner spinner = findViewById(R.id.val_1);
        String val = val_view.getText().toString();
        int v = 0;
        try {
            v = Integer.parseInt(val);
        }catch (Exception ignored){

        }

        String[] click_v = getResources().getStringArray(R.array.click_v);
        ArrayList<String> arr = new ArrayList<>(Arrays.asList(click_v));
        adapter = new ArrayAdapter<>(flow_do.this, R.layout.item_select, arr);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                memo[2] = spinner.getSelectedItem().toString();

                if(position == 2){
                    findViewById(R.id.ctl_for).setVisibility(View.GONE);
                    findViewById(R.id.ctl_loop0).setVisibility(View.VISIBLE);
                }else{
                    findViewById(R.id.ctl_for).setVisibility(View.VISIBLE);
                    findViewById(R.id.ctl_loop0).setVisibility(View.GONE);
                }

                if(position == 3){
                    if(Build.VERSION.SDK_INT < 28){
                        Toast.makeText(flow_do.this, getString(R.string.build_sdk28), Toast.LENGTH_SHORT).show();
                        spinner.setSelection(0);
                    }
                }
                val_view.setText(String.valueOf(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> spinner) {

            }
        });
		
		if(v >= arr.size()) v = 0;
        spinner.setSelection(v, true);		
    }

    private void setValInput(){
        EditText val_view = findViewById(R.id.val);
        EditText input = findViewById(R.id.val_2);
        String val = val_view.getText().toString();
        if(!val.isEmpty() && val.equals(data.getString("val"))) {
            input.setText(val);
        }
        memo[2] = "...";
    }

    private void setValCheck(){
        int[] check_val = new int[2];
        String[] check_memo = new String[2];
        EditText val_view = findViewById(R.id.val);
        String[] val = val_view.getText().toString().split(":");
        int v = 0;

        Spinner spinner = findViewById(R.id.check_obj);
        String[] check_obj_v = getResources().getStringArray(R.array.check_obj_v);
        ArrayList<String> arr = new ArrayList<>(Arrays.asList(check_obj_v));
        adapter = new ArrayAdapter<>(flow_do.this, R.layout.item_select, arr);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                check_val[0] = position;
                check_memo[0] = spinner.getSelectedItem().toString();
                val_view.setText(String.format("%d:%d", check_val[0], check_val[1]));
                memo[2] = check_memo[0] +":"+ check_memo[1];
            }

            @Override
            public void onNothingSelected(AdapterView<?> spinner) {

            }
        });
        if(val.length == 2) v = Integer.parseInt(val[0]);
        spinner.setSelection(v, true);


        spinner = findViewById(R.id.check_goto);
        long tid = data.getLong("tid");
        ArrayList<Bundle> flows = db.getFlowsByTid(tid);

        arr = new ArrayList<>();
        for (int i = 0; i < flows.size(); i++) {
            Bundle flow = flows.get(i);
            arr.add((i + 1) +" "+ flow.getString("memo"));
        }

        adapter = new ArrayAdapter<>(flow_do.this, R.layout.item_select, arr);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                long fid = data.getLong("id");
                Bundle flow = flows.get(position);
                if (fid > 0 && fid == flow.getLong("id")) {
                    spinner.setSelection(0);
                } else {
                    check_val[1] = position;
                    check_memo[1] = String.valueOf(position + 1);
                    val_view.setText(String.format("%d:%d", check_val[0], check_val[1]));
                    memo[2] = check_memo[0] +":"+ check_memo[1];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> spinner) {

            }
        });

        if(val.length == 2) v = Integer.parseInt(val[1]);
        spinner.setSelection(v, true);
    }

    private void setValGoto(int pos){
        EditText val_view = findViewById(R.id.val);
        Spinner spinner = findViewById(R.id.val_4);
        String val = val_view.getText().toString();
        int v = 0;

        if(pos == 0){
            try {
                v = Integer.parseInt(val);
            }catch (Exception ignored){

            }
            long tid = data.getLong("tid");
            ArrayList<Bundle> flows = db.getFlowsByTid(tid);

            ArrayList<String> arr = new ArrayList<>();
            for (int i = 0; i < flows.size(); i++) {
                Bundle flow = flows.get(i);
                arr.add((i + 1) +" "+ flow.getString("memo"));
            }

            adapter = new ArrayAdapter<>(flow_do.this, R.layout.item_select, arr);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                    long fid = data.getLong("id");
                    Bundle flow = flows.get(position);
                    if (fid > 0 && fid == flow.getLong("id")) {
                        spinner.setSelection(0);
                    } else {
                        memo[2] = "(" + spinner.getSelectedItem().toString() + ")";
                        val_view.setText(String.valueOf(position));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> spinner) {

                }
            });
			
            if(v >= arr.size()) v = 0;
			spinner.setSelection(v, true);
        } else {
            long tid = 0;
            try{
                tid = Long.parseLong(val);
            }catch (Exception ignored){

            }
            ArrayList<Bundle> tasks = db.getTasks();

            ArrayList<String> arr = new ArrayList<>();
            for (int i = 0; i < tasks.size(); i++) {
                Bundle task = tasks.get(i);
                arr.add(task.getString("name"));
                if(tid == task.getLong("id")){
                    v = i;
                }
            }

            adapter = new ArrayAdapter<>(flow_do.this, R.layout.item_select, arr);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                    Bundle task = tasks.get(position);
                    val_view.setText(String.valueOf(task.getLong("id")));
                }

                @Override
                public void onNothingSelected(AdapterView<?> spinner) {

                }
            });
			
            if(v >= arr.size()) v = 0;
			spinner.setSelection(v, true);			
        }
    }

    private void setMemo(){
        EditText memo_view = findViewById(R.id.memo);
        memo_view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String text = memo_view.getText().toString();
                if(text.equals("")){
                    memo_view.setText(String.format("%s %s %s", memo[0], memo[1], memo[2]));
                }
            }
        });
    }

    private void obj_chooser(int id_type, int id_type0, int id_type1){
        Spinner spinner = findViewById(id_type);
        if(start.getString("obj").equals("web")){
            spinner.setVisibility(View.GONE);
            choose_next();
        }else {
            int v = 0;
            EditText obj_view = findViewById(R.id.obj);
            TextView obj_type0 = findViewById(id_type0);
            EditText obj_type1 = findViewById(id_type1);

            String obj = obj_view.getText().toString();
            String[] obj_arr = obj.split("#");
            if (obj_arr.length == 3) {
                v = Integer.parseInt(obj_arr[2]);
                obj_type1.setText(String.format("%s#%s", obj_arr[0], obj_arr[1]));
            }

            String[] obj_type_v = getResources().getStringArray(R.array.obj_type_v);
            ArrayList<String> arr = new ArrayList<>(Arrays.asList(obj_type_v));
            adapter = new ArrayAdapter<>(flow_do.this, R.layout.item_select, arr);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                    if (position == 0) {
                        obj_type0.setVisibility(View.VISIBLE);
                        obj_type1.setVisibility(View.GONE);
                    } else {
                        obj_type0.setVisibility(View.GONE);
                        obj_type1.setVisibility(View.VISIBLE);
                    }
                    choose_next();
                }

                @Override
                public void onNothingSelected(AdapterView<?> spinner) {

                }
            });
            spinner.setSelection(v, true);
        }
    }

    private void choose_next(){
        switch (index) {
            case 1:
                setValClick();
                break;
            case 2:
                setValInput();
                break;
            case 3:
                setValCheck();
                break;
        }
    }

    private String handle_obj(String obj, int id_type, int id_type1){
        Spinner obj_type = findViewById(id_type);
        if(obj_type.getSelectedItemPosition() == 1){
            EditText obj_type_1 = findViewById(id_type1);
            String obj_value = obj_type_1.getText().toString();
            if(!obj_value.isEmpty()){
                obj = obj_value + "#1";
            }
        }
        return obj;
    }

    private void notifyRobot(){
        String pkgName = start.getString("val");
        if(pkgName != null && !pkgName.isEmpty()) {
            EventBus.getDefault().post(new event("package", pkgName, null));
        }
    }

    @SuppressLint("DiscouragedApi")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(event e) {
        if(e.type.equals("callback")){
            Spinner spinner = findViewById(R.id.cmd);
            if(spinner.getSelectedItemPosition() == 3 && e.data.charAt(0) == '*'){
                return; //判断指令坐标对象无效
            }

            String text = e.more;
            String id = e.data;
            setText("obj", id);

            int objId = getResources().getIdentifier("obj"+ index +"_type0", "id", getPackageName());
            TextView view = findViewById(objId);
            view.setText(text);
            memo[1] = text;

            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if(vibrator.hasVibrator()) vibrator.vibrate(500);
            Toast.makeText(flow_do.this, getString(R.string.flow_get) +" : "+ text, Toast.LENGTH_LONG).show();

            if(update == 1 && start.getString("obj").equals("app")){
                long tid = data.getLong("tid");
                db.setTaskAuth(tid, 1);
                update = 2;
            }
        }
    }

    public void doStart(View view) {
        if(start.getString("obj").equals("app")) {
            String pkgName = start.getString("val");
            if (pkgName != null && !pkgName.isEmpty()) {
                if (update == 0) update = 1;
                app app = new app(flow_do.this);
                app.run("app", pkgName);
            } else {
                Toast.makeText(flow_do.this, getString(R.string.flow_no_app), Toast.LENGTH_SHORT).show();
            }
        }else{
            FrameLayout bs = findViewById(R.id.browser);
            bs.setVisibility(View.VISIBLE);
            String tag = (String) bs.getTag();
            if(tag == null || tag.isEmpty()){
                webview wv = new webview(flow_do.this, findViewById(R.id.webview), "choose.js");
                wv.chooser();
                wv.open(start.getString("val"));
                bs.setTag("init");
            }
        }
    }

    public void doWvback(View view) {
        WebView wv = findViewById(R.id.webview);
        if(wv.canGoBack()) wv.goBack();
    }

    public void doWvclose(View view) {
        FrameLayout bs = findViewById(R.id.browser);
        bs.setVisibility(View.GONE);
    }

    public void doWvrefresh(View view){
        WebView wv = findViewById(R.id.webview);
        wv.reload();
    }
}