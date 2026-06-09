package com.myapp.guidbuild.ui.main.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.imageview.ShapeableImageView;
import com.myapp.guidbuild.R;
import com.myapp.guidbuild.data.database.AppDatabase;
import com.myapp.guidbuild.data.database.Guide;
import com.myapp.guidbuild.data.database.User;
import com.myapp.guidbuild.utils.AvatarHelper;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class GuidesAdapter extends RecyclerView.Adapter<GuidesAdapter.ViewHolder> {

    private List<Guide> guides;
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private long currentUserId;

    private OnItemClickListener itemClickListener;
    private OnLikeClickListener likeClickListener;
    private OnFavoriteClickListener favoriteClickListener;
    private OnDeleteClickListener deleteClickListener;

    private boolean showDelete = false;

    public interface OnItemClickListener {
        void onItemClick(Guide guide);
    }

    public interface OnLikeClickListener {
        void onLikeClick(Guide guide, int position);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Guide guide, int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Guide guide, int position);
    }

    public GuidesAdapter(List<Guide> guides) {
        this.guides = guides;
    }

    public void setShowDelete(boolean show) {
        this.showDelete = show;
    }

    public void setCurrentUserId(long userId) {
        this.currentUserId = userId;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnLikeClickListener(OnLikeClickListener listener) {
        this.likeClickListener = listener;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public void updateData(List<Guide> newGuides) {
        this.guides = newGuides;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_guide, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Guide guide = guides.get(position);

        holder.tvTitle.setText(guide.title);
        holder.tvDate.setText(dateFormat.format(guide.createdDate));
        holder.tvLikes.setText(String.valueOf(guide.likesCount));

        holder.tvAuthorName.setText("Загрузка...");

        // загружаем название игры
        new Thread(() -> {
            String gameName = AppDatabase.getInstance(context).gameDao().getGameNameById(guide.gameId);
            holder.itemView.post(() -> {
                if (gameName != null) {
                    holder.tvGameName.setText(gameName);
                } else {
                    holder.tvGameName.setText("Игра " + guide.gameId);
                }
            });
        }).start();

        // загружаем автора
        loadAuthor(holder, guide.userId);

        // иконки (без проверок, только обработчики)
        holder.ivLike.setOnClickListener(v -> {
            if (likeClickListener != null) {
                likeClickListener.onLikeClick(guide, position);
            }
        });

        holder.ivFavorite.setOnClickListener(v -> {
            if (favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(guide, position);
            }
        });

        // удаление
        if (showDelete && holder.ivDelete != null) {
            holder.ivDelete.setVisibility(View.VISIBLE);
            holder.ivDelete.setOnClickListener(v -> {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(guide, position);
                }
            });
        } else if (holder.ivDelete != null) {
            holder.ivDelete.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(guide);
            }
        });
    }

    private void loadAuthor(ViewHolder holder, long userId) {
        new Thread(() -> {
            User author = AppDatabase.getInstance(context).userDao().getUserById(userId);
            holder.itemView.post(() -> {
                if (author != null) {
                    holder.tvAuthorName.setText(author.login);
                    AvatarHelper.loadAvatar(context, author.avatarPath, holder.ivAuthorAvatar);
                } else {
                    holder.tvAuthorName.setText("Неизвестный автор");
                }
            });
        }).start();
    }

    @Override
    public int getItemCount() {
        return guides == null ? 0 : guides.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvGameName, tvDate, tvDescription, tvAuthorName, tvLikes;
        ShapeableImageView ivAuthorAvatar;
        ImageView ivLike, ivFavorite, ivDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvGameName = itemView.findViewById(R.id.tvGameName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            ivAuthorAvatar = itemView.findViewById(R.id.ivAuthorAvatar);
            ivLike = itemView.findViewById(R.id.ivLike);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}