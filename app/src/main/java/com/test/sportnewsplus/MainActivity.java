package com.test.sportnewsplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    Intent intent;
    WebView webview;
    WebSettings webSettings;
    NetworkInfo networkInfo;

    String URL;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String PREFERENCES_NAME = "MyPrefsFile";
    String keyURL = "keyURL";
    String defaultURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

//        editor = preferences.edit();
//        editor.clear();
//
//        String configURL = getURLFromGoogleService();
//        System.out.println("configURL - " + configURL);
//        startWebview(configURL, savedInstanceState);

        if (sharedPreferences.contains(keyURL)) {
            exeIfSavedURL(savedInstanceState);
        } else {
            exeIfNoSavedURL(savedInstanceState);
        }
    }

    private void exeIfSavedURL(Bundle savedInstanceState) {
        if (isThereInternet(this)) {
            URL = sharedPreferences.getString(keyURL, defaultURL);
            startWebview(URL, savedInstanceState);
        } else {
            Intent intent = new Intent(this, TurnOnInternet.class);
            startActivity(intent);
        }
    }

    private void exeIfNoSavedURL(Bundle savedInstanceState) {
        URL = getURLFromGoogleService();
        saveURL(URL);
        if (URL.equals("") || isEmulator() || isNotThereSim()) {
            startNews();
        } else {
            saveURL(URL);
            startWebview(URL, savedInstanceState);
        }
    }

    private void saveURL(String valueURL) {
        editor.putString(keyURL, valueURL);
        editor.apply();
    }

    private boolean isNotThereSim() {
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT;
    }

    private void startNews() {
        intent = new Intent(MainActivity.this, NewsActivity.class);
        startActivity(intent);
    }

    private void startWebview(String URL, Bundle savedInstanceState) {
        webview = findViewById(R.id.webview);
        setSettingWebview();
        if (!Objects.isNull(savedInstanceState)) {
            webview.restoreState(savedInstanceState);
        } else {
            webview.loadUrl(URL);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setSettingWebview() {
        webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webview.setInitialScale(1);

        webview.setWebViewClient(new MyWebViewClient());
    }

    private boolean isThereInternet(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = manager.getActiveNetworkInfo();
        return !Objects.isNull(networkInfo) && networkInfo.isConnectedOrConnecting();
    }

    private String getURLFromGoogleService() {
        String configURL = "";
        FirebaseApp.getInstance();
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder().build();
//        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
//                .setMinimumFetchIntervalInSeconds(10) // change to 3600 on published app
//                .build();
        config.setConfigSettingsAsync(settings);
        configURL = config.getString("url");
        System.out.println("configURL config.getString - " + configURL);
        config.fetchAndActivate()
                .addOnCompleteListener(MainActivity.this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(
                                MainActivity.this,
                                "Fetch and activate succeeded",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                MainActivity.this,
                                "Fetch failed",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
        return configURL;
    }

    private boolean isEmulator() {
        if (BuildConfig.DEBUG) {
            return false;
        }
        String phoneModel = Build.MODEL;
        String buildProduct = Build.PRODUCT;
        String buildHardware = Build.HARDWARE;
        String brand = Build.BRAND;
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.MANUFACTURER.contains("Genymotion")
                || phoneModel.contains("google_sdk")
                || phoneModel.toLowerCase().contains("droid4x")
                || phoneModel.contains("Emulator")
                || phoneModel.contains("Android SDK built for x86")
                || buildHardware.equals("goldfish")
                || brand.contains("google")
                || buildHardware.equals("vbox86")
                || buildProduct.equals("sdk")
                || buildProduct.equals("google_sdk")
                || buildProduct.equals("sdk_x86")
                || buildProduct.equals("vbox86p")
                || Build.BOARD.toLowerCase().contains("nox")
                || Build.BOOTLOADER.toLowerCase().contains("nox")
                || buildHardware.toLowerCase().contains("nox")
                || buildProduct.toLowerCase().contains("nox")
                || (brand.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || buildProduct.equals("google_sdk"));
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            CookieManager manager = CookieManager.getInstance();
            manager.setAcceptCookie(true);
            editor.putString(keyURL, view.getUrl());
            editor.apply();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false;
        }

    }

    @Override
    public void onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack();
        } else {
            webview.loadUrl(webview.getUrl());
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        webview = findViewById(R.id.webview);
        webview.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("LongLogTag")
    private String getURLFromGoogleService2() {
        AtomicReference<String> configURL = new AtomicReference<>("");
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(10) // change to 3600 on published app
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(MainActivity.this, task -> {
                    if (task.isSuccessful()) {
                        final String url = remoteConfig.getString("url");
                        Log.d("fetchAndActivate", url);
//                        System.out.println("url - " + url);
                        configURL.set(url);
                        Log.d("configURL", configURL.get());
//                        System.out.println("configURL.get() - " + configURL.get());
                    }
                });
        return configURL.get();
    }
}