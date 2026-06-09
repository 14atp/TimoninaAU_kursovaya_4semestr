package com.myapp.guidbuild.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.navigation.NavigationView;
import com.myapp.guidbuild.R;
import com.myapp.guidbuild.data.database.AppDatabase;
import com.myapp.guidbuild.data.database.Game;
import com.myapp.guidbuild.data.database.User;
import com.myapp.guidbuild.ui.auth.LoginActivity;
import com.myapp.guidbuild.ui.main.create.CreateGuideFragment;
import com.myapp.guidbuild.ui.main.home.HomeFragment;
import com.myapp.guidbuild.ui.main.profile.AccountFragment;
import com.myapp.guidbuild.ui.main.settings.SettingsFragment;
import com.myapp.guidbuild.utils.AvatarHelper;
import com.myapp.guidbuild.utils.SessionManager;
import com.myapp.guidbuild.utils.UserConstants;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FragmentManager fragmentManager;
    private SessionManager sessionManager;
    private ImageView ivAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        fragmentManager = getSupportFragmentManager();
        sessionManager = new SessionManager(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

        // аватарка справа, переход в аккаунт
        ivAvatar = findViewById(R.id.ivAvatar);
        ivAvatar.setOnClickListener(v -> {
            openFragment(new AccountFragment(), "Аккаунт");
            navigationView.setCheckedItem(R.id.nav_account);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        loadAvatar();


        if (savedInstanceState == null) {
            openFragment(new HomeFragment(), "Главная");
            navigationView.setCheckedItem(R.id.nav_home);
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                openFragment(new HomeFragment(), "Главная");
            } else if (id == R.id.nav_create_guide) {
                openFragment(new CreateGuideFragment(), "Создать гайд");
            } else if (id == R.id.nav_account) {
                openFragment(new AccountFragment(), "Аккаунт");
            } else if (id == R.id.nav_settings) {
                openFragment(new SettingsFragment(), "Настройки");
            } else if (id == R.id.nav_admin_users) {
                openFragment(new AdminUsersFragment(), "Пользователи");
            } else if (id == R.id.nav_admin_games) {
                showAddGameDialog();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_admin_classes) {
                showAddClassDialog();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_logout) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Выход")
                        .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                        .setPositiveButton("Да", (dialog, which) -> logout())
                        .setNegativeButton("Нет", null)
                        .show();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });


        navigationView.post(() -> updateAdminMenuVisibility());
    }

    private void loadAvatar() {
        long userId = sessionManager.getCurrentUserId();
        if (userId == -1) return;

        new Thread(() -> {
            User user = AppDatabase.getInstance(this).userDao().getUserById(userId);
            runOnUiThread(() -> {
                if (user != null) {
                    AvatarHelper.loadAvatar(this, user.avatarPath, ivAvatar);
                }
            });
        }).start();
    }

    private void updateAdminMenuVisibility() {
        long userId = sessionManager.getCurrentUserId();

        new Thread(() -> {
            User user = AppDatabase.getInstance(this).userDao().getUserById(userId);
            boolean isAdmin = (user != null && user.role == UserConstants.ROLE_ADMIN);

            runOnUiThread(() -> {
                try {
                    // проверяем, что меню существует
                    if (navigationView == null || navigationView.getMenu() == null) {
                        Log.e("MainActivity", "navigationView or menu is null");
                        return;
                    }

                    // находим пункты меню
                    MenuItem usersItem = navigationView.getMenu().findItem(R.id.nav_admin_users);
                    MenuItem gamesItem = navigationView.getMenu().findItem(R.id.nav_admin_games);
                    MenuItem classesItem = navigationView.getMenu().findItem(R.id.nav_admin_classes);

                    // устанавливаем видимость, только если пункты существуют
                    if (usersItem != null) usersItem.setVisible(isAdmin);
                    if (gamesItem != null) gamesItem.setVisible(isAdmin);
                    if (classesItem != null) classesItem.setVisible(isAdmin);

                    Log.d("MainActivity", "Admin menu visibility updated: isAdmin=" + isAdmin);
                } catch (Exception e) {
                    Log.e("MainActivity", "Error updating admin menu", e);
                }
            });
        }).start();
    }

    private void openFragment(Fragment fragment, String title) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
        toolbar.setTitle(title);
        Log.d("MainActivity", "set toolbar title to: " + title);

        // скрываем аватарку на странице аккаунта
        if (fragment instanceof AccountFragment) {
            ivAvatar.setVisibility(View.GONE);
        } else {
            ivAvatar.setVisibility(View.VISIBLE);
        }
    }

    private void logout() {
        sessionManager.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showAddGameDialog() {
        // проверка, что текущий пользователь — админ
        long userId = sessionManager.getCurrentUserId();
        new Thread(() -> {
            User currentUser = AppDatabase.getInstance(this).userDao().getUserById(userId);
            boolean isAdmin = (currentUser != null && currentUser.role == UserConstants.ROLE_ADMIN);

            runOnUiThread(() -> {
                if (!isAdmin) {
                    Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
                    return;
                }

                // существующий код диалога
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Добавить игру");
                EditText input = new EditText(this);
                input.setHint("Название игры");
                builder.setView(input);
                builder.setPositiveButton("Добавить", (dialog, which) -> {
                    String gameName = input.getText().toString().trim();
                    if (!gameName.isEmpty()) {
                        new Thread(() -> {
                            AppDatabase.getInstance(this).gameDao().insert(new Game(gameName));
                            runOnUiThread(() -> Toast.makeText(this, "Игра добавлена", Toast.LENGTH_SHORT).show());
                        }).start();
                    }
                });
                builder.setNegativeButton("Отмена", null);
                builder.show();
            });
        }).start();
    }

    private void showAddClassDialog() {
        // проверка, что текущий пользователь — админ
        long userId = sessionManager.getCurrentUserId();
        new Thread(() -> {
            User currentUser = AppDatabase.getInstance(this).userDao().getUserById(userId);
            boolean isAdmin = (currentUser != null && currentUser.role == UserConstants.ROLE_ADMIN);

            runOnUiThread(() -> {
                if (!isAdmin) {
                    Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
                    return;
                }

                // временная заглушка
                new AlertDialog.Builder(this)
                        .setTitle("Добавить класс")
                        .setMessage("Функция в разработке")
                        .setPositiveButton("OK", null)
                        .show();
            });
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return true;
    }
}