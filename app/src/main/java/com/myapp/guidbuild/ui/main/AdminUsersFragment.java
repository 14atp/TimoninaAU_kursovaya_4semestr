package com.myapp.guidbuild.ui.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.imageview.ShapeableImageView;
import com.myapp.guidbuild.R;
import com.myapp.guidbuild.data.database.AppDatabase;
import com.myapp.guidbuild.data.database.User;
import com.myapp.guidbuild.utils.AvatarHelper;
import com.myapp.guidbuild.utils.SessionManager;
import com.myapp.guidbuild.utils.UserConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminUsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText etSearch;
    private UsersAdapter adapter;
    private List<User> allUsers;
    private List<User> filteredUsers;
    private long currentAdminId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_users, container, false);

        currentAdminId = new SessionManager(requireContext()).getCurrentUserId();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        etSearch = view.findViewById(R.id.etSearch);

        filteredUsers = new ArrayList<>();
        adapter = new UsersAdapter(filteredUsers);
        recyclerView.setAdapter(adapter);

        loadUsers();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadUsers() {
        new Thread(() -> {
            allUsers = AppDatabase.getInstance(requireContext())
                    .userDao().getAllUsersSync();

            requireActivity().runOnUiThread(() -> {
                filteredUsers.clear();
                filteredUsers.addAll(allUsers);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void filterUsers() {
        String searchText = etSearch.getText().toString().toLowerCase(Locale.getDefault());

        filteredUsers.clear();
        for (User user : allUsers) {
            if (searchText.isEmpty() || user.login.toLowerCase().contains(searchText)) {
                filteredUsers.add(user);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

        private List<User> users;

        UsersAdapter(List<User> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User user = users.get(position);
            holder.tvLogin.setText(user.login);
            holder.tvEmail.setText(user.email);

            String roleText = user.role == UserConstants.ROLE_ADMIN ? "Админ" : "Пользователь";
            String statusText = user.status == UserConstants.STATUS_BANNED ? "заблокирован" : "активен";
            holder.tvStatus.setText(roleText + " • " + statusText);

            // загружаем аватарку
            AvatarHelper.loadAvatar(holder.itemView.getContext(), user.avatarPath, holder.ivAvatar);

            if (user.status == UserConstants.STATUS_BANNED) {
                holder.btnBan.setText("Разблокировать");
            } else {
                holder.btnBan.setText("Заблокировать");
            }

            if (user.id == currentAdminId) {
                holder.btnBan.setEnabled(false);
                holder.btnBan.setText("Это вы");
            } else {
                holder.btnBan.setEnabled(true);
                holder.btnBan.setOnClickListener(v -> {
                    if (user.status == UserConstants.STATUS_BANNED) {
                        unbanUser(user);
                    } else {
                        showBanDialog(user);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvLogin, tvEmail, tvStatus;
            Button btnBan;
            ShapeableImageView ivAvatar;

            ViewHolder(View itemView) {
                super(itemView);
                tvLogin = itemView.findViewById(R.id.tvLogin);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                btnBan = itemView.findViewById(R.id.btnBan);
                ivAvatar = itemView.findViewById(R.id.ivAvatar);
            }
        }
    }

    private void showBanDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Блокировка пользователя");
        builder.setMessage("Заблокировать пользователя " + user.login + "?");

        EditText input = new EditText(requireContext());
        input.setHint("Причина блокировки (необязательно)");
        builder.setView(input);

        builder.setPositiveButton("Заблокировать", (dialog, which) -> {
            String reason = input.getText().toString().trim();
            if (reason.isEmpty()) reason = "Нарушение правил";
            banUser(user.id, reason);
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void banUser(long userId, String reason) {
        new Thread(() -> {
            AppDatabase.getInstance(requireContext())
                    .userDao().updateUserStatus(userId, UserConstants.STATUS_BANNED, reason, System.currentTimeMillis());
            requireActivity().runOnUiThread(() -> {
                loadUsers();
                Toast.makeText(getContext(), "Пользователь заблокирован", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void unbanUser(User user) {
        new Thread(() -> {
            AppDatabase.getInstance(requireContext())
                    .userDao().updateUserStatus(user.id, UserConstants.STATUS_ACTIVE, null, 0);
            requireActivity().runOnUiThread(() -> {
                loadUsers();
                Toast.makeText(getContext(), "Пользователь разблокирован", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}