package com.achim.webinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.achim.common.module;

public class WebViewJavaScriptInterface extends AppCompatActivity {
    private Context context;
    private WebView webview;

    public WebViewJavaScriptInterface(Context context, WebView webview) {
        this.context = context;
        this.webview = webview;
    }

    @JavascriptInterface
    public void getStatusBarHeight(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int statusBarHeight = new module().getStatusBarHeight(context);
                webview.loadUrl("javascript:getStatusBarHeight_CallBack('" + statusBarHeight + "')");
            }
        });
    }
}
