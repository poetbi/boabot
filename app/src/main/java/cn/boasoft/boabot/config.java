package cn.boasoft.boabot;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import cn.boasoft.boabot.library.file;

public class config extends base {
	private file file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
		file = new file(config.this);

        setData();
    }

    private void setData() {
        int robot = file.getIntKV("robot");
        int remote = file.getIntKV("remote");
        int notice = file.getIntKV("notice");
        int logs = file.getIntKV("logs");

        if(robot == 1){
            ((RadioButton) findViewById(R.id.robot_y)).toggle();
        }else{
            ((RadioButton) findViewById(R.id.robot_n)).toggle();
        }

        LinearLayout remote_div = findViewById(R.id.remote_div);
        if(remote == 1){
            remote_div.setVisibility(View.VISIBLE);
            ((RadioButton) findViewById(R.id.remote_y)).toggle();

            String user = file.getKV("user", "");
            String pass = file.getKV("pass", "");
            ((EditText) findViewById(R.id.user)).setText(user);
            ((EditText) findViewById(R.id.pass)).setText(pass);
        }else{
            remote_div.setVisibility(View.GONE);
            ((RadioButton) findViewById(R.id.remote_n)).toggle();
        }

        RadioGroup remotes = findViewById(R.id.remotes);
        remotes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.remote_y){
                    remote_div.setVisibility(View.VISIBLE);
                }else{
                    remote_div.setVisibility(View.GONE);
                }
            }
        });

        if(notice == 1){
            ((CheckBox) findViewById(R.id.notice)).setChecked(true);
        }

        ((EditText) findViewById(R.id.logs)).setText(String.valueOf(logs));
    }

    public void doSave(View view) {
        int robot = 1;
        if(((RadioButton) findViewById(R.id.robot_n)).isChecked()){
            robot = 0;
        }
        file.setIntKV("robot", robot);

        int remote = 0;
        if(((RadioButton) findViewById(R.id.remote_y)).isChecked()){
            remote = 1;
        }
        if(remote == 1) {
            String user = ((EditText) findViewById(R.id.user)).getText().toString();
            user = user.trim();
            if(user.isEmpty()){
                Toast.makeText(config.this, R.string.config_user_set, Toast.LENGTH_SHORT).show();
                return;
            }

            String pass = ((EditText) findViewById(R.id.pass)).getText().toString();
            pass = pass.trim();
            if(pass.isEmpty()){
                Toast.makeText(config.this, R.string.config_pass_set, Toast.LENGTH_SHORT).show();
                return;
            }

            file.setKV("user", user);
            file.setKV("pass", pass);
        }
        file.setIntKV("remote", remote);

        boolean check = ((CheckBox) findViewById(R.id.notice)).isChecked();
        int notice = check ? 1 : 0;
        file.setIntKV("notice", notice);

        int logs = 0;
        String logstr = ((EditText) findViewById(R.id.logs)).getText().toString();
        try {
            logs = Integer.parseInt(logstr);
        }catch (Exception ignored){

        }
        file.setIntKV("logs", logs);

        Toast.makeText(config.this, R.string.done, Toast.LENGTH_SHORT).show();
    }
}