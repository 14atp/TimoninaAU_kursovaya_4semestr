package com.myapp.guidbuild.data.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "likes")
public class Like {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public long guideId;

    public Like(long userId, long guideId) {
        this.userId = userId;
        this.guideId = guideId;
    }
}