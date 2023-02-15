package com.test.sportnewsplus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.widget.Toast;

import com.test.sportnewsplus.model.Articles;
import com.test.sportnewsplus.model.Headlines;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsActivity extends AppCompatActivity {
    private final String API_KEY = "8bad61c92479421e82b258a009106019";
    RecyclerView recyclerView;
    Adapter adapter;
    List<Articles> articles = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        retrieveJson();

        swipeRefreshLayout.setOnRefreshListener(this::retrieveJson);
    }

    public void retrieveJson() {
        swipeRefreshLayout.setRefreshing(true);
        Call<Headlines> call = ApiClient
                .getInstance()
                .getApi()
                .getHeadlines(
                        getCountry(),
                        "sports",
                        API_KEY);
        call.enqueue(new Callback<Headlines>() {
            @Override
            public void onResponse(Call<Headlines> call, Response<Headlines> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful()) {
                    Headlines headlines = response.body();
                    if (headlines != null) {
                        articles.clear();
                        articles = headlines.getArticles();
                        adapter = new Adapter(NewsActivity.this, articles);
                        recyclerView.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<Headlines> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(NewsActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getCountry() {
        return Locale.getDefault().getCountry().toLowerCase(Locale.ROOT);
    }
}