package com.web7.player;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Scans device storage for video files using MediaStore API
 */
public class VideoScanner {

    /**
     * Get all videos from device storage
     */
    public static List<VideoItem> getAllVideos(Context context) {
        List<VideoItem> videoList = new ArrayList<>();

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        };

        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;

        try {
            cursor = resolver.query(collection, projection, null, null, sortOrder);

            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                int displayNameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                int dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                int sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                int dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
                int dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
                int resolutionCol = cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION);
                int mimeTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
                int bucketCol = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);

                do {
                    VideoItem video = new VideoItem();
                    video.setId(cursor.getLong(idCol));
                    video.setTitle(cursor.getString(titleCol));
                    video.setDisplayName(cursor.getString(displayNameCol));
                    video.setPath(cursor.getString(dataCol));
                    video.setDuration(cursor.getLong(durationCol));
                    video.setSize(cursor.getLong(sizeCol));
                    video.setDateAdded(cursor.getLong(dateAddedCol));
                    video.setDateModified(cursor.getLong(dateModifiedCol));
                    video.setMimeType(cursor.getString(mimeTypeCol));

                    if (resolutionCol >= 0) {
                        video.setResolution(cursor.getString(resolutionCol));
                    }
                    if (bucketCol >= 0) {
                        video.setFolderName(cursor.getString(bucketCol));
                    } else {
                        // Extract folder name from path
                        String path = video.getPath();
                        if (path != null) {
                            File file = new File(path);
                            File parent = file.getParentFile();
                            if (parent != null) {
                                video.setFolderName(parent.getName());
                            }
                        }
                    }

                    // Only add if video file exists and has duration
                    if (video.getPath() != null && video.getDuration() > 0) {
                        videoList.add(video);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return videoList;
    }

    /**
     * Search videos by query
     */
    public static List<VideoItem> searchVideos(Context context, String query) {
        List<VideoItem> allVideos = getAllVideos(context);
        List<VideoItem> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (VideoItem video : allVideos) {
            if ((video.getTitle() != null && video.getTitle().toLowerCase().contains(lowerQuery)) ||
                    (video.getDisplayName() != null && video.getDisplayName().toLowerCase().contains(lowerQuery)) ||
                    (video.getFolderName() != null && video.getFolderName().toLowerCase().contains(lowerQuery))) {
                results.add(video);
            }
        }
        return results;
    }

    /**
     * Get unique folder names for filter chips
     */
    public static List<String> getFolderNames(List<VideoItem> videos) {
        List<String> folders = new ArrayList<>();
        folders.add("All");

        for (VideoItem video : videos) {
            String folder = video.getFolderName();
            if (folder != null && !folder.isEmpty() && !folders.contains(folder)) {
                folders.add(folder);
            }
        }
        return folders;
    }

    /**
     * Filter videos by folder name
     */
    public static List<VideoItem> filterByFolder(List<VideoItem> videos, String folder) {
        if (folder == null || folder.equals("All")) {
            return new ArrayList<>(videos);
        }

        List<VideoItem> filtered = new ArrayList<>();
        for (VideoItem video : videos) {
            if (folder.equals(video.getFolderName())) {
                filtered.add(video);
            }
        }
        return filtered;
    }
}
