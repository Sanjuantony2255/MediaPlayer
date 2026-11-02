package com.web7.player;

import android.net.Uri;
import java.io.Serializable;

public class VideoItem implements Serializable {
    private long id;
    private String title;
    private String displayName;
    private String path;
    private long duration;
    private long size;
    private long dateAdded;
    private long dateModified;
    private String resolution;
    private String mimeType;
    private String folderName;
    private int likeCount;
    private boolean isLiked;
    private boolean isDisliked;

    public VideoItem() {
        this.likeCount = (int)(Math.random() * 1000) + 10;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public long getDateAdded() { return dateAdded; }
    public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }

    public long getDateModified() { return dateModified; }
    public void setDateModified(long dateModified) { this.dateModified = dateModified; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }

    public boolean isDisliked() { return isDisliked; }
    public void setDisliked(boolean disliked) { isDisliked = disliked; }

    public Uri getUri() {
        return Uri.parse(path);
    }

    /**
     * Format duration from ms to readable string like YouTube
     */
    public String getFormattedDuration() {
        long seconds = duration / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%d:%02d", minutes, secs);
        }
    }

    /**
     * Format file size to readable string
     */
    public String getFormattedSize() {
        double sizeKB = size / 1024.0;
        double sizeMB = sizeKB / 1024.0;
        double sizeGB = sizeMB / 1024.0;

        if (sizeGB >= 1) {
            return String.format("%.1f GB", sizeGB);
        } else if (sizeMB >= 1) {
            return String.format("%.1f MB", sizeMB);
        } else {
            return String.format("%.0f KB", sizeKB);
        }
    }

    /**
     * Get a clean title without extension
     */
    public String getCleanTitle() {
        if (title != null && !title.isEmpty()) {
            int lastDot = title.lastIndexOf('.');
            if (lastDot > 0) {
                return title.substring(0, lastDot);
            }
            return title;
        }
        return displayName != null ? displayName : "Unknown";
    }

    /**
     * Generate pseudo view count for YouTube-like display
     */
    public String getViewCount() {
        // Create consistent but pseudo-random view count based on file size
        long views = (size / 1024) + (dateAdded % 10000);
        if (views > 1000000) {
            return String.format("%.1fM views", views / 1000000.0);
        } else if (views > 1000) {
            return String.format("%.1fK views", views / 1000.0);
        }
        return views + " views";
    }

    /**
     * Get time ago string like YouTube
     */
    public String getTimeAgo() {
        long now = System.currentTimeMillis() / 1000;
        long diff = now - dateAdded;

        if (diff < 60) return "Just now";
        if (diff < 3600) return (diff / 60) + " minutes ago";
        if (diff < 86400) return (diff / 3600) + " hours ago";
        if (diff < 604800) return (diff / 86400) + " days ago";
        if (diff < 2592000) return (diff / 604800) + " weeks ago";
        if (diff < 31536000) return (diff / 2592000) + " months ago";
        return (diff / 31536000) + " years ago";
    }
}
