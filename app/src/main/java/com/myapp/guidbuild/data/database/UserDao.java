package com.myapp.guidbuild.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

@Dao//Data Access Object
public interface UserDao {//доступ к данным пользователей
    @Insert//добавление User в таблицу, возвращение ID
    long insert(User user);
    @Query("SELECT * FROM users WHERE login = :login")//поиск по логину
    User getUserByLogin(String login);

    @Query("SELECT * FROM users WHERE login = :login AND passwordHash = :passwordHash")//по логину и паролю
    User login(String login, String passwordHash);

    @Query("SELECT COUNT(*) FROM users WHERE login = :login")//число пользователей с таким логином
    int isLoginExists(String login);

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int isEmailExists(String email);

    @Query("UPDATE users SET role = :role WHERE id = :userId")//обновление роли
    void setUserRole(long userId, int role);

    @Query("UPDATE users SET avatarPath = :avatarPath WHERE id = :userId")//обновление аватарки
    void updateAvatarPath(long userId, String avatarPath);

    @Query("SELECT * FROM users WHERE id = :userId")//поиск по id
    User getUserById(long userId);

    @Query("SELECT COUNT(*) FROM guides WHERE userId = :userId")
    int getUserGuidesCount(long userId);

    @Query("SELECT SUM(likesCount) FROM guides WHERE userId = :userId")
    int getUserTotalLikes(long userId);

    @Query("SELECT COUNT(*) FROM users WHERE role = 1")
    int getAdminCount();

    @Query("SELECT * FROM users")
    List<User> getAllUsersSync();

    @Query("UPDATE users SET status = :status, banReason = :banReason, banDate = :banDate WHERE id = :userId")
    void updateUserStatus(long userId, int status, String banReason, long banDate);

    @Delete//удаление
    void delete(User user);
}