package com.achim.mapprj;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import com.achim.common.module;

public class PermissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        findViewById(R.id.consentBtn).setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                TedPermission permission = new TedPermission(PermissionActivity.this);
                permission.setPermissionListener(permissionListener);
                // 권한을 거부했을 때 이후 나오는 다이얼로그의 '설정'버튼 표시 유무
                permission.setGotoSettingButton(true);
                permission.setDeniedMessage("필수 권한에 모두 동의하지 않으면 앱을 이용할 수 없습니다.");
                permission.setDeniedCloseButtonText(android.R.string.ok);
                // 필요 권한 세팅
                permission.setPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE
                );
                permission.check();
            }
        });
    }

    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() { // 모든 권한 획득 시 다음 액티비티 이동
            SharedPreferences preferences = getSharedPreferences("firstLoadFlag", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("mainFlag", false);
            editor.apply();

            Intent intent = new Intent(PermissionActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
            finish();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) { // 모든 권한을 허용하지 않았을때 확인 버튼을 누를경우
            new module().die();
        }
    };
}
