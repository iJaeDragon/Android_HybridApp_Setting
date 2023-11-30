package com.achim.mapprj;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.achim.api.RestApiHelper;
import com.achim.common.module;
import com.airbnb.lottie.LottieAnimationView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class IntroActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro); //xml , java 소스 연결

        final Boolean[] introEndFlag = {false};
        final Boolean[] serverCheckEndFlag = {false};

        // -- 인트로 이미지
        LottieAnimationView animationView = animationView = findViewById(R.id.lottie);
        animationView.setAnimation("lottie_intro.json");
        animationView.playAnimation();
        animationView.setRepeatCount(0);

        TextView introImageCopyright = findViewById(R.id.introImageCopyright);
        introImageCopyright.setText("Intro image... \nCopyright 2022.lottiefiles_Lucian Mangu.All right reserved");

        animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Timer timer = new Timer();

                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if(introEndFlag[0] == true && serverCheckEndFlag[0] == true) {

                            Intent intent;

                            SharedPreferences preferences = getSharedPreferences("firstLoadFlag", MODE_PRIVATE);
                            if(preferences.getBoolean("mainFlag",true)) {
                                intent = new Intent(getApplicationContext(), PermissionActivity.class);
                            } else {
                                intent = new Intent(getApplicationContext(), MainActivity.class);
                            }

                            startActivity(intent);

                            timer.cancel();
                        }
                    }
                };
                timer.schedule(timerTask,0,1000);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                introEndFlag[0] = true;
            }
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        // 인트로 이미지 --

        String apiServerUrl = getString(R.string.api_server_protocol) + "://" + getString(R.string.api_server_ip) + ":" + getString(R.string.api_server_port) + getString(R.string.api_server_serverStatus);
        String statusTypeCode = "001";
        RestApiHelper restApiHelper = new RestApiHelper();

        Thread thread = new Thread() {
            public void run() {
                try {
                    JSONObject getServerStatus = restApiHelper.getServerStatus(apiServerUrl + "/" + statusTypeCode, IntroActivity.this);

                    String onBool = "";
                    if(!getServerStatus.isNull("on"))
                        onBool = getServerStatus.getString("on");


                    String resultCode = "";
                    if(!getServerStatus.isNull("resultCode"))
                        resultCode = getServerStatus.getString("resultCode");

                    if(onBool.equals("true") && resultCode.equals("200")) { // server error
                        serverCheckEndFlag[0] = true;
                    } else {
                        new module().openAlertPositiveDialog(IntroActivity.this,"알림!", "서버 점검 혹은 차단 상태입니다. \n잠시 후 다시 시도해주세요.", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        };

        thread.setDaemon(true);
        thread.start();
     }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}