package com.example.storyhub.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storyhub.R;
import com.example.storyhub.UserProfileActivity;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.api.ReviewApiService;
import com.example.storyhub.models.Comment;
import com.example.storyhub.models.CommentRequest;
import com.example.storyhub.models.CommentResponse;
import com.example.storyhub.models.IsLikedResponse;
import com.example.storyhub.models.MessageResponse;
import com.example.storyhub.utils.ImageHelper;
import com.example.storyhub.utils.TimeAgoHelper;
import com.example.storyhub.utils.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context context;
    private List<Comment> commentList;
    private TokenManager tokenManager;
    private ReviewApiService reviewService;
    private String targetType;
    private int targetId;

    public CommentAdapter(Context context, List<Comment> commentList,
                          String targetType, int targetId) {
        this.context = context;
        this.commentList = commentList;
        this.targetType = targetType;
        this.targetId = targetId;
        this.tokenManager = new TokenManager(context);
        this.reviewService = RetrofitClient.getAuthInstance(tokenManager)
                .create(ReviewApiService.class);
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
        Comment comment = commentList.get(position);
        bindComment(holder, comment, targetType, targetId);
    }

    public void bindComment(ViewHolder holder, Comment comment,
                            String parentType, int parentId) {

        holder.txtUsername.setText(comment.username);
        holder.txtKomentar.setText(comment.komentar);
        holder.txtLikes.setText(String.valueOf(comment.totalLikes));
        holder.txtBalasan.setText(String.valueOf(comment.totalBalasan));
        holder.txtTimeAgo.setText(TimeAgoHelper.getTimeAgo(comment.created));

        if (comment.diedit != null && !comment.diedit.isEmpty()) {
            holder.txtDiedit.setVisibility(View.VISIBLE);
            holder.txtDiedit.setText("(diedit)");
        } else {
            holder.txtDiedit.setVisibility(View.GONE);
        }

        holder.txtUsername.setOnClickListener(v -> openUserProfile(comment.username));
        holder.imgProfile.setOnClickListener(v -> openUserProfile(comment.username));

        Glide.with(context)
                .load(ImageHelper.getImageUrl(comment.profileImg))
                .placeholder(R.drawable.placeholder_png)
                .error(R.drawable.placeholder_png)
                .circleCrop()
                .into(holder.imgProfile);

        boolean[] isLiked = {comment.isLiked};
        int[] likesCount = {comment.totalLikes};

        if (tokenManager.isLoggedIn()) {
            reviewService.isLiked(parentType, parentId, "comments", comment.id)
                    .enqueue(new Callback<IsLikedResponse>() {
                        @Override
                        public void onResponse(Call<IsLikedResponse> call,
                                               Response<IsLikedResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                isLiked[0] = response.body().isLiked;
                                holder.btnLike.setImageResource(isLiked[0]
                                        ? R.drawable.ic_like_active
                                        : R.drawable.ic_like_inactive);
                            }
                        }
                        @Override
                        public void onFailure(Call<IsLikedResponse> call, Throwable t) {}
                    });
        }

        holder.btnLike.setOnClickListener(v -> {
            if (!tokenManager.isLoggedIn()) {
                Toast.makeText(context,
                        "Kamu harus login untuk memberikan like ke komentar ini.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (isLiked[0]) {
                reviewService.deleteLike(parentType, parentId, "comments", comment.id)
                        .enqueue(new Callback<MessageResponse>() {
                            @Override
                            public void onResponse(Call<MessageResponse> call,
                                                   Response<MessageResponse> response) {
                                if (response.isSuccessful()) {
                                    isLiked[0] = false;
                                    likesCount[0] = Math.max(0, likesCount[0] - 1);
                                    holder.btnLike.setImageResource(R.drawable.ic_like_inactive);
                                    holder.txtLikes.setText(String.valueOf(likesCount[0]));
                                }
                            }
                            @Override
                            public void onFailure(Call<MessageResponse> call, Throwable t) {}
                        });
            } else {
                reviewService.postLike(parentType, parentId, "comments", comment.id)
                        .enqueue(new Callback<MessageResponse>() {
                            @Override
                            public void onResponse(Call<MessageResponse> call,
                                                   Response<MessageResponse> response) {
                                if (response.isSuccessful()) {
                                    isLiked[0] = true;
                                    likesCount[0] += 1;
                                    holder.btnLike.setImageResource(R.drawable.ic_like_active);
                                    holder.txtLikes.setText(String.valueOf(likesCount[0]));
                                }
                            }
                            @Override
                            public void onFailure(Call<MessageResponse> call, Throwable t) {}
                        });
            }
        });

        boolean[] showingReplies = {false};
        holder.btnReply.setOnClickListener(v -> {
            if (showingReplies[0]) {
                showingReplies[0] = false;
                holder.containerReplies.setVisibility(View.GONE);
                holder.containerReplies.removeAllViews();
            } else {
                showingReplies[0] = true;
                loadReplies(holder.containerReplies, comment, comment.depth);
            }
        });
    }

    private void openUserProfile(String username) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra("username", username);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void loadReplies(LinearLayout container, Comment comment, int depth) {
        container.removeAllViews();
        container.setVisibility(View.VISIBLE);

        View inputView = LayoutInflater.from(context)
                .inflate(R.layout.layout_comment_input, container, false);
        EditText etInput = inputView.findViewById(R.id.etCommentInput);
        View btnSend = inputView.findViewById(R.id.btnSendComment);
        container.addView(inputView);

        btnSend.setOnClickListener(v -> {
            if (!tokenManager.isLoggedIn()) {
                Toast.makeText(context,
                        "Kamu harus login untuk berkomentar.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String text = etInput.getText().toString().trim();

            if (text.isEmpty()) return;

            int targetCommentId;

            if (comment.depth >= 7 && comment.parentCommentId != null) {
                targetCommentId = comment.parentCommentId;
            } else {
                targetCommentId = comment.id;
            }

            reviewService.postComment(
                    "comments",
                    targetCommentId,
                    new CommentRequest(text)
            ).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call,
                                       Response<MessageResponse> response) {

                    if (response.isSuccessful()) {
                        etInput.setText("");

                        loadNestedReplies(
                                container,
                                targetCommentId
                        );
                    }
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {}
            });
        });

        loadNestedReplies(container, comment.id);
    }

    private void loadNestedReplies(LinearLayout container, int commentId) {
        reviewService.getComments("comments", commentId)
                .enqueue(new Callback<CommentResponse>() {
                    @Override
                    public void onResponse(Call<CommentResponse> call,
                                           Response<CommentResponse> response) {
                        if (container.getChildCount() > 1) {
                            container.removeViews(1, container.getChildCount() - 1);
                        }
                        if (response.isSuccessful() && response.body() != null
                                && response.body().data != null) {
                            for (Comment c : response.body().data) {
                                View view = LayoutInflater.from(context)
                                        .inflate(R.layout.item_comment, container, false);
                                ViewHolder vh = new ViewHolder(view);
                                bindComment(vh, c, "comments", commentId);
                                container.addView(view);
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<CommentResponse> call, Throwable t) {}
                });
    }

    @Override
    public int getItemCount() {
        return commentList != null ? commentList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile, btnLike, btnReply;
        TextView txtUsername, txtKomentar, txtLikes,
                txtBalasan, txtTimeAgo, txtDiedit;
        LinearLayout containerReplies;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtKomentar = itemView.findViewById(R.id.txtKomentar);
            txtLikes = itemView.findViewById(R.id.txtLikes);
            txtBalasan = itemView.findViewById(R.id.txtBalasan);
            txtTimeAgo = itemView.findViewById(R.id.txtTimeAgo);
            txtDiedit = itemView.findViewById(R.id.txtDiedit);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnReply = itemView.findViewById(R.id.btnReply);
            containerReplies = itemView.findViewById(R.id.containerReplies);
        }
    }
}