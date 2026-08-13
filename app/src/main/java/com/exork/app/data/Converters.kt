package com.exork.app.data

import androidx.room.TypeConverter
import com.exork.app.model.JourneyEventType
import com.exork.app.model.JourneyRarity

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
