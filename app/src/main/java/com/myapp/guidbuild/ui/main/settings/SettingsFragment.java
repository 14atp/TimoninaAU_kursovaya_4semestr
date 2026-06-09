package com.myapp.guidbuild.ui.main.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.myapp.guidbuild.R;
import com.myapp.guidbuild.data.database.AppDatabase;
import com.myapp.guidbuild.data.database.User;
import com.myapp.guidbuild.utils.SessionManager;
import com.myapp.guidbuild.utils.UserConstants;

public class SettingsFragment extends Fragment {

    private Button btnToggleAdmin;
    private SessionManager sessionManager;
    private long userId;
    private boolean isAdmin = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        sessionManager = new SessionManager(requireContext());
        userId = sessionManager.getCurrentUserId();

        btnToggleAdmin = view.findViewById(R.id.btnToggleAdmin);

        loadUserRole();

        btnToggleAdmin.setOnClickListener(v -> toggleAdminRole());

        return view;
    }

    private void loadUserRole() {
        new Thread(() -> {
            User user = AppDatabase.getInstance(requireContext()).userDao().getUserById(userId);
            isAdmin = (user != null && user.role == UserConstants.ROLE_ADMIN);

            requireActivity().runOnUiThread(() -> {
                if (isAdmin) {
                    btnToggleAdmin.setText("Стать обычным пользователем");
                } else {
                    btnToggleAdmin.setText("Стать админом");
                }
            });
        }).start();
    }

    private void toggleAdminRole() {
        int newRole = isAdmin ? UserConstants.ROLE_USER : UserConstants.ROLE_ADMIN;
        String message = isAdmin ? "Вы уверены, что хотите стать обычным пользователем?" : "Вы уверены, что хотите стать администратором?";

        new AlertDialog.Builder(requireContext())
                .setTitle("Смена роли")
                .setMessage(message)
                .setPositiveButton("Да", (dialog, which) -> {
                    new Thread(() -> {
                        AppDatabase.getInstance(requireContext())
                                .userDao().setUserRole(userId, newRole);

                        requireActivity().runOnUiThread(() -> {
                            isAdmin = !isAdmin;
                            if (isAdmin) {
                                btnToggleAdmin.setText("Стать обычным пользователем");
                                Toast.makeText(getContext(), "Теперь вы администратор. Перезайдите, чтоб функции обновились.", Toast.LENGTH_SHORT).show();
                            } else {
                                btnToggleAdmin.setText("Стать админом");
                                Toast.makeText(getContext(), "Теперь вы обычный пользователь. Перезайдите, чтоб функции обновились.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                })
                .setNegativeButton("Нет", null)
                .show();
    }
}