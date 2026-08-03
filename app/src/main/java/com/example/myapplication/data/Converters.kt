package com.example.myapplication.data

import androidx.room.TypeConverter
import com.example.myapplication.model.JourneyEventType
import com.example.myapplication.model.JourneyRarity

class Converters {
    @TypeConverter
    fun fromJourneyEventType(value: JourneyEventType): String {
        return value.name
    }

    @TypeConverter
    fun toJourneyEventType(value: String): JourneyEventType {
        return JourneyEventType.valueOf(value)
    }

    @TypeConverter
    fun fromJourneyRarity(value: JourneyRarity): String {
        return value.name
    }

    @TypeConverter
    fun toJourneyRarity(value: String): JourneyRarity {
        return JourneyRarity.valueOf(value)
    }
}
