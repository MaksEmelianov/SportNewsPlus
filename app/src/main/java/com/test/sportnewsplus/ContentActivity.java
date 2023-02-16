package com.test.sportnewsplus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ContentActivity extends AppCompatActivity {

    TextView tvTitle, tvDesc;
    ImageView imageView;
//    WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        tvTitle = findViewById(R.id.tvTitle);
        tvDesc = findViewById(R.id.tvDesc);
        imageView = findViewById(R.id.image);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String desc = intent.getStringExtra("desc");
        String imageUrl = intent.getStringExtra("imageUrl");

        Picasso.with(ContentActivity.this).load(imageUrl).into(imageView);

        tvTitle.setText(title);
        tvDesc.setText(desc);

//        webView = findViewById(R.id.webView);
//        webView.getSettings().setDomStorageEnabled(true);
//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.getSettings().setLoadsImagesAutomatically(true);
//        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//        webView.setWebViewClient(new WebViewClient());
//        webView.loadUrl(url);
    }
}