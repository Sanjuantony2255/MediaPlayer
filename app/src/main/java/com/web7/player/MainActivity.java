package com.web7.player;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private RecyclerView recyclerVideos;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyState;
    private LinearLayout chipContainer;
    private Button btnGrantPermission;
    private VideoAdapter videoAdapter;
    private List<VideoItem> allVideos = new ArrayList<>();
    private String selectedFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        setupBottomNav();
        setupToolbarActions();

        if (checkPermission()) {
            loadVideos();
        } else {
            requestPermission();
        }
    }

    private void initViews() {
        recyclerVideos = findViewById(R.id.recyclerVideos);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        emptyState = findViewById(R.id.emptyState);
        chipContainer = findViewById(R.id.chipContainer);
        btnGrantPermission = findViewById(R.id.btnGrantPermission);

        // Swipe to refresh
        swipeRefresh.setColorSchemeColors(
                ContextCompat.getColor(this, R.color.yt_red));
        swipeRefresh.setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(this, R.color.yt_dark_surface));
        swipeRefresh.setOnRefreshListener(this::loadVideos);

        // Grant permission button
        btnGrantPermission.setOnClickListener(v -> requestPermission());
    }

    private void setupRecyclerView() {
        videoAdapter = new VideoAdapter(this, new ArrayList<>());
        recyclerVideos.setLayoutManager(new LinearLayoutManager(this));
        recyclerVideos.setAdapter(videoAdapter);

        videoAdapter.setOnVideoClickListener(new VideoAdapter.OnVideoClickListener() {
            @Override
            public void onVideoClick(VideoItem video, int position) {
                openPlayer(video, position);
            }

            @Override
            public void onMoreClick(VideoItem video, View anchor) {
                showVideoOptions(video);
            }
        });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.nav_shorts) {
                Toast.makeText(this, "Shorts - Coming Soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_subscriptions) {
                Toast.makeText(this, "Subscriptions", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_library) {
                Toast.makeText(this, "Library", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_you) {
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void setupToolbarActions() {
        ImageButton btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        });

        ImageButton btnNotification = findViewById(R.id.btnNotification);
        btnNotification.setOnClickListener(v ->
                Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show());

        ImageButton btnCast = findViewById(R.id.btnCast);
        btnCast.setOnClickListener(v ->
                Toast.makeText(this, "Cast", Toast.LENGTH_SHORT).show());
    }

    private void loadVideos() {
        new Thread(() -> {
            allVideos = VideoScanner.getAllVideos(this);
            List<String> folders = VideoScanner.getFolderNames(allVideos);

            runOnUiThread(() -> {
                swipeRefresh.setRefreshing(false);
                setupFilterChips(folders);

                if (allVideos.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                    applyFilter();
                }
            });
        }).start();
    }

    private void setupFilterChips(List<String> folders) {
        chipContainer.removeAllViews();

        for (String folder : folders) {
            TextView chip = new TextView(this);
            chip.setText(folder);
            chip.setTextSize(14);
            chip.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
            chip.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(dpToPx(8));
            chip.setLayoutParams(params);

            updateChipStyle(chip, folder.equals(selectedFilter));

            chip.setOnClickListener(v -> {
                selectedFilter = folder;
                // Update all chips
                for (int i = 0; i < chipContainer.getChildCount(); i++) {
                    View child = chipContainer.getChildAt(i);
                    if (child instanceof TextView) {
                        String chipText = ((TextView) child).getText().toString();
                        updateChipStyle((TextView) child, chipText.equals(selectedFilter));
                    }
                }
                applyFilter();
            });

            chipContainer.addView(chip);
        }
    }

    private void updateChipStyle(TextView chip, boolean selected) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.chip_background_selected);
            chip.setTextColor(ContextCompat.getColor(this, R.color.yt_chip_selected_text));
        } else {
            chip.setBackgroundResource(R.drawable.chip_background);
            chip.setTextColor(ContextCompat.getColor(this, R.color.yt_chip_text));
        }
    }

    private void applyFilter() {
        List<VideoItem> filtered = VideoScanner.filterByFolder(allVideos, selectedFilter);
        videoAdapter.updateVideos(filtered);
        showEmptyState(filtered.isEmpty());
    }

    private void openPlayer(VideoItem video, int position) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("video", video);
        intent.putExtra("position", position);

        // Pass all video paths for next/prev functionality
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

    private void showVideoOptions(VideoItem video) {
        Toast.makeText(this, video.getCleanTitle() + " · " + video.getFormattedSize(),
                Toast.LENGTH_SHORT).show();
    }

    private void showEmptyState(boolean show) {
        if (show) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerVideos.setVisibility(View.GONE);
            if (!checkPermission()) {
                btnGrantPermission.setVisibility(View.VISIBLE);
            }
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerVideos.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_VIDEO},
                    PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadVideos();
            } else {
                showEmptyState(true);
                btnGrantPermission.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Permission required to scan videos",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission() && allVideos.isEmpty()) {
            loadVideos();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
