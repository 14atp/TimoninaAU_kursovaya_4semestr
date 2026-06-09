package com.myapp.guidbuild.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface GameDao {
    @Insert
    long insert(Game game);

    @Query("SELECT * FROM games ORDER BY name")
    List<Game> getAllGames();

    @Query("SELECT * FROM games WHERE id = :gameId")
    Game getGameById(long gameId);

    @Query("SELECT name FROM games WHERE id = :gameId")
    String getGameNameById(long gameId);
}