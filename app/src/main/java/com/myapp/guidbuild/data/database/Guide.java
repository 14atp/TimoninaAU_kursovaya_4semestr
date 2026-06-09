package com.myapp.guidbuild.data.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "guides")
public class Guide {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;          // автор
    public long gameId;          // игра
    public String title;
    public String content;       // JSON с блоками
    public long createdDate;
    public int likesCount;
    public int commentsCount;
    public int isDraft;          // 0=опубликован, 1=черновик
}