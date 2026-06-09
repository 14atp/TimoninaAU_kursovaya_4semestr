package com.myapp.guidbuild.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.myapp.guidbuild.R;
import com.myapp.guidbuild.data.database.AppDatabase;
import com.myapp.guidbuild.data.database.User;
import com.myapp.guidbuild.utils.HashUtils;
import com.myapp.guidbuild.utils.SessionManager;
import com.myapp.guidbuild.utils.UserConstants;
import com.myapp.guidbuild.ui.main.MainActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etLogin, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister, btnBackToLogin;
    private ProgressBar progressBar;
    private TextView tvError;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(this);

        etLogin = findViewById(R.id.etLogin);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);

        btnRegister.setOnClickListener(v -> attemptRegister());
        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String login = etLogin.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Валидация
        if (login.isEmpty() || login.length() < 3) {
            tvError.setText("Логин должен быть минимум 3 символа");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvError.setText("Введите корректный email");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        if (password.isEmpty() || password.length() < 4) {
            tvError.setText("Пароль должен быть минимум 4 символа");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            tvError.setText("Пароли не совпадают");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        tvError.setVisibility(View.GONE);
        setLoading(true);

        new Thread(() -> {
            // Проверяем логин
            int loginExists = AppDatabase.getInstance(this).userDao()
                    .isLoginExists(login);

            if (loginExists > 0) {
                runOnUiThread(() -> {
                    setLoading(false);
                    tvError.setText("Логин уже занят");
                    tvError.setVisibility(View.VISIBLE);
                });
                return;
            }

            // Проверяем email
            int emailExists = AppDatabase.getInstance(this).userDao().isEmailExists(email);

            if (emailExists > 0) {
                runOnUiThread(() -> {
                    setLoading(false);
                    tvError.setText("Email уже зарегистрирован");
                    tvError.setVisibility(View.VISIBLE);
                });
                return;
            }

            // Создаём пользователя
            String passwordHash = HashUtils.sha256(password);
            User newUser = new User(login, passwordHash, email);

            long userId = AppDatabase.getInstance(this).userDao().insert(newUser);

            // если это первый пользователь — делаем админом
            int adminCount = AppDatabase.getInstance(this).userDao().getAdminCount();
            if (adminCount == 0 && userId > 0) {
                AppDatabase.getInstance(this).userDao()
                        .setUserRole(userId, UserConstants.ROLE_ADMIN);
            }

            final long finalUserId = userId;
            runOnUiThread(() -> {
                setLoading(false);

                if (finalUserId > 0) {
                    sessionManager.saveSession(finalUserId);
                    Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                } else {
                    tvError.setText("Ошибка регистрации");
                    tvError.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
        btnBackToLogin.setEnabled(!isLoading);
        etLogin.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        etConfirmPassword.setEnabled(!isLoading);
    }
}