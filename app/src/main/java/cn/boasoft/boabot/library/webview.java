package cn.boasoft.boabot.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.boasoft.boabot.R;
import cn.boasoft.boabot.adapter.event;

public class webview {
    private Context context;
    private WebView wv;
    private String js;
    private Drawable icon;

    public webview(Context context, WebView wv, String jsFile){
        this.context = context;
        this.wv = wv;
        if(jsFile != null && !jsFile.isEmpty()){
            loadJs(jsFile);
        }
        init();

        Resources resources = context.getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon);
        icon = new BitmapDrawable(resources, bitmap);
    }

    public void chooser(){
        wv.addJavascriptInterface(new javascript(), "app");
    }

    public void open(String url){
        wv.loadUrl(url);
    }

    private void loadJs(String jsFile){
        try {
            InputStream in = context.getAssets().open(jsFile);
            byte[] buff = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len;
            while ((len = in.read(buff)) != -1){
                bos.write(buff, 0, len);
            }
            js = bos.toString();
            in.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        WebSettings setting = wv.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setSupportZoom(true); //缩放开关
        setting.setLoadWithOverviewMode(true);
        setting.setUseWideViewPort(true);
        setting.setBuiltInZoomControls(true);
        setting.setDomStorageEnabled(true);
        setting.setDatabaseEnabled(true);
        setting.setUserAgentString("Mozilla/5.0 (Linux; Android 13; LIO-AN00 Build/HUAWEILIO-AN00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/105.0.5195.77 UCBrowser/15.0.6.1196 Mobile Safari/537.36");

        wv.setWebViewClient(new WebViewClient() {
            @Override
            @SuppressLint("WebViewClientOnReceivedSslError")
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if(url.startsWith("http:") || url.startsWith("https:")){
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.startsWith("http:") || url.startsWith("https:")){
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    view.setForeground(icon);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (js != null && !js.isEmpty()) {
                    view.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {

                        }
                    });
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    view.setForeground(null);
                }
            }
        });
    }

    private class javascript {
        @JavascriptInterface
        public void choose(String obj, String text){
            EventBus.getDefault().post(new event("callback", obj, text));
        }
	} 
}
