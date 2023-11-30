package com.achim.mapprj;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.achim.webinterface.MyWebChromeClient;
import com.achim.webinterface.MyWebViewClient;
import com.achim.common.module;
import com.achim.webinterface.WebViewJavaScriptInterface;
import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    private WebView mainWebView;
    private String serverUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadActionEvent();
    }

    private void loadActionEvent() {
        mainWebView = (WebView) findViewById(R.id.mainWebView);

        new module().StatusBarTransparent(MainActivity.this); // 현재 액티비티의 상태바 투명처리

        WebSettings mainWebViewSettings = mainWebView.getSettings();
        mainWebViewSettings.setJavaScriptEnabled(true); // 자바스크립트 사용여부
        mainWebViewSettings.setJavaScriptCanOpenWindowsAutomatically(true);  // 자바스크립트가 창을 자동으로 열 수 있게할지 여부
        mainWebViewSettings.setDomStorageEnabled(true); // localStorage 사용을 위해
        mainWebViewSettings.setLoadsImagesAutomatically(true); // 이미지 자동 로드
        mainWebViewSettings.setUseWideViewPort(true); // wide viewport 설정
        mainWebViewSettings.setLoadWithOverviewMode(true); //컨텐츠가 웹뷰보다 클때 스크린크기에 맞추기
        mainWebViewSettings.setSupportZoom(false); // 줌설정
        mainWebViewSettings.setBuiltInZoomControls(true); // 줌 컨트롤 제거
        mainWebViewSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 캐시설정
        mainWebViewSettings.setAllowFileAccess(true); // 웹뷰 내에서 파일 액세스 활성화 여부
        mainWebViewSettings.setSupportMultipleWindows(true);
        // HTML5 --
        mainWebViewSettings.setGeolocationEnabled(true); // 위치 탐색을 위한 HTML5 Geolocation 활성화
        mainWebViewSettings.setAppCacheEnabled(true);
        mainWebViewSettings.setDatabaseEnabled(true);
        mainWebViewSettings.setDomStorageEnabled(true);
        // -- HTML5
        mainWebViewSettings.setUserAgentString(mainWebViewSettings.getUserAgentString() + " MapPrj"); // 유저에이전트 커스텀

        mainWebView.addJavascriptInterface(new WebViewJavaScriptInterface(this, mainWebView), "MapPrj"); // 웹, 앱 통신 정의

        mainWebView.setWebChromeClient(new MyWebChromeClient()); // alert, confirm, window.open 등 정의

        //mainWebView.setWebViewClient(new WebViewClient() {
        mainWebView.setWebViewClient(new MyWebViewClient(MainActivity.this) {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                /**
                 * SSL 인증 에러 발생 시 앱이 강제종료가 되지 않고 계속 진행하기 위해서 처리
                 * 인증받지 않은 SSL 인증서를 사용하였을 경우에도 에러 발생함.
                 */
                handler.proceed();
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }


            LottieAnimationView loadingImgView;
            /**
             * 웹페이지 로딩이 시작할 때 처리
             */
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loadingImgView = new module().LoadingViewBegin(MainActivity.this, Context.WINDOW_SERVICE, getApplicationContext());
            }

            /**
             * 웹페이지 로딩이 끝났을 때 처리
             */
            @Override
            public void onPageFinished(WebView view, String url) {
                new module().LoadingViewEnd(MainActivity.this, Context.WINDOW_SERVICE, loadingImgView);
            }
        });

        serverUrl = getString(R.string.web_server_protocol) + "://" + getString(R.string.web_server_ip) + ":" + getString(R.string.web_server_port);
        mainWebView.loadUrl(serverUrl);
    }

    private long backBtnTime = 0;
    @Override
    public void onBackPressed() {
        if(mainWebView.canGoBack()){
            mainWebView.goBack();
        }else{
            long curTime = System.currentTimeMillis();
            long gapTime = curTime - backBtnTime;
            if (mainWebView.canGoBack()) {
                mainWebView.goBack();
            } else if (0 <= gapTime && 2000 >= gapTime) {
                super.onBackPressed();
            } else {
                backBtnTime = curTime;
                Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /* [Android] EditText가 아닌 다른 곳 클릭시 키보드 내리기 */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View focusView = getCurrentFocus();
        if (focusView != null) {
            Rect rect = new Rect();
            focusView.getGlobalVisibleRect(rect);
            int x = (int) ev.getX(), y = (int) ev.getY();
            if (!rect.contains(x, y)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}