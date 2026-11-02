package com.web7.player;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Scans device storage for all video formats across MediaStore and local file system.
 */
public class VideoScanner {

    public static final Set<String> SUPPORTED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "mp4", "mkv", "webm", "avi", "mov", "flv", "wmv", "3gp",
            "ts", "m4v", "vob", "ogv", "rmvb", "f4v", "mpg", "mpeg",
            "m2ts", "divx", "asf", "mts"
    ));

    /**
     * Get all videos from device storage supporting all video formats
     */
    public static List<VideoItem> getAllVideos(Context context) {
        List<VideoItem> videoList = new ArrayList<>();
        Set<String> seenPaths = new HashSet<>();

        // 1. Scan MediaStore Video
        scanMediaStoreVideo(context, videoList, seenPaths);

        // 2. Scan MediaStore Files for unindexed video formats (MKV, AVI, FLV, WebM, etc.)
        scanMediaStoreFiles(context, videoList, seenPaths);

        // 3. Scan common local storage folders for video files (especially useful in emulators)
        scanLocalDirectories(videoList, seenPaths);

        return videoList;
    }

    private static void scanMediaStoreVideo(Context context, List<VideoItem> videoList, Set<String> seenPaths) {
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
        try (Cursor cursor = resolver.query(collection, projection, null, null, sortOrder)) {
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
                    String path = cursor.getString(dataCol);
                    if (path == null || seenPaths.contains(path)) continue;

                    File file = new File(path);
                    if (!file.exists()) continue;

                    VideoItem video = new VideoItem();
                    video.setId(cursor.getLong(idCol));
                    video.setTitle(cursor.getString(titleCol));
                    video.setDisplayName(cursor.getString(displayNameCol));
                    video.setPath(path);
                    video.setSize(cursor.getLong(sizeCol));
                    video.setDateAdded(cursor.getLong(dateAddedCol));
                    video.setDateModified(cursor.getLong(dateModifiedCol));
                    video.setMimeType(cursor.getString(mimeTypeCol));

                    long duration = cursor.getLong(durationCol);
                    if (duration <= 0) {
                        duration = extractDurationFromFile(path);
                    }
                    video.setDuration(duration);

                    if (resolutionCol >= 0) {
                        video.setResolution(cursor.getString(resolutionCol));
                    }
                    if (bucketCol >= 0 && cursor.getString(bucketCol) != null) {
                        video.setFolderName(cursor.getString(bucketCol));
                    } else if (file.getParentFile() != null) {
                        video.setFolderName(file.getParentFile().getName());
                    }

                    seenPaths.add(path);
                    videoList.add(video);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void scanMediaStoreFiles(Context context, List<VideoItem> videoList, Set<String> seenPaths) {
        try {
            Uri filesUri = MediaStore.Files.getContentUri("external");
            String[] projection = {
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_MODIFIED,
                    MediaStore.Files.FileColumns.MIME_TYPE
            };

            // Look for video mime types or files
            String selection = MediaStore.Files.FileColumns.MIME_TYPE + " LIKE 'video/%'";
            try (Cursor cursor = context.getContentResolver().query(filesUri, projection, selection, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                    int dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                    int nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                    int sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                    int dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);
                    int mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);

                    do {
                        String path = cursor.getString(dataCol);
                        if (path == null || seenPaths.contains(path)) continue;

                        File file = new File(path);
                        if (!file.exists() || !isVideoFile(file)) continue;

                        VideoItem video = createVideoItemFromFile(file, cursor.getLong(idCol));
                        video.setDateModified(cursor.getLong(dateCol));
                        video.setSize(cursor.getLong(sizeCol));
                        video.setMimeType(cursor.getString(mimeCol));

                        seenPaths.add(path);
                        videoList.add(video);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void scanLocalDirectories(List<VideoItem> videoList, Set<String> seenPaths) {
        try {
            File[] commonDirs = {
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    Environment.getExternalStorageDirectory(),
                    new File("/storage/emulated/0/Download"),
                    new File("/storage/emulated/0/Movies"),
                    new File("/storage/emulated/0/DCIM/Camera")
            };

            for (File dir : commonDirs) {
                if (dir != null && dir.exists() && dir.isDirectory()) {
                    scanDirectoryRecursive(dir, videoList, seenPaths, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void scanDirectoryRecursive(File dir, List<VideoItem> videoList, Set<String> seenPaths, int depth) {
        if (depth > 4 || dir == null) return;
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory() && !file.getName().startsWith(".")) {
                scanDirectoryRecursive(file, videoList, seenPaths, depth + 1);
            } else if (file.isFile() && isVideoFile(file)) {
                String path = file.getAbsolutePath();
                if (!seenPaths.contains(path)) {
                    VideoItem item = createVideoItemFromFile(file, System.currentTimeMillis() + videoList.size());
                    seenPaths.add(path);
                    videoList.add(item);
                }
            }
        }
    }

    public static boolean isVideoFile(File file) {
        if (file == null || !file.exists()) return false;
        String name = file.getName().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        if (dot >= 0 && dot < name.length() - 1) {
            String ext = name.substring(dot + 1);
            return SUPPORTED_EXTENSIONS.contains(ext);
        }
        return false;
    }

    public static VideoItem createVideoItemFromFile(File file, long id) {
        VideoItem video = new VideoItem();
        video.setId(id);
        video.setPath(file.getAbsolutePath());
        video.setDisplayName(file.getName());
        video.setTitle(file.getName());
        video.setSize(file.length());
        video.setDateModified(file.lastModified() / 1000);
        if (file.getParentFile() != null) {
            video.setFolderName(file.getParentFile().getName());
        } else {
            video.setFolderName("Local Videos");
        }

        long duration = extractDurationFromFile(file.getAbsolutePath());
        video.setDuration(duration);
        return video;
    }

    public static long extractDurationFromFile(String path) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            String durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
            if (durStr != null) {
                return Long.parseLong(durStr);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    /**
     * Search videos by query
     */
    public static List<VideoItem> searchVideos(Context context, String query) {
        List<VideoItem> allVideos = getAllVideos(context);
        List<VideoItem> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase(Locale.ROOT);

        for (VideoItem video : allVideos) {
            if ((video.getTitle() != null && video.getTitle().toLowerCase(Locale.ROOT).contains(lowerQuery)) ||
                    (video.getDisplayName() != null && video.getDisplayName().toLowerCase(Locale.ROOT).contains(lowerQuery)) ||
                    (video.getFolderName() != null && video.getFolderName().toLowerCase(Locale.ROOT).contains(lowerQuery))) {
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
