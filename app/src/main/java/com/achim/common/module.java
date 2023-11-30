package com.achim.common;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class module extends AppCompatActivity  {

    /**
     *
     * @param title 제목
     * @param content 내용
     * @param activity 액티비티
     * @param Event 버튼클릭 시 이벤트
     */
    public void openAlertPositiveDialog(Activity activity, String title, String content, DialogInterface.OnClickListener Event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setPositiveButton("확인", Event);
        builder.create().show(); //보이기
    }

    /**
     * 앱을 강제로 재실행하는 메서드
     */
    public void appRestart() {
        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        startActivity(mainIntent);
        System.exit(0);
    }

    /**
     * @param context 사용중인 context ( new Class 를 통해 다른 클래스에서 처리를 하려고 할때 새로운 context를 사용하려고 하기 떄문에 기존 context를 불러와야 한다. )
     * @param windowService 윈도우 관리자 객체 선언을 위해 윈도우 현재 컨텍스트의 윈도우 서비스를 담아준다.
     * @param ApplicationContext 로티 애니메이션 뷰 선언
     * 로딩 뷰 불러오기
     *
     */
    public LottieAnimationView LoadingViewBegin(Context context, String windowService, Context ApplicationContext) {
        //윈도우 관리자 가져오기
        WindowManager wm = (WindowManager)context.getSystemService(windowService);
        Display dis = wm.getDefaultDisplay();
        Point pt = new Point();
        dis.getSize(pt);

        // 로티 애니메이션 뷰 선언 - 로딩 이미지
        LottieAnimationView loadingImgView = new LottieAnimationView(ApplicationContext);
        loadingImgView.setAnimation("lottie_loading.json");
        loadingImgView.playAnimation();
        loadingImgView.loop(true);

        WindowManager.LayoutParams param = new WindowManager.LayoutParams();
        param.gravity = Gravity.LEFT | Gravity.TOP;

        param.x = pt.x;
        param.y = pt.y;
        param.width = WindowManager.LayoutParams.WRAP_CONTENT;
        param.height = WindowManager.LayoutParams.WRAP_CONTENT;

        //FLAG_LAYOUT_IN_SCREEN 이므로 윈도우의 좌상단을 기준으로 한 좌표에 배치됨
        param.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //이미지가 아닌 버튼이나 스피너가 투명색을 제대로 표현하게 함.
        param.format = PixelFormat.TRANSLUCENT;

        //만든 뷰를 레이아웃에 붙인다.
        wm.addView(loadingImgView, param);

        return loadingImgView;
    }

    /**
     *
     * @param context 사용중인 context ( new Class 를 통해 다른 클래스에서 처리를 하려고 할때 새로운 context를 사용하려고 하기 떄문에 기존 context를 불러와야 한다. )
     * @param windowService 윈도우 관리자 객체 선언을 위해 윈도우 현재 컨텍스트의 윈도우 서비스를 담아준다.
     * @param loadingImgView LoadingViewBegin 메서드에서 리턴받은 로딩뷰 아이템을 담아준다.
     *
     * 로딩 뷰 없애기
     */
    public void LoadingViewEnd(Context context, String windowService, LottieAnimationView loadingImgView) {
        //윈도우 관리자 가져오기
        WindowManager wm = (WindowManager)context.getSystemService(windowService);

        wm.removeView(loadingImgView);
    }

    /**
     *
     * @param activity 상태바를 없앨 액티비티
     * 현재 액티비티의 상태바 없애기
     */
    public void StatusBarTransparent(Activity activity) {
        Window w = activity.getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    public int getStatusBarHeight(Context context) {
        int screenSizeType = (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);

        int statusbar = 0;
        if(screenSizeType != Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");

            if (resourceId > 0)
                statusbar = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusbar;
    }

    public void die() {
        System.exit(0);
    }
}
