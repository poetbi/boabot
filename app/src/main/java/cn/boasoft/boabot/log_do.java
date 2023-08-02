package cn.boasoft.boabot;

import static cn.boasoft.boabot.library.tool._error;
import static cn.boasoft.boabot.library.tool.time2str;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class log_do extends base {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_do);
        setData();
    }

    private void setData(){
        Intent intent = getIntent();
        Bundle data = intent.getBundleExtra("data");

        String id = String.valueOf(data.getLong("id"));
        ((TextView) findViewById(R.id.log_id)).setText(id);

        long fid = data.getLong("fid");
		if(fid > 0){
            findViewById(R.id.log_div).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.log_fid)).setText(String.valueOf(fid));
			
			String tid = String.valueOf(data.getLong("tid"));
            ((TextView) findViewById(R.id.log_tid)).setText(tid);
						
			String memo = data.getString("memo");
            ((TextView) findViewById(R.id.log_memo)).setText(memo);
		}else{
            findViewById(R.id.log_div).setVisibility(View.GONE);
        }

        String val = data.getString("val");
        ((TextView) findViewById(R.id.log_val)).setText(val);

        int code = data.getInt("res");
		String res = _error(code) + "("+ code +")";
        ((TextView) findViewById(R.id.log_res)).setText(res);

        String time = time2str("yyyy-MM-dd HH:mm:ss", data.getLong("time"));
        ((TextView) findViewById(R.id.log_time)).setText(time);
    }
}