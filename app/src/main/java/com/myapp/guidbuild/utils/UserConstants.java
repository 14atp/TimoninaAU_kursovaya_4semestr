package com.myapp.guidbuild.utils;

import android.content.Context;
import android.app.Activity;
import android.widget.Toast;

import com.myapp.guidbuild.data.database.AppDatabase;
public class UserConstants {
    public static final int ROLE_USER = 0;
    public static final int ROLE_ADMIN = 1;

    public static final int STATUS_ACTIVE = 0;
    public static final int STATUS_BANNED = 1;

    //метод для быстрой смены роли смены роли (для тестирования)
    public static void makeAdmin(Context context, long userId) {
        new Thread(() -> {
            AppDatabase.getInstance(context).userDao()
                    .setUserRole(userId, ROLE_ADMIN);

            // Опционально: показать уведомление в UI потоке
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "Теперь вы администратор!", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // Для удобства — получить роль как строку
    public static String getRoleName(int role) {
        return role == ROLE_ADMIN ? "Администратор" : "Пользователь";
    }

    public static String getStatusName(int status) {
        return status == STATUS_BANNED ? "Заблокирован" : "Активен";
    }
}