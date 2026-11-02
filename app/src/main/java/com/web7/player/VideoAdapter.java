package com.web7.player;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<VideoItem> videos;
    private final Context context;
    private OnVideoClickListener listener;

    public interface OnVideoClickListener {
        void onVideoClick(VideoItem video, int position);
        void onMoreClick(VideoItem video, View anchor);
    }

    public VideoAdapter(Context context, List<VideoItem> videos) {
        this.context = context;
        this.videos = videos != null ? videos : new ArrayList<>();
    }

    public void setOnVideoClickListener(OnVideoClickListener listener) {
        this.listener = listener;
    }

    public void updateVideos(List<VideoItem> newVideos) {
        this.videos = newVideos != null ? newVideos : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem video = videos.get(position);

        holder.txtTitle.setText(video.getCleanTitle());
        holder.txtDuration.setText(video.getFormattedDuration());

        // Meta info like YouTube: folder · views · time ago
        String meta = (video.getFolderName() != null ? video.getFolderName() : "Local") +
                " · " + video.getViewCount() + " · " + video.getTimeAgo();
        holder.txtMeta.setText(meta);

        // Load thumbnail
        loadThumbnail(video, holder.imgThumbnail);

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVideoClick(video, position);
            }
        });

        holder.btnMore.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMoreClick(video, v);
            }
        });
    }

    private void loadThumbnail(VideoItem video, ImageView imageView) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, video.getId());
                Bitmap thumbnail = context.getContentResolver()
                        .loadThumbnail(contentUri, new Size(480, 270), null);
                imageView.setImageBitmap(thumbnail);
            } else {
                Bitmap thumbnail = MediaStore.Video.Thumbnails.getThumbnail(
                        context.getContentResolver(),
                        video.getId(),
                        MediaStore.Video.Thumbnails.MINI_KIND,
                        null);
                if (thumbnail != null) {
                    imageView.setImageBitmap(thumbnail);
                } else {
                    imageView.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
        } catch (Exception e) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        ImageView imgChannelAvatar;
        TextView txtTitle;
        TextView txtMeta;
        TextView txtDuration;
        ImageButton btnMore;

        VideoViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            imgChannelAvatar = itemView.findViewById(R.id.imgChannelAvatar);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtMeta = itemView.findViewById(R.id.txtMeta);
            txtDuration = itemView.findViewById(R.id.txtDuration);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
