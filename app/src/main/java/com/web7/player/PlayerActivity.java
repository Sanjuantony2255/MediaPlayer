package com.web7.player;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Rational;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity {

    private VideoView videoView;
    private FrameLayout playerContainer;
    private RelativeLayout controlsOverlay;
    private ScrollView scrollContent;
    private ProgressBar miniProgress;

    // Controls
    private ImageButton btnPlayPause, btnBack, btnFullscreen, btnPip;
    private ImageButton btnRewind, btnForward, btnPrev, btnNext;
    private ImageButton btnSettings, btnAutoplay;
    private SeekBar seekBar;
    private TextView txtCurrentTime, txtTotalTime, txtPlayerTitle;

    // Info section
    private TextView txtVideoTitle, txtVideoInfo, txtChannelName, txtSubscribers;
    private TextView txtDescription, txtShowMore, txtCommentCount;
    private TextView txtLikeCount;
    private LinearLayout btnLike, btnShare, btnDownload, btnSave;
    private LinearLayout descriptionSection;
    private Button btnSubscribe;
    private EditText edtComment;

    // Related videos
    private RecyclerView recyclerRelated, recyclerComments;
    private RelatedVideoAdapter relatedAdapter;
    private CommentAdapter commentAdapter;

    // State
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isFullscreen = false;
    private boolean controlsVisible = false;
    private boolean isDescriptionExpanded = false;
    private boolean isLiked = false;
    private boolean isSubscribed = false;
    private int likeCount;
    private VideoItem currentVideo;
    private ArrayList<String> videoPaths;
    private ArrayList<String> videoTitles;
    private int currentPosition;

    // Gestures
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initViews();
        setupGestures();
        setupControls();
        setupActionButtons();
        setupComments();

        // Get video data from intent
        currentVideo = (VideoItem) getIntent().getSerializableExtra("video");
        currentPosition = getIntent().getIntExtra("position", 0);
        videoPaths = getIntent().getStringArrayListExtra("paths");
        videoTitles = getIntent().getStringArrayListExtra("titles");

        if (currentVideo != null) {
            loadVideo(currentVideo);
        }
    }

    private void initViews() {
        videoView = findViewById(R.id.videoView);
        playerContainer = findViewById(R.id.playerContainer);
        controlsOverlay = findViewById(R.id.controlsOverlay);
        scrollContent = findViewById(R.id.scrollContent);
        miniProgress = findViewById(R.id.miniProgress);

        // Controls
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnBack = findViewById(R.id.btnBack);
        btnFullscreen = findViewById(R.id.btnFullscreen);
        btnPip = findViewById(R.id.btnPip);
        btnRewind = findViewById(R.id.btnRewind);
        btnForward = findViewById(R.id.btnForward);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnSettings = findViewById(R.id.btnSettings);
        btnAutoplay = findViewById(R.id.btnAutoplay);
        seekBar = findViewById(R.id.seekBar);
        txtCurrentTime = findViewById(R.id.txtCurrentTime);
        txtTotalTime = findViewById(R.id.txtTotalTime);
        txtPlayerTitle = findViewById(R.id.txtPlayerTitle);

        // Info
        txtVideoTitle = findViewById(R.id.txtVideoTitle);
        txtVideoInfo = findViewById(R.id.txtVideoInfo);
        txtChannelName = findViewById(R.id.txtChannelName);
        txtSubscribers = findViewById(R.id.txtSubscribers);
        txtDescription = findViewById(R.id.txtDescription);
        txtShowMore = findViewById(R.id.txtShowMore);
        txtCommentCount = findViewById(R.id.txtCommentCount);
        txtLikeCount = findViewById(R.id.txtLikeCount);
        btnLike = findViewById(R.id.btnLike);
        btnShare = findViewById(R.id.btnShare);
        btnDownload = findViewById(R.id.btnDownload);
        btnSave = findViewById(R.id.btnSave);
        btnSubscribe = findViewById(R.id.btnSubscribe);
        descriptionSection = findViewById(R.id.descriptionSection);
        edtComment = findViewById(R.id.edtComment);

        // RecyclerViews
        recyclerRelated = findViewById(R.id.recyclerRelated);
        recyclerComments = findViewById(R.id.recyclerComments);
    }

    private void setupGestures() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                toggleControls();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                float x = e.getX();
                float width = playerContainer.getWidth();

                if (x < width / 3) {
                    // Double tap left - rewind 10s
                    seekTo(videoView.getCurrentPosition() - 10000);
                    showQuickSeekFeedback("−10s");
                } else if (x > width * 2 / 3) {
                    // Double tap right - forward 10s
                    seekTo(videoView.getCurrentPosition() + 10000);
                    showQuickSeekFeedback("+10s");
                } else {
                    // Double tap center - toggle play/pause
                    togglePlayPause();
                }
                return true;
            }
        });

        playerContainer.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void setupControls() {
        btnPlayPause.setOnClickListener(v -> togglePlayPause());

        btnBack.setOnClickListener(v -> {
            if (isFullscreen) {
                exitFullscreen();
            } else {
                finish();
            }
        });

        btnFullscreen.setOnClickListener(v -> {
            if (isFullscreen) {
                exitFullscreen();
            } else {
                enterFullscreen();
            }
        });

        btnPip.setOnClickListener(v -> enterPiP());

        btnRewind.setOnClickListener(v -> {
            seekTo(videoView.getCurrentPosition() - 10000);
        });

        btnForward.setOnClickListener(v -> {
            seekTo(videoView.getCurrentPosition() + 10000);
        });

        btnPrev.setOnClickListener(v -> playPrevious());
        btnNext.setOnClickListener(v -> playNext());

        btnSettings.setOnClickListener(v ->
                Toast.makeText(this, "Playback Settings", Toast.LENGTH_SHORT).show());

        btnAutoplay.setOnClickListener(v ->
                Toast.makeText(this, "Autoplay toggled", Toast.LENGTH_SHORT).show());

        // SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && videoView.getDuration() > 0) {
                    int newPos = (int) ((long) progress * videoView.getDuration() / 1000);
                    txtCurrentTime.setText(formatTime(newPos));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(hideControlsRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int newPos = (int) ((long) seekBar.getProgress() * videoView.getDuration() / 1000);
                videoView.seekTo(newPos);
                scheduleHideControls();
            }
        });

        // VideoView listeners
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            txtTotalTime.setText(formatTime(videoView.getDuration()));
            seekBar.setMax(1000);
            startProgressUpdater();
        });

        videoView.setOnCompletionListener(mp -> {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            // Auto-play next
            playNext();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, "Error playing video", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void setupActionButtons() {
        // Like
        btnLike.setOnClickListener(v -> {
            isLiked = !isLiked;
            if (isLiked) {
                likeCount++;
                txtLikeCount.setText(formatCount(likeCount));
                Toast.makeText(this, "Liked!", Toast.LENGTH_SHORT).show();
            } else {
                likeCount--;
                txtLikeCount.setText(formatCount(likeCount));
            }
        });

        // Share
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("video/*");
            if (currentVideo != null) {
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(currentVideo.getPath()));
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this video: " + currentVideo.getCleanTitle());
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        // Download
        btnDownload.setOnClickListener(v ->
                Toast.makeText(this, "Video is already saved locally", Toast.LENGTH_SHORT).show());

        // Save
        btnSave.setOnClickListener(v ->
                Toast.makeText(this, "Saved to Watch Later", Toast.LENGTH_SHORT).show());

        // Subscribe
        btnSubscribe.setOnClickListener(v -> {
            isSubscribed = !isSubscribed;
            if (isSubscribed) {
                btnSubscribe.setText("Subscribed");
                btnSubscribe.setBackgroundResource(R.drawable.action_button_bg);
                Toast.makeText(this, "Subscribed!", Toast.LENGTH_SHORT).show();
            } else {
                btnSubscribe.setText("Subscribe");
                btnSubscribe.setBackgroundResource(R.drawable.subscribe_button_bg);
            }
        });

        // Description expand/collapse
        descriptionSection.setOnClickListener(v -> toggleDescription());
        txtShowMore.setOnClickListener(v -> toggleDescription());
    }

    private void setupComments() {
        // Setup comments recycler
        List<CommentAdapter.Comment> sampleComments = new ArrayList<>();
        sampleComments.add(new CommentAdapter.Comment("user123", "Great video! Really enjoyed watching this.", "2 hours ago", 45));
        sampleComments.add(new CommentAdapter.Comment("techfan", "Amazing quality! What camera did you use?", "1 day ago", 23));
        sampleComments.add(new CommentAdapter.Comment("moviebuff", "This is exactly what I was looking for 🔥", "3 days ago", 12));
        sampleComments.add(new CommentAdapter.Comment("viewer_01", "Thanks for sharing!", "1 week ago", 8));

        commentAdapter = new CommentAdapter(sampleComments);
        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerComments.setAdapter(commentAdapter);

        txtCommentCount.setText(sampleComments.size() + "");

        // Comment input
        edtComment.setOnEditorActionListener((v, actionId, event) -> {
            String comment = edtComment.getText().toString().trim();
            if (!comment.isEmpty()) {
                commentAdapter.addComment(new CommentAdapter.Comment(
                        "You", comment, "Just now", 0));
                edtComment.setText("");
                txtCommentCount.setText(String.valueOf(commentAdapter.getItemCount()));
                Toast.makeText(this, "Comment added!", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    private void loadVideo(VideoItem video) {
        currentVideo = video;

        // Set video source
        try {
            videoView.setVideoPath(video.getPath());
            videoView.start();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot play this video", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update UI
        txtPlayerTitle.setText(video.getCleanTitle());
        txtVideoTitle.setText(video.getCleanTitle());

        String info = video.getViewCount() + " · " + video.getTimeAgo();
        txtVideoInfo.setText(info);

        // Channel name from folder
        String channel = video.getFolderName() != null ? video.getFolderName() : "Local Videos";
        txtChannelName.setText(channel);
        txtSubscribers.setText(formatCount(new Random().nextInt(50000) + 100) + " subscribers");

        // Like count
        likeCount = video.getLikeCount();
        txtLikeCount.setText(formatCount(likeCount));

        // Description
        String desc = "📁 File: " + video.getDisplayName() + "\n" +
                "📐 Resolution: " + (video.getResolution() != null ? video.getResolution() : "Unknown") + "\n" +
                "📦 Size: " + video.getFormattedSize() + "\n" +
                "⏱ Duration: " + video.getFormattedDuration() + "\n" +
                "📂 Location: " + video.getPath() + "\n\n" +
                "Played with Web7 - Your local video player with YouTube experience.";
        txtDescription.setText(desc);

        // Load related videos
        loadRelatedVideos();
    }

    private void loadRelatedVideos() {
        new Thread(() -> {
            List<VideoItem> allVideos = VideoScanner.getAllVideos(this);
            // Remove current video and shuffle for "related"
            List<VideoItem> related = new ArrayList<>();
            for (VideoItem v : allVideos) {
                if (v.getId() != currentVideo.getId()) {
                    related.add(v);
                }
                if (related.size() >= 10) break;
            }

            runOnUiThread(() -> {
                relatedAdapter = new RelatedVideoAdapter(this, related);
                recyclerRelated.setLayoutManager(new LinearLayoutManager(this));
                recyclerRelated.setAdapter(relatedAdapter);

                relatedAdapter.setOnRelatedVideoClickListener(video -> {
                    // Find position
                    if (videoPaths != null) {
                        int pos = videoPaths.indexOf(video.getPath());
                        if (pos >= 0) currentPosition = pos;
                    }
                    loadVideo(video);
                    scrollContent.scrollTo(0, 0);
                });
            });
        }).start();
    }

    // ===== Playback Controls =====

    private void togglePlayPause() {
        if (videoView.isPlaying()) {
            videoView.pause();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        } else {
            videoView.start();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private void seekTo(int posMs) {
        int clamped = Math.max(0, Math.min(posMs, videoView.getDuration()));
        videoView.seekTo(clamped);
    }

    private void playNext() {
        if (videoPaths != null && currentPosition < videoPaths.size() - 1) {
            currentPosition++;
            VideoItem nextVideo = findVideoByPath(videoPaths.get(currentPosition));
            if (nextVideo != null) {
                loadVideo(nextVideo);
                scrollContent.scrollTo(0, 0);
            }
        }
    }

    private void playPrevious() {
        if (videoPaths != null && currentPosition > 0) {
            currentPosition--;
            VideoItem prevVideo = findVideoByPath(videoPaths.get(currentPosition));
            if (prevVideo != null) {
                loadVideo(prevVideo);
                scrollContent.scrollTo(0, 0);
            }
        }
    }

    private VideoItem findVideoByPath(String path) {
        List<VideoItem> all = VideoScanner.getAllVideos(this);
        for (VideoItem v : all) {
            if (path.equals(v.getPath())) return v;
        }
        return null;
    }

    // ===== Controls Visibility =====

    private void toggleControls() {
        if (controlsVisible) {
            hideControls();
        } else {
            showControls();
        }
    }

    private void showControls() {
        controlsOverlay.setVisibility(View.VISIBLE);
        controlsOverlay.setAlpha(0f);
        controlsOverlay.animate().alpha(1f).setDuration(200).start();
        miniProgress.setVisibility(View.GONE);
        controlsVisible = true;
        scheduleHideControls();
    }

    private void hideControls() {
        controlsOverlay.animate().alpha(0f).setDuration(200).withEndAction(() -> {
            controlsOverlay.setVisibility(View.GONE);
            miniProgress.setVisibility(View.VISIBLE);
        }).start();
        controlsVisible = false;
    }

    private final Runnable hideControlsRunnable = this::hideControls;

    private void scheduleHideControls() {
        handler.removeCallbacks(hideControlsRunnable);
        handler.postDelayed(hideControlsRunnable, 4000);
    }

    // ===== Progress Updater =====

    private void startProgressUpdater() {
        handler.post(progressRunnable);
    }

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (videoView != null && videoView.isPlaying()) {
                int current = videoView.getCurrentPosition();
                int total = videoView.getDuration();

                if (total > 0) {
                    int progress = (int) ((long) current * 1000 / total);
                    seekBar.setProgress(progress);
                    miniProgress.setProgress((int) ((long) current * 100 / total));
                    txtCurrentTime.setText(formatTime(current));
                }
            }
            handler.postDelayed(this, 500);
        }
    };

    // ===== Fullscreen / Landscape =====

    private void enterFullscreen() {
        isFullscreen = true;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        // Hide system bars
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        // Make player fullscreen
        ViewGroup.LayoutParams params = playerContainer.getLayoutParams();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        playerContainer.setLayoutParams(params);

        // Hide content below
        scrollContent.setVisibility(View.GONE);
    }

    private void exitFullscreen() {
        isFullscreen = false;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Show system bars
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        // Restore player size
        ViewGroup.LayoutParams params = playerContainer.getLayoutParams();
        params.height = dpToPx(240);
        playerContainer.setLayoutParams(params);

        // Show content
        scrollContent.setVisibility(View.VISIBLE);
    }

    // ===== Picture-in-Picture =====

    private void enterPiP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder();
            pipBuilder.setAspectRatio(new Rational(16, 9));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pipBuilder.setAutoEnterEnabled(true);
                pipBuilder.setSeamlessResizeEnabled(true);
            }

            enterPictureInPictureMode(pipBuilder.build());
        } else {
            Toast.makeText(this, "PiP not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPiPMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPiPMode, newConfig);
        if (isInPiPMode) {
            // Hide all UI except video
            controlsOverlay.setVisibility(View.GONE);
            scrollContent.setVisibility(View.GONE);
            miniProgress.setVisibility(View.GONE);

            // Make player fill entire window
            ViewGroup.LayoutParams params = playerContainer.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            playerContainer.setLayoutParams(params);
        } else {
            // Restore UI
            scrollContent.setVisibility(View.VISIBLE);
            miniProgress.setVisibility(View.VISIBLE);

            ViewGroup.LayoutParams params = playerContainer.getLayoutParams();
            params.height = dpToPx(240);
            playerContainer.setLayoutParams(params);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // Auto-enter PiP when user presses home while video playing
        if (videoView != null && videoView.isPlaying()) {
            enterPiP();
        }
    }

    // ===== Description Toggle =====

    private void toggleDescription() {
        isDescriptionExpanded = !isDescriptionExpanded;
        if (isDescriptionExpanded) {
            txtDescription.setMaxLines(Integer.MAX_VALUE);
            txtShowMore.setText("Show less");
        } else {
            txtDescription.setMaxLines(3);
            txtShowMore.setText("Show more");
        }
    }

    // ===== Configuration Change =====

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            enterFullscreen();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            exitFullscreen();
        }
    }

    // ===== Helpers =====

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        }
        return String.format("%d:%02d", minutes, secs);
    }

    private String formatCount(int count) {
        if (count >= 1000000) {
            return String.format("%.1fM", count / 1000000.0);
        } else if (count >= 1000) {
            return String.format("%.1fK", count / 1000.0);
        }
        return String.valueOf(count);
    }

    private void showQuickSeekFeedback(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if (isFullscreen) {
            exitFullscreen();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Don't pause in PiP mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode()) {
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}
