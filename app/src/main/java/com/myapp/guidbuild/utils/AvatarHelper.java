package com.myapp.guidbuild.utils;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.myapp.guidbuild.R;
import java.io.File;

public class AvatarHelper {

    public static void loadAvatar(Context context, String avatarPath, ImageView imageView) {
        if (avatarPath != null && !avatarPath.isEmpty()) {
            File imgFile = new File(avatarPath);
            if (imgFile.exists()) {
                // используем время последнего изменения файла как уникальную подпись
                long lastModified = imgFile.lastModified();

                Glide.with(context)
                        .load(imgFile)
                        .circleCrop()
                        .placeholder(R.drawable.ic_avatar)
                        .error(R.drawable.ic_avatar)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .signature(new ObjectKey(lastModified))  // ← ключевая строка
                        .into(imageView);
                return;
            }
        }
        imageView.setImageResource(R.drawable.ic_avatar);
    }
}