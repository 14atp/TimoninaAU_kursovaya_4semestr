package com.myapp.guidbuild.data.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites")
public class Favorite {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public long guideId;

    public Favorite(long userId, long guideId) {
        this.userId = userId;
        this.guideId = guideId;
    }
}