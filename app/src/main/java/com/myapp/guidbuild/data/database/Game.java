package com.myapp.guidbuild.data.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "games")
public class Game {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;

    public Game(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;   //
    }
}