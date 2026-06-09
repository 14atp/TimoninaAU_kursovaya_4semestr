package com.myapp.guidbuild.data.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface GuideDao {
    @Insert
    long insert(Guide guide);

    @Query("SELECT * FROM guides WHERE userId = :userId AND isDraft = 0 ORDER BY createdDate DESC")
    List<Guide> getGuidesByUser(long userId);

    @Query("SELECT COUNT(*) FROM guides WHERE userId = :userId")
    int getUserGuidesCount(long userId);

    @Query("SELECT SUM(likesCount) FROM guides WHERE userId = :userId")
    int getUserTotalLikes(long userId);

    @Query("SELECT * FROM guides WHERE isDraft = 0 ORDER BY createdDate DESC")
    List<Guide> getAllGuides();

    @Query("UPDATE guides SET likesCount = :likesCount WHERE id = :guideId")
    void updateLikesCount(long guideId, int likesCount);

    @Query("SELECT COUNT(*) FROM guides WHERE userId = :userId AND isDraft = 0")
    int getGuidesCountByUser(long userId);

    @Query("SELECT SUM(likesCount) FROM guides WHERE userId = :userId AND isDraft = 0")
    int getTotalLikesByUser(long userId);

    @Query("SELECT * FROM guides WHERE id = :guideId")
    Guide getGuideById(long guideId);

    @Delete
    void delete(Guide guide);

}