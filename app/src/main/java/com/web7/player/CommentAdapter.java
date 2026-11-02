package com.web7.player;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private final List<Comment> comments;

    public static class Comment {
        String author;
        String body;
        String timeAgo;
        int likes;

        public Comment(String author, String body, String timeAgo, int likes) {
            this.author = author;
            this.body = body;
            this.timeAgo = timeAgo;
            this.likes = likes;
        }
    }

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments != null ? comments : new ArrayList<>();
    }

    public void addComment(Comment comment) {
        comments.add(0, comment);
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.txtCommentAuthor.setText("@" + comment.author);
        holder.txtCommentBody.setText(comment.body);
        holder.txtCommentTime.setText(comment.timeAgo);
        holder.txtCommentLikes.setText(String.valueOf(comment.likes));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCommentAuthor;
        TextView txtCommentBody;
        TextView txtCommentTime;
        TextView txtCommentLikes;

        ViewHolder(View itemView) {
            super(itemView);
            txtCommentAuthor = itemView.findViewById(R.id.txtCommentAuthor);
            txtCommentBody = itemView.findViewById(R.id.txtCommentBody);
            txtCommentTime = itemView.findViewById(R.id.txtCommentTime);
            txtCommentLikes = itemView.findViewById(R.id.txtCommentLikes);
        }
    }
}
