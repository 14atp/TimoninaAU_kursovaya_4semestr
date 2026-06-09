package com.myapp.guidbuild.ui.main.create;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.myapp.guidbuild.R;
import com.myapp.guidbuild.data.database.GuideBlock;
import java.io.File;
import java.util.List;

public class BlocksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<GuideBlock> blocks;
    private Context context;
    private OnBlockDeleteListener deleteListener;
    private OnImagePickedListener imagePickedListener;

    public interface OnBlockDeleteListener {
        void onDelete(int position);
    }

    public interface OnImagePickedListener {
        void onImagePicked(int position);
    }

    public BlocksAdapter(List<GuideBlock> blocks) {
        this.blocks = blocks;
    }

    public void setOnBlockDeleteListener(OnBlockDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setOnImagePickedListener(OnImagePickedListener listener) {
        this.imagePickedListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return blocks.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == GuideBlock.TYPE_TEXT) {
            View view = inflater.inflate(R.layout.item_block_text, parent, false);
            return new TextBlockViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_block_image, parent, false);
            return new ImageBlockViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GuideBlock block = blocks.get(position);
        int currentPosition = position; // сохраняем позицию

        if (holder instanceof TextBlockViewHolder) {
            TextBlockViewHolder textHolder = (TextBlockViewHolder) holder;
            textHolder.etBlockText.setText(block.text);

            // убираем старый listener, чтобы не было дублирования
            textHolder.etBlockText.removeTextChangedListener(textHolder.textWatcher);

            // создаём новый listener
            TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    block.text = s.toString();
                }
                @Override
                public void afterTextChanged(Editable s) {}
            };
            textHolder.etBlockText.addTextChangedListener(watcher);
            textHolder.textWatcher = watcher;

            textHolder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDelete(currentPosition);
            });
        } else if (holder instanceof ImageBlockViewHolder) {
            ImageBlockViewHolder imageHolder = (ImageBlockViewHolder) holder;
            if (block.imagePath != null && !block.imagePath.isEmpty()) {
                Glide.with(context).load(new File(block.imagePath)).into(imageHolder.ivImage);
            }
            imageHolder.btnChangeImage.setOnClickListener(v -> {
                if (imagePickedListener != null) {
                    imagePickedListener.onImagePicked(currentPosition);
                }
            });
            imageHolder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDelete(currentPosition);
            });
        }
    }

    @Override
    public int getItemCount() {
        return blocks == null ? 0 : blocks.size();
    }

    static class TextBlockViewHolder extends RecyclerView.ViewHolder {
        EditText etBlockText;
        Button btnDelete;
        TextWatcher textWatcher;

        TextBlockViewHolder(View itemView) {
            super(itemView);
            etBlockText = itemView.findViewById(R.id.etBlockText);
            btnDelete = itemView.findViewById(R.id.btnDeleteBlock);
        }
    }

    static class ImageBlockViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        Button btnChangeImage, btnDelete;

        ImageBlockViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivBlockImage);
            btnChangeImage = itemView.findViewById(R.id.btnChangeImage);
            btnDelete = itemView.findViewById(R.id.btnDeleteBlock);
        }
    }
}