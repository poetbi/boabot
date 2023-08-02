package cn.boasoft.boabot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.boasoft.boabot.library.file;

public class agree extends base {
    private file file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agree);
        file = new file(agree.this);

        setData();
    }

    private void setData() {
        String content = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            InputStream is = getResources().openRawResource(R.raw.license);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            content = bos.toString();
            is.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(content != null){
            ((TextView) findViewById(R.id.license)).setText(content);
        }
    }

    public void doAgree(View view) {
        file.setIntKV("agree", 1);
        setResult(RESULT_OK, new Intent());
        finish();
    }

    public void doDisagree(View view) {
        file.setIntKV("agree", 0);
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }
}