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
import com.myapp.guidbuild.ui.main.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etLogin, etPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;
    private TextView tvError;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        // Если уже авторизован - сразу в главную
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (login.isEmpty()) {
            tvError.setText("Введите логин");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        if (password.isEmpty()) {
            tvError.setText("Введите пароль");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        tvError.setVisibility(View.GONE);
        setLoading(true);

        new Thread(() -> {
            String passwordHash = HashUtils.sha256(password);
            User user = AppDatabase.getInstance(this).userDao()
                    .login(login, passwordHash);

            runOnUiThread(() -> {
                setLoading(false);

                if (user != null) {
                    // Проверка на блокировку
                    if (user.status == 1) { // STATUS_BANNED
                        String message = "Аккаунт заблокирован\n";
                        if (user.banReason != null && !user.banReason.isEmpty()) {
                            message += "Причина: " + user.banReason;
                        }
                        tvError.setText(message);
                        tvError.setVisibility(View.VISIBLE);
                        return;
                    }

                    sessionManager.saveSession(user.id);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    tvError.setText("Неверный логин или пароль");
                    tvError.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        btnRegister.setEnabled(!isLoading);
        etLogin.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
    }
}