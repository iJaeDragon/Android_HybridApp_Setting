package com.achim.api;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

import com.achim.common.module;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// Rest API calling task
public class RestApiHelper {
    int defaultTimeoutSecond = 5;
    

    JSONObject result;

    /**
     * @param serverUrl, activity
     * @return JSONObject DataType으로 on(boolean), resultCode(int) 반환
     *  서버상태체크를 위한 API 호출 메서드 입니다.
     *  안드로이드 스튜디오 4.0 버전 부터는 메인 쓰레드 에서 통신을 하는 것을 막아 놨기 때문에 다른 스레드를 이용 해서 통신을 해야 하기에 new Thread()를 활용하여 해당 메서드를 호출해야합니다.
     *  ! Exception(android.os.NetworkOnMainThreadException)이 발생합니다.
     */
    public JSONObject getServerStatus(String serverUrl, Activity activity) throws JSONException {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(defaultTimeoutSecond, TimeUnit.SECONDS)
                    .writeTimeout(defaultTimeoutSecond, TimeUnit.SECONDS)
                    .readTimeout(defaultTimeoutSecond, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                .url(serverUrl)
                .build(); //GET Request

            //동기 처리시 execute함수 사용
            Response response = client.newCall(request).execute();

            //출력
            String message = response.body().string();
            result = new JSONObject(message);
        } catch (SocketTimeoutException e) { // OkHttpClient.Builder() Timeout Exception

            // java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
            // Thread 안에 Thread를 사용할때 발생하는 에러로 아래처럼 Handler를 선언하여 사용하였습니다.
            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new module().openAlertPositiveDialog(activity,"알림!", "서버 연결에 실패하였습니다. \n잠시 후 다시 시도해주세요. \n사유: 타임아웃", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    });
                }
            }, defaultTimeoutSecond * 1000);
        } catch (Exception e){
            e.printStackTrace();

            new module().appRestart(); // 알수없는 에러 앱 강제 재실행
        }

        return result;
    }
}