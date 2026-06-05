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
import androidx.appcompat.app.AlertDialog;
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
import com.example.storyhub.models.Review;
import com.example.storyhub.utils.ImageHelper;
import com.example.storyhub.utils.TimeAgoHelper;
import com.example.storyhub.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private Context context;
    private List<Review> reviewList;
    private TokenManager tokenManager;
    private ReviewApiService reviewService;
    private String targetType;
    private int targetId;

    public ReviewAdapter(Context context, List<Review> reviewList,
                         String targetType, int targetId) {
        this.context = context;
        this.reviewList = reviewList;
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
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviewList.get(position);

        holder.txtUsername.setText(review.username);
        holder.txtRating.setText("⭐ " + review.rating + "/10");
        holder.txtKomentar.setText(review.komentar);
        holder.txtLikes.setText(String.valueOf(review.totalLikes));
        holder.txtBalasan.setText(String.valueOf(review.totalBalasan));
        holder.txtTimeAgo.setText(TimeAgoHelper.getTimeAgo(review.created));

        if (review.diedit != null && !review.diedit.isEmpty() && !review.diedit.equals("0")) {
            holder.txtDiedit.setVisibility(View.VISIBLE);
            holder.txtDiedit.setText("(diedit)");
        } else {
            holder.txtDiedit.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(ImageHelper.getImageUrl(review.profileImg))
                .placeholder(R.drawable.placeholder_png)
                .error(R.drawable.placeholder_png)
                .circleCrop()
                .into(holder.imgProfile);

        updateLikeIcon(holder, review.isLiked);

        if (tokenManager.isLoggedIn()) {
            reviewService.isLiked(targetType, targetId, "reviews", review.id)
                    .enqueue(new Callback<IsLikedResponse>() {
                        @Override
                        public void onResponse(Call<IsLikedResponse> call,
                                               Response<IsLikedResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                review.isLiked = response.body().isLiked;
                                updateLikeIcon(holder, review.isLiked);
                            }
                        }
                        @Override
                        public void onFailure(Call<IsLikedResponse> call, Throwable t) {}
                    });
        }

        holder.txtUsername.setOnClickListener(v -> openUserProfile(review.username));
        holder.imgProfile.setOnClickListener(v -> openUserProfile(review.username));

        holder.btnLike.setOnClickListener(v -> {
            if (!tokenManager.isLoggedIn()) {
                Toast.makeText(context,
                        "Kamu harus login untuk memberikan like ke review ini.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            toggleLike(holder, review, "reviews");
        });

        holder.btnComment.setOnClickListener(v -> {
            if (review.showReplies) {
                review.showReplies = false;
                holder.containerReplies.setVisibility(View.GONE);
                holder.containerReplies.removeAllViews();
            } else {
                review.showReplies = true;
                loadReplies(holder, review);
            }
        });
    }

    private void openUserProfile(String username) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra("username", username);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void updateLikeIcon(ViewHolder holder, boolean isLiked) {
        holder.btnLike.setImageResource(
                isLiked ? R.drawable.ic_like_active : R.drawable.ic_like_inactive);
    }

    private void toggleLike(ViewHolder holder, Review review, String likeTargetType) {
        if (review.isLiked) {
            reviewService.deleteLike(targetType, targetId, likeTargetType, review.id)
                    .enqueue(new Callback<MessageResponse>() {
                        @Override
                        public void onResponse(Call<MessageResponse> call,
                                               Response<MessageResponse> response) {
                            if (response.isSuccessful()) {
                                review.isLiked = false;
                                review.totalLikes = Math.max(0, review.totalLikes - 1);
                                updateLikeIcon(holder, false);
                                holder.txtLikes.setText(String.valueOf(review.totalLikes));
                            }
                        }
                        @Override
                        public void onFailure(Call<MessageResponse> call, Throwable t) {}
                    });
        } else {
            reviewService.postLike(targetType, targetId, likeTargetType, review.id)
                    .enqueue(new Callback<MessageResponse>() {
                        @Override
                        public void onResponse(Call<MessageResponse> call,
                                               Response<MessageResponse> response) {
                            if (response.isSuccessful()) {
                                review.isLiked = true;
                                review.totalLikes += 1;
                                updateLikeIcon(holder, true);
                                holder.txtLikes.setText(String.valueOf(review.totalLikes));
                            }
                        }
                        @Override
                        public void onFailure(Call<MessageResponse> call, Throwable t) {}
                    });
        }
    }

    private void loadReplies(ViewHolder holder, Review review) {
        holder.containerReplies.removeAllViews();
        holder.containerReplies.setVisibility(View.VISIBLE);

        View inputView = LayoutInflater.from(context)
                .inflate(R.layout.layout_comment_input, holder.containerReplies, false);
        EditText etInput = inputView.findViewById(R.id.etCommentInput);
        View btnSend = inputView.findViewById(R.id.btnSendComment);
        holder.containerReplies.addView(inputView);

        btnSend.setOnClickListener(v -> {
            if (!tokenManager.isLoggedIn()) {
                Toast.makeText(context, "Kamu harus login untuk berkomentar.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String text = etInput.getText().toString().trim();
            if (text.isEmpty()) return;

            reviewService.postComment("reviews", review.id, new CommentRequest(text))
                    .enqueue(new Callback<MessageResponse>() {
                        @Override
                        public void onResponse(Call<MessageResponse> call,
                                               Response<MessageResponse> response) {
                            if (response.isSuccessful()) {
                                etInput.setText("");
                                review.totalBalasan += 1;
                                holder.txtBalasan.setText(
                                        String.valueOf(review.totalBalasan));
                                loadCommentReplies(holder, review.id);
                            }
                        }
                        @Override
                        public void onFailure(Call<MessageResponse> call, Throwable t) {}
                    });
        });

        loadCommentReplies(holder, review.id);
    }

    private void loadCommentReplies(ViewHolder holder, int reviewId) {
        reviewService.getComments("reviews", reviewId)
                .enqueue(new Callback<CommentResponse>() {
                    @Override
                    public void onResponse(Call<CommentResponse> call,
                                           Response<CommentResponse> response) {

                        if (holder.containerReplies.getChildCount() > 1) {
                            holder.containerReplies.removeViews(
                                    1, holder.containerReplies.getChildCount() - 1);
                        }

                        if (response.isSuccessful() && response.body() != null
                                && response.body().data != null) {
                            for (Comment comment : response.body().data) {
                                addCommentView(holder.containerReplies, comment,
                                        "reviews", reviewId);
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<CommentResponse> call, Throwable t) {}
                });
    }

    private void addCommentView(LinearLayout container, Comment comment,
                                String parentType, int parentId) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_comment, container, false);

        TextView txtUsername = view.findViewById(R.id.txtUsername);
        TextView txtKomentar = view.findViewById(R.id.txtKomentar);
        TextView txtLikes = view.findViewById(R.id.txtLikes);
        TextView txtBalasan = view.findViewById(R.id.txtBalasan);
        TextView txtTimeAgo = view.findViewById(R.id.txtTimeAgo);
        TextView txtDiedit = view.findViewById(R.id.txtDiedit);
        ImageView imgProfile = view.findViewById(R.id.imgProfile);
        ImageView btnLike = view.findViewById(R.id.btnLike);
        ImageView btnReply = view.findViewById(R.id.btnReply);
        LinearLayout containerReplies = view.findViewById(R.id.containerReplies);

        txtUsername.setText(comment.username);
        txtKomentar.setText(comment.komentar);
        txtLikes.setText(String.valueOf(comment.totalLikes));
        txtBalasan.setText(String.valueOf(comment.totalBalasan));
        txtTimeAgo.setText(TimeAgoHelper.getTimeAgo(comment.created));

        if (comment.diedit != null && !comment.diedit.isEmpty()) {
            txtDiedit.setVisibility(View.VISIBLE);
            txtDiedit.setText("(diedit)");
        }

        txtUsername.setOnClickListener(v -> openUserProfile(comment.username));
        imgProfile.setOnClickListener(v -> openUserProfile(comment.username));

        Glide.with(context)
                .load(ImageHelper.getImageUrl(comment.profileImg))
                .placeholder(R.drawable.placeholder_png)
                .error(R.drawable.placeholder_png)
                .circleCrop()
                .into(imgProfile);

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
                                btnLike.setImageResource(isLiked[0]
                                        ? R.drawable.ic_like_active
                                        : R.drawable.ic_like_inactive);
                            }
                        }
                        @Override
                        public void onFailure(Call<IsLikedResponse> call, Throwable t) {}
                    });
        }

        btnLike.setOnClickListener(v -> {
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
                                    btnLike.setImageResource(R.drawable.ic_like_inactive);
                                    txtLikes.setText(String.valueOf(likesCount[0]));
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
                                    btnLike.setImageResource(R.drawable.ic_like_active);
                                    txtLikes.setText(String.valueOf(likesCount[0]));
                                }
                            }
                            @Override
                            public void onFailure(Call<MessageResponse> call, Throwable t) {}
                        });
            }
        });

        boolean[] showingReplies = {false};
        btnReply.setOnClickListener(v -> {
            if (showingReplies[0]) {
                showingReplies[0] = false;
                containerReplies.setVisibility(View.GONE);
                containerReplies.removeAllViews();
            } else {
                showingReplies[0] = true;
                loadNestedReplies(containerReplies, comment);
            }
        });

        container.addView(view);
    }

    private void loadNestedReplies(LinearLayout container, Comment comment) {
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
                public void onResponse(
                        Call<MessageResponse> call,
                        Response<MessageResponse> response) {

                    if (response.isSuccessful()) {

                        etInput.setText("");

                        loadNestedCommentReplies(
                                container,
                                targetCommentId
                        );
                    }
                }

                @Override
                public void onFailure(
                        Call<MessageResponse> call,
                        Throwable t) {
                }
            });
        });

        loadNestedCommentReplies(container, comment.id);
    }

    private void loadNestedCommentReplies(LinearLayout container, int commentId) {
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
                                addCommentView(container, c, "comments", commentId);
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<CommentResponse> call, Throwable t) {}
                });
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile, btnLike, btnComment;
        TextView txtUsername, txtRating, txtKomentar, txtLikes,
                txtBalasan, txtTimeAgo, txtDiedit;
        LinearLayout containerReplies;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtKomentar = itemView.findViewById(R.id.txtKomentar);
            txtLikes = itemView.findViewById(R.id.txtLikes);
            txtBalasan = itemView.findViewById(R.id.txtBalasan);
            txtTimeAgo = itemView.findViewById(R.id.txtTimeAgo);
            txtDiedit = itemView.findViewById(R.id.txtDiedit);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            containerReplies = itemView.findViewById(R.id.containerReplies);
        }
    }
}