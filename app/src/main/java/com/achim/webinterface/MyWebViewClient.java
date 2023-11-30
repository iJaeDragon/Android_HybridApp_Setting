package com.achim.webinterface;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

// 클래스 정의 https://helloit.tistory.com/303

/**
 * 페이지 탐색 처리 참고 : https://developer.android.com/guide/webapps/webview#HandlingNavigation
 * 사용자가 WebView에서 웹페이지의 링크를 클릭하면 URL을 처리하는 앱이
 * Android에서 실행되는 것이 기본 동작입니다. 대개 기본 웹브라우저에 도착 URL이 열리고 로드됩니다.
 * 하지만 링크가 WebView 내에서 열리도록 WebView의 이 동작을 재정의할 수 있습니다.
 * 그러면 WebView에 의해 유지 관리되는 웹페이지 방문 기록을 통해 사용자가 앞뒤로 탐색할 수 있습니다.
 */
public class MyWebViewClient extends WebViewClient {
    private String TAG = "MyWebViewClient";
    private Context mApplicationContext =null;
    public MyWebViewClient(Context _applicationContext) {
        mApplicationContext = _applicationContext;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Log.d("DEBUG","shouldOverrideUrlLoading(view:"+view+ ", request:"+request+")");
        return super.shouldOverrideUrlLoading(view, request);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        Log.d("DEBUG","onPageStarted(view:"+view+ ", url:"+url+ ", favicon:"+favicon+")");
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        Log.d("DEBUG","onPageFinished(view:"+view+ ", url:"+url+")");
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Log.i(TAG, "onReceivedError() " + error.getErrorCode() + " ---> " + error.getDescription());
            onReceivedError(error.getErrorCode(),String.valueOf(error.getDescription()));

        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onReceivedError(errorCode,description);
        }

    }

    private void onReceivedError(int errorCode, String description){
        switch (errorCode) {
            case WebViewClient.ERROR_TIMEOUT:   //연결 시간 초과
            case WebViewClient.ERROR_CONNECT:   //서버로 연결 실패
                //case WebViewClient.ERROR_UNKNOWN:   // 일반 오류
            case WebViewClient.ERROR_FILE_NOT_FOUND: //404
            case WebViewClient.ERROR_HOST_LOOKUP :
            case WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME:
            case WebViewClient.ERROR_AUTHENTICATION:
            case WebViewClient.ERROR_PROXY_AUTHENTICATION:
            case WebViewClient.ERROR_IO:
            case WebViewClient.ERROR_REDIRECT_LOOP:
            case WebViewClient.ERROR_UNSUPPORTED_SCHEME:
            case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE:
            case WebViewClient.ERROR_BAD_URL:
            case WebViewClient.ERROR_FILE:
            case WebViewClient.ERROR_TOO_MANY_REQUESTS:
            case WebViewClient.ERROR_UNSAFE_RESOURCE:
                Log.d("DEBUG","WebViewClient,onReceivedError("+errorCode+") 에러 발생 "  );
                break;
        }
    }
}