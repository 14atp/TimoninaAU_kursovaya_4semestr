package com.myapp.guidbuild.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface FavoriteDao {
    @Insert
    void insert(Favorite favorite);

    @Delete
    void delete(Favorite favorite);

    @Query("SELECT g.* FROM guides g " +
            "INNER JOIN favorites f ON g.id = f.guideId " +
            "WHERE f.userId = :userId")
    List<Guide> getFavoriteGuides(long userId);

    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId AND guideId = :guideId")
    int isFavorite(long userId, long guideId);

    @Query("DELETE FROM favorites WHERE userId = :userId AND guideId = :guideId")
    void deleteByUserAndGuide(long userId, long guideId);

    @Query("DELETE FROM favorites WHERE guideId = :guideId")
    void deleteByGuideId(long guideId);
}