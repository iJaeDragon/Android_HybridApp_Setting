package com.achim.webinterface;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;

// 클래스 정의 https://helloit.tistory.com/303

public class MyWebChromeClient extends WebChromeClient {

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
    }
    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        final Dialog dialog = new Dialog(view.getContext()); // 최상위 창 (다이얼로그)

        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT); // 다이얼로그 배경색상
        InsetDrawable inset = new InsetDrawable(back, -8, -8, -8, -8); // 다이얼로그 여백 (풀 스크린으로 보여지기 위해 음수를 주었습니다.)
        dialog.getWindow().setBackgroundDrawable(inset);

        RelativeLayout relativeLayout = new RelativeLayout(view.getContext()); // 다이얼로그에 넣을 레이아웃

        RelativeLayout.LayoutParams layoutParams;
        layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,// android:layout_width="match_parent"
                RelativeLayout.LayoutParams.WRAP_CONTENT // android:layout_height="match_parent"
        );

        // 닫기 버튼 동적 생성 start
        Button closeBtn = new Button(view.getContext());
        closeBtn.setId(View.generateViewId()); // 버튼 아래 영역에 웹뷰를 추가를 해야하므로, 임의로 id를 부여하였음 (minSDK 가 17 이상)
        closeBtn.setText("창 닫기"); //버튼에 들어갈 텍스트를 지정(String)
        closeBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 1);
        layoutParams.setMarginEnd(0);
        closeBtn.setLayoutParams(layoutParams);

        relativeLayout.addView(closeBtn); // 버튼을 레이아웃에 추가
        // 닫기 버튼 동적 생성 end

        layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,// android:layout_width="match_parent"
                RelativeLayout.LayoutParams.MATCH_PARENT // android:layout_height="match_parent"
        );

        // 웹뷰 동적 생성 start
        WebView newWebView = new WebView(view.getContext());
        WebSettings settings = newWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);

        // 레이아웃 종류 https://jdroid.tistory.com/8
        // RelativeLayout 속성 정리 https://seminzzang.tistory.com/17
        layoutParams.addRule(RelativeLayout.BELOW, closeBtn.getId()); // 버튼의 below (기준뷰 아래)
        newWebView.setLayoutParams(layoutParams);

        relativeLayout.addView(newWebView); // 웹뷰를 레이아웃에 추가
        // 웹뷰 동적 생성 end

        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(relativeLayout);
        dialog.show();

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() { // 온 키 이벤트
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                if(keyCode == KeyEvent.KEYCODE_BACK) {  // 뒤로가기 동작
                    if(newWebView.canGoBack()){ // 히스토리가 있을때
                        newWebView.goBack();
                    }else{ // 히스토리가 없을경우 다이얼로그 닫기
                        dialog.dismiss();
                    }
                    return true;
                }else{
                    return false;
                }
            }
        });
        newWebView.setWebViewClient(new MyWebViewClient(view.getContext()));
        newWebView.setWebChromeClient(new MyWebChromeClient() {
            @Override
            public void onCloseWindow(WebView window) {
                dialog.dismiss();
            }
        });


        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(newWebView);
        resultMsg.sendToTarget();
        return true;
    }
    @Override
    public void onCloseWindow(WebView window) {
        window.setVisibility(View.GONE);
        window.destroy();
        super.onCloseWindow(window);
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        new AlertDialog.Builder(view.getContext())
                .setTitle("")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();

                            }
                        })
                .setCancelable(false)
                .create()
                .show();

        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
        new AlertDialog.Builder(view.getContext())
                .setTitle("")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();

                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        })
                .create()
                .show();
        return true;
    }

    // WebView에서 GeoLocation을 사용하기 위해서.. (web에서 위치 가져오기) 사용하지 않는다면 제거
    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        super.onGeolocationPermissionsShowPrompt(origin, callback);
        callback.invoke(origin, true, false);
    }

}