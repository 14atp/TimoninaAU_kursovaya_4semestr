package com.myapp.guidbuild.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

@Dao
public interface LikeDao {
    @Insert
    void insert(Like like);

    @Delete
    void delete(Like like);

    @Query("SELECT COUNT(*) FROM likes WHERE userId = :userId AND guideId = :guideId")
    int isLiked(long userId, long guideId);

    @Query("DELETE FROM likes WHERE userId = :userId AND guideId = :guideId")
    void deleteByUserAndGuide(long userId, long guideId);
}