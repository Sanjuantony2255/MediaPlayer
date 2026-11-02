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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RelatedVideoAdapter extends RecyclerView.Adapter<RelatedVideoAdapter.ViewHolder> {

    private List<VideoItem> videos;
    private final Context context;
    private OnRelatedVideoClickListener listener;

    public interface OnRelatedVideoClickListener {
        void onRelatedVideoClick(VideoItem video);
    }

    public RelatedVideoAdapter(Context context, List<VideoItem> videos) {
        this.context = context;
        this.videos = videos != null ? videos : new ArrayList<>();
    }

    public void setOnRelatedVideoClickListener(OnRelatedVideoClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_related_video, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoItem video = videos.get(position);

        holder.txtRelatedTitle.setText(video.getCleanTitle());
        holder.txtRelatedDuration.setText(video.getFormattedDuration());
        String meta = (video.getFolderName() != null ? video.getFolderName() : "Local") +
                " · " + video.getViewCount();
        holder.txtRelatedMeta.setText(meta);

        // Load thumbnail
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, video.getId());
                Bitmap thumbnail = context.getContentResolver()
                        .loadThumbnail(contentUri, new Size(320, 180), null);
                holder.imgRelatedThumb.setImageBitmap(thumbnail);
            } else {
                Bitmap thumbnail = MediaStore.Video.Thumbnails.getThumbnail(
                        context.getContentResolver(),
                        video.getId(),
                        MediaStore.Video.Thumbnails.MINI_KIND,
                        null);
                if (thumbnail != null) {
                    holder.imgRelatedThumb.setImageBitmap(thumbnail);
                }
            }
        } catch (Exception e) {
            holder.imgRelatedThumb.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRelatedVideoClick(video);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRelatedThumb;
        TextView txtRelatedTitle;
        TextView txtRelatedMeta;
        TextView txtRelatedDuration;

        ViewHolder(View itemView) {
            super(itemView);
            imgRelatedThumb = itemView.findViewById(R.id.imgRelatedThumb);
            txtRelatedTitle = itemView.findViewById(R.id.txtRelatedTitle);
            txtRelatedMeta = itemView.findViewById(R.id.txtRelatedMeta);
            txtRelatedDuration = itemView.findViewById(R.id.txtRelatedDuration);
        }
    }
}
