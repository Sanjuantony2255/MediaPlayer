package com.web7.player;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaMetadataRetriever;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import androidx.appcompat.app.AlertDialog;
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
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
    private FrameLayout btnPlaybackSpeed;
    private TextView txtPlaybackSpeedBadge;
    private SeekBar seekBar;
    private TextView txtCurrentTime, txtTotalTime, txtPlayerTitle;

    // Info section
    private TextView txtVideoTitle, txtVideoInfo, txtChannelName, txtSubscribers;
    private TextView txtDescription, txtShowMore, txtCommentCount;
    private TextView txtLikeCount;
    private LinearLayout btnLike, btnShare, btnDownload, btnSave, btnPlaybackSpeedAction;
    private TextView txtSpeedActionText;
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

    // Playback Speed Options
    private static final float[] PLAYBACK_SPEEDS = {1.0f, 1.25f, 1.5f, 1.7f, 2.0f, 2.25f, 2.5f, 3.0f};
    private static final String[] PLAYBACK_SPEED_LABELS = {
            "1x (Normal)",
            "1.25x",
            "1.5x",
            "1.7x",
            "2x",
            "2.25x",
            "2.5x",
            "3x"
    };
    private float currentPlaybackSpeed = 1.0f;
    private MediaPlayer activeMediaPlayer;

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
        } else if (getIntent().getData() != null) {
            loadVideoFromUri(getIntent().getData());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        currentVideo = (VideoItem) intent.getSerializableExtra("video");
        if (currentVideo != null) {
            loadVideo(currentVideo);
        } else if (intent.getData() != null) {
            loadVideoFromUri(intent.getData());
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
        btnPlaybackSpeed = findViewById(R.id.btnPlaybackSpeed);
        txtPlaybackSpeedBadge = findViewById(R.id.txtPlaybackSpeedBadge);
        btnPlaybackSpeedAction = findViewById(R.id.btnPlaybackSpeedAction);
        txtSpeedActionText = findViewById(R.id.txtSpeedActionText);
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

        btnSettings.setOnClickListener(v -> showPlaybackSpeedDialog());
        if (btnPlaybackSpeed != null) {
            btnPlaybackSpeed.setOnClickListener(v -> showPlaybackSpeedDialog());
        }
        if (btnPlaybackSpeedAction != null) {
            btnPlaybackSpeedAction.setOnClickListener(v -> showPlaybackSpeedDialog());
        }

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
            activeMediaPlayer = mp;
            mp.setLooping(false);
            if (currentPlaybackSpeed != 1.0f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    PlaybackParams params = mp.getPlaybackParams();
                    if (params == null) params = new PlaybackParams();
                    params.setSpeed(currentPlaybackSpeed);
                    mp.setPlaybackParams(params);
                } catch (Exception ignored) {}
            }
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

        // Set video source supporting all paths & URIs
        try {
            String path = video.getPath();
            if (path != null && (path.startsWith("content://") || path.startsWith("android.resource://") || path.startsWith("file://"))) {
                videoView.setVideoURI(Uri.parse(path));
            } else if (path != null) {
                videoView.setVideoPath(path);
            }
            videoView.start();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot play this video format", Toast.LENGTH_SHORT).show();
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
                "Played with emulator ad - Your local video player with YouTube experience.";
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

    // ===== Playback Speed Controls =====

    private void showPlaybackSpeedDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialog);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_playback_speed, null);
        dialog.setContentView(sheetView);

        // Ensure transparent window/background around rounded corners
        if (dialog.getWindow() != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        }

        TextView txtSpeedValue = sheetView.findViewById(R.id.txtSpeedValue);
        FrameLayout btnSpeedMinus = sheetView.findViewById(R.id.btnSpeedMinus);
        FrameLayout btnSpeedPlus = sheetView.findViewById(R.id.btnSpeedPlus);
        SeekBar seekSpeed = sheetView.findViewById(R.id.seekSpeed);

        final float[] presetSpeeds = {0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 3.0f};
        final TextView[] chipViews = {
                sheetView.findViewById(R.id.chip_0_25),
                sheetView.findViewById(R.id.chip_0_5),
                sheetView.findViewById(R.id.chip_0_75),
                sheetView.findViewById(R.id.chip_1_0),
                sheetView.findViewById(R.id.chip_1_25),
                sheetView.findViewById(R.id.chip_1_5),
                sheetView.findViewById(R.id.chip_1_75),
                sheetView.findViewById(R.id.chip_2_0),
                sheetView.findViewById(R.id.chip_3_0)
        };

        // Range: 0.25x to 3.00x in 0.05x steps -> 55 steps
        final int MAX_STEPS = 55;
        seekSpeed.setMax(MAX_STEPS);

        int initialProgress = Math.round((currentPlaybackSpeed - 0.25f) / 0.05f);
        if (initialProgress < 0) initialProgress = 0;
        if (initialProgress > MAX_STEPS) initialProgress = MAX_STEPS;
        seekSpeed.setProgress(initialProgress);

        updatePlaybackSpeedSheetUI(currentPlaybackSpeed, txtSpeedValue, chipViews, presetSpeeds);

        seekSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float speed = 0.25f + progress * 0.05f;
                speed = Math.round(speed * 100.0f) / 100.0f;
                updatePlaybackSpeedSheetUI(speed, txtSpeedValue, chipViews, presetSpeeds);
                setPlaybackSpeedInternal(speed, false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSpeedMinus.setOnClickListener(v -> {
            int currentProg = seekSpeed.getProgress();
            if (currentProg > 0) {
                seekSpeed.setProgress(currentProg - 1);
            }
        });

        btnSpeedPlus.setOnClickListener(v -> {
            int currentProg = seekSpeed.getProgress();
            if (currentProg < MAX_STEPS) {
                seekSpeed.setProgress(currentProg + 1);
            }
        });

        for (int i = 0; i < presetSpeeds.length; i++) {
            final int index = i;
            if (chipViews[i] != null) {
                chipViews[i].setOnClickListener(v -> {
                    float speed = presetSpeeds[index];
                    int prog = Math.round((speed - 0.25f) / 0.05f);
                    seekSpeed.setProgress(prog);
                });
            }
        }

        // Auto-scroll to selected chip if needed
        HorizontalScrollView scrollChips = sheetView.findViewById(R.id.scrollChips);
        if (scrollChips != null && initialProgress >= 15) {
            scrollChips.post(() -> {
                View selectedContainer = sheetView.findViewById(R.id.container_1_0);
                if (currentPlaybackSpeed >= 2.0f) {
                    selectedContainer = sheetView.findViewById(R.id.container_2_0);
                } else if (currentPlaybackSpeed >= 1.5f) {
                    selectedContainer = sheetView.findViewById(R.id.container_1_5);
                } else if (currentPlaybackSpeed >= 1.25f) {
                    selectedContainer = sheetView.findViewById(R.id.container_1_25);
                }
                if (selectedContainer != null) {
                    int scrollX = selectedContainer.getLeft() - (scrollChips.getWidth() / 4);
                    scrollChips.smoothScrollTo(Math.max(0, scrollX), 0);
                }
            });
        }

        dialog.show();
    }

    private void updatePlaybackSpeedSheetUI(float speed, TextView txtSpeedValue, TextView[] chipViews, float[] presetSpeeds) {
        if (txtSpeedValue != null) {
            txtSpeedValue.setText(String.format(java.util.Locale.US, "%.2fx", speed));
        }

        for (int i = 0; i < presetSpeeds.length; i++) {
            if (chipViews[i] != null) {
                boolean isSelected = Math.abs(presetSpeeds[i] - speed) < 0.025f;
                if (isSelected) {
                    chipViews[i].setBackgroundResource(R.drawable.speed_chip_bg_selected);
                    chipViews[i].setTextColor(0xFFFFFFFF);
                } else {
                    chipViews[i].setBackgroundResource(R.drawable.speed_chip_bg_normal);
                    chipViews[i].setTextColor(0xFF0F0F0F);
                }
            }
        }
    }

    public void setPlaybackSpeed(float speed) {
        setPlaybackSpeedInternal(speed, true);
    }

    public void setPlaybackSpeedInternal(float speed, boolean showToast) {
        this.currentPlaybackSpeed = speed;
        updateSpeedBadge(speed);
        if (activeMediaPlayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                boolean wasPlaying = videoView.isPlaying();
                PlaybackParams params = activeMediaPlayer.getPlaybackParams();
                if (params == null) {
                    params = new PlaybackParams();
                }
                params.setSpeed(speed);
                activeMediaPlayer.setPlaybackParams(params);
                if (!wasPlaying) {
                    videoView.pause();
                }
                if (showToast) {
                    Toast.makeText(this, "Speed set to " + speed + "x", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (showToast) {
                    Toast.makeText(this, "Playback speed " + speed + "x not supported on this video codec", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (showToast) {
            Toast.makeText(this, "Speed set to " + speed + "x", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSpeedBadge(float speed) {
        String badgeText;
        if (Math.abs(speed - 1.0f) < 0.02f) {
            badgeText = "P";
        } else {
            badgeText = (speed == (int) speed) ? String.format(java.util.Locale.US, "%dx", (int) speed) : String.format(java.util.Locale.US, "%.2fx", speed);
        }
        if (txtPlaybackSpeedBadge != null) {
            txtPlaybackSpeedBadge.setText(badgeText);
        }
        if (txtSpeedActionText != null) {
            txtSpeedActionText.setText(Math.abs(speed - 1.0f) < 0.02f ? "Speed" : badgeText);
        }
    }

    private void loadVideoFromUri(Uri uri) {
        if (uri == null) return;
        try {
            VideoItem video = new VideoItem();
            String path = uri.getPath();
            String title = "Video";

            if ("content".equalsIgnoreCase(uri.getScheme())) {
                try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (nameIdx >= 0) {
                            title = cursor.getString(nameIdx);
                        }
                        int sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE);
                        if (sizeIdx >= 0) {
                            video.setSize(cursor.getLong(sizeIdx));
                        }
                    }
                } catch (Exception ignored) {}
            } else if (path != null) {
                File f = new File(path);
                title = f.getName();
                video.setSize(f.length());
            }

            video.setTitle(title);
            video.setDisplayName(title);
            video.setPath(uri.toString());

            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(this, uri);
                String dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                if (dur != null) video.setDuration(Long.parseLong(dur));
                String w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                String h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                if (w != null && h != null) video.setResolution(w + "x" + h);
                retriever.release();
            } catch (Exception ignored) {}

            loadVideo(video);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
