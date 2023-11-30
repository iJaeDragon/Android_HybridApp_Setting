# AndroidStudio-HybridApp

## default setting

### AndroidManifext.xml

```

<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
>

    <!-- 앱 재설치 시 SharedPreferences 초기화를 위해 allowBackup, fullBackupOnly, fullBackupContent 속성을 false로 설정했습니다. -->
    <application
        android:allowBackup="false"
        android:fullBackupOnly="false"
        android:fullBackupContent="false"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:usesCleartextTraffic="true"
        android:theme="@style/Theme.MapPrj">
        <activity
            android:name=".IntroActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity android:name=".MainActivity" />
        <activity android:name=".PermissionActivity" />
    </application>

    <!-- 네트워크 접속 권한 -->
    <uses-permission android:name="android.permission.INTERNET"/> <!--WEBVIEW ERR_CACHE_MISS ERROR 대응-->

    <!--ACCESS_COARSE_LOCATION : 네트워크를 이용하여 단말기 위치 식별-->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <!--ACCESS_FINE_LOCATION : GPS와 네트워크를 이용하여 단말기 위치 식별-->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

</manifest>

```

### IntroActivity

앱이 처음 구동되었을때 열리는 페이지

lottie 라이브러리를 통해 움직이는 이미지를 시각적으로 제공하고 동시에 API Server 정상 가동여부와 접속 가능 여부를 판단한다.
이때 가동중이지 않거나, 접속이 불가능 할 경우 앱이 강제로 종료된다.

이후 최초 권한을 요청한적이 없다면 권한요청페이지, 최초 권한 요청을 한 이후라면 메인페이지로 이동

```

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

```

### PermissionActivity

권한 요청 페이지 tedpermission 라이브러리 사용

```

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

```

### RestApiHelper

```

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

```

### res/values/strings.xml

서버정보와 api 호출 경로를 관리함.

```

<resources>
    <string name="app_name">MapPrj</string>

    <string name="web_server_protocol">https</string>
    <string name="web_server_ip">192.168.35.62</string>
    <string name="web_server_port">4443</string>
    <string name="api_server_protocol">http</string>
    <string name="api_server_ip">192.168.35.62</string>
    <string name="api_server_port">8091</string>



    <!--REST API LIST-->
    <string name="api_server_serverStatus">/rest/api/v1/serverStatus</string>
</resources>

```

### webview setting
```
    
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

```

### themes setting

```
<item name="windowNoTitle">true</item><!--title bar delete!!-->
```
