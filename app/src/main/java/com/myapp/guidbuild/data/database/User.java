package com.myapp.guidbuild.data.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String login;//логин
    public String passwordHash;//пароль
    public String email;//почта
    public long registeredDate;//дата регистрации
    public int role;// 0 = обычный, 1 = админ
    public int status;// 0 = активен, 1 = заблокирован
    public String banReason;// причина блокировки (для сообщения пользователю)
    public long banDate;// когда заблокировали
    public String avatarPath;// аватарка
    public User(String login, String passwordHash, String email) {
        this.login = login;
        this.passwordHash = passwordHash;
        this.email = email;
        this.registeredDate = System.currentTimeMillis();//дата с 1970г в мс
        this.role = 0;//0-обычный, 1-админ
        this.status = 0;//0- активен, 1- заблокировали
        this.avatarPath = null; // без аватарки
    }
}