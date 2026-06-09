package com.myapp.guidbuild.data.database;

import androidx.room.TypeConverter;

public class Converters {//конвертер для дат
    @TypeConverter
    public static long fromDate(Long date) {
        return date == null ? 0 : date;
    }

    @TypeConverter
    public static Long toDate(long timestamp) {
        return timestamp;
    }
}