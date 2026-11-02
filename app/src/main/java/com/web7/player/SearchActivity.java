package com.web7.player;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText edtSearch;
    private RecyclerView recyclerSearchResults;
    private VideoAdapter searchAdapter;
    private List<VideoItem> allVideos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        edtSearch = findViewById(R.id.edtSearch);
        recyclerSearchResults = findViewById(R.id.recyclerSearchResults);
        ImageButton btnBack = findViewById(R.id.btnSearchBack);
        ImageButton btnSubmit = findViewById(R.id.btnSearchSubmit);

        // Setup RecyclerView
        searchAdapter = new VideoAdapter(this, new ArrayList<>());
        recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearchResults.setAdapter(searchAdapter);

        searchAdapter.setOnVideoClickListener(new VideoAdapter.OnVideoClickListener() {
            @Override
            public void onVideoClick(VideoItem video, int position) {
                Intent intent = new Intent(SearchActivity.this, PlayerActivity.class);
                intent.putExtra("video", video);
                intent.putExtra("position", position);

                ArrayList<String> paths = new ArrayList<>();
                ArrayList<String> titles = new ArrayList<>();
                for (VideoItem v : allVideos) {
                    paths.add(v.getPath());
                    titles.add(v.getCleanTitle());
                }
                intent.putStringArrayListExtra("paths", paths);
                intent.putStringArrayListExtra("titles", titles);
                startActivity(intent);
            }

            @Override
            public void onMoreClick(VideoItem video, View anchor) {
                // Options menu
            }
        });

        // Load all videos for searching
        new Thread(() -> {
            allVideos = VideoScanner.getAllVideos(this);
            runOnUiThread(() -> searchAdapter.updateVideos(allVideos));
        }).start();

        // Search functionality
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(edtSearch.getText().toString());
                return true;
            }
            return false;
        });

        btnSubmit.setOnClickListener(v -> performSearch(edtSearch.getText().toString()));
        btnBack.setOnClickListener(v -> finish());

        // Auto-focus search field
        edtSearch.requestFocus();
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            searchAdapter.updateVideos(allVideos);
        } else {
            List<VideoItem> results = VideoScanner.searchVideos(this, query);
            searchAdapter.updateVideos(results);
        }
    }
}
