package cn.boasoft.boabot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import cn.boasoft.boabot.library.sqlite;
import cn.boasoft.boabot.library.app;

public class task_do extends base {
	private sqlite db;
    private Bundle data;
    private ArrayList<Integer> values = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_do);
        initData();
        setData();
    }

    @Override
    protected void onDestroy(){
		if(db != null) db.close();
        super.onDestroy();
    }
	
    @SuppressLint("DiscouragedApi")
    public void doSave(View view) {
        int position;

        EditText name_view = findViewById(R.id.name);
        String name_val = name_view.getText().toString();
        if(name_val.isEmpty()){
            name_view.requestFocus();
            Toast.makeText(task_do.this, getString(R.string.task_input_name), Toast.LENGTH_SHORT).show();
            return;
        }else{
            data.putString("name", name_val);
        }

        Spinner status_view = findViewById(R.id.status);
        int status_val = status_view.getSelectedItemPosition();
        data.putInt("status", status_val);

        Spinner type_view = findViewById(R.id.type);
        int type_val = type_view.getSelectedItemPosition();
        data.putInt("type", type_val);

        if (type_val == 1) {
            StringBuilder exec = new StringBuilder();
            String[] tag = {"minute", "hour", "day", "week", "month"};
            int resId;
            Spinner exec_view;
            for (int i = 0; i < tag.length; i++) {
                resId = getResources().getIdentifier("exec_" + tag[i], "id", getPackageName());
                exec_view = findViewById(resId);
                position = exec_view.getSelectedItemPosition();
                if(i == 0){
                    exec.append(values.get(position + 1));
                }else{
                    exec.append(",").append(values.get(position));
                }
            }
            data.putString("exec", exec.toString());
        }else{
            data.putString("exec", "");
        }

        db = new sqlite(task_do.this);
        long id = data.getLong("id");
        if(id > 0) {
            db.editTask(data);
        }else{
            id = db.addTask(data);
        }

        if(status_val == 0){
            db.delTimer(id, 0);
        }

        setResult(RESULT_OK, new Intent());
        finish();
    }

    private void initData() {
        for(int i = -1; i < 60; i++){
            values.add(i);
        }
    }

    private void setData(){
        Intent intent = getIntent();
        data = intent.getBundleExtra("data");
        if(data != null && data.getLong("id") > 0){
            String name = data.getString("name");
            int status = data.getInt("status");
            int type = data.getInt("type");
            String exec = data.getString("exec");

            setText("name", name);
            setStatus(status);
            setType(type);

			if(exec.isEmpty()) exec = "0,-1,-1,-1,-1";
            String[] arr = exec.split(",");
            setMinute(Integer.parseInt(arr[0]));
            setHour(Integer.parseInt(arr[1]) + 1);
            setDay(Integer.parseInt(arr[2]) + 1);
            setWeek(Integer.parseInt(arr[3]) + 1);
            setMonth(Integer.parseInt(arr[4]) + 1);
        }else{
            data = new Bundle();
            setStatus(0);
            setType(0);
            setMinute(0);
            setHour(0);
            setDay(0);
            setWeek(0);
            setMonth(0);
        }
    }

    @SuppressLint("DiscouragedApi")
    private void setText(String id, String v) {
        int vId = getResources().getIdentifier(id, "id", getPackageName());
        EditText view = findViewById(vId);
        if(!v.isEmpty()) view.setText(v);
    }

    private void setStatus(int v){
        Spinner spinner = findViewById(R.id.status);
        String[] status_v = getResources().getStringArray(R.array.status_v);
        ArrayList<String> arr = new ArrayList<>(Arrays.asList(status_v));
        adapter = new ArrayAdapter<>(task_do.this, R.layout.item_select, arr);
        spinner.setAdapter(adapter);
        spinner.setSelection(v, true);
    }

    private void setType(int v){
        Spinner spinner = findViewById(R.id.type);
        String[] type_v = getResources().getStringArray(R.array.type_v);
        ArrayList<String> arr = new ArrayList<>(Arrays.asList(type_v));
        adapter = new ArrayAdapter<>(task_do.this, R.layout.item_select, arr);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            @SuppressLint("DiscouragedApi")
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                int resId;
                View obj;
                for(int i = 0; i < spinner.getCount(); i++) {
                    resId = getResources().getIdentifier("type_"+ i, "id", getPackageName());
                    obj = findViewById(resId);
                    if(obj != null){
                        if(i == position){
                            obj.setVisibility(View.VISIBLE);
                        }else{
                            obj.setVisibility(View.GONE);
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

    private void setMinute(int v){
        Spinner spinner = findViewById(R.id.exec_minute);
        ArrayList<String> arr = new ArrayList<>();
        for(int i = 0; i < 60; i++){
            arr.add(i + getString(R.string.unit_minute));
        }
        adapter = new ArrayAdapter<>(task_do.this, R.layout.item_select, arr);
        spinner.setAdapter(adapter);
        spinner.setSelection(v, true);
    }

    private void setHour(int v){
        Spinner spinner = findViewById(R.id.exec_hour);
        ArrayList<String> arr = new ArrayList<>();
        arr.add(getString(R.string.per_hour));
        for(int i = 0; i < 24; i++){
            arr.add(i + getString(R.string.unit_hour));
        }
        adapter = new ArrayAdapter<>(task_do.this, R.layout.item_select, arr);
        spinner.setAdapter(adapter);
        spinner.setSelection(v, true);
    }

    private void setDay(int v){
        Spinner spinner = findViewById(R.id.exec_day);
        ArrayList<String> arr = new ArrayList<>();
        arr.add(getString(R.string.per_day));
        for(int i = 0; i < 31; i++){
            arr.add((i + 1) + getString(R.string.unit_day));
        }
        adapter = new ArrayAdapter<>(task_do.this, R.layout.item_select, arr);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                if(position > 0){
                    ((Spinner) findViewById(R.id.exec_week)).setSelection(0, true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> spinner) {

            }
        });
		
		spinner.setSelection(v, true);
    }

    private void setWeek(int v){
        Spinner spinner = findViewById(R.id.exec_week);
        ArrayList<String> arr = new ArrayList<>();
        arr.add(getString(R.string.per_day));
        String[] week_v = getResources().getStringArray(R.array.week_v);
        arr.addAll(Arrays.asList(week_v));
        adapter = new ArrayAdapter<>(task_do.this, R.layout.item_select, arr);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                if(position > 0){
                    ((Spinner) findViewById(R.id.exec_day)).setSelection(0, true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> spinner) {

            }
        });
		
        spinner.setSelection(v, true);
    }

    private void setMonth(int v){
        Spinner spinner = findViewById(R.id.exec_month);
        ArrayList<String> arr = new ArrayList<>();
        arr.add(getString(R.string.per_month));
        for(int i = 0; i < 12; i++){
            arr.add((i + 1) + getString(R.string.unit_month));
        }
        adapter = new ArrayAdapter<>(task_do.this, R.layout.item_select, arr);
        spinner.setAdapter(adapter);
        spinner.setSelection(v, true);
    }

    public void doLink(View view) {
        app app = new app(task_do.this);
        app.run("web", "http://boasoft.cn");
    }
}