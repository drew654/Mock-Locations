package com.drew654.mocklocations.domain.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class SpeedUnitTypeAdapter : JsonSerializer<SpeedUnit>, JsonDeserializer<SpeedUnit> {
    override fun serialize(
        src: SpeedUnit?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        return src?.let { JsonPrimitive(it.name) }
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): SpeedUnit? {
        if (json == null || json.isJsonNull) return null

        return when (json.asString) {
            SpeedUnit.MetersPerSecond.name -> SpeedUnit.MetersPerSecond
            SpeedUnit.KilometersPerHour.name -> SpeedUnit.KilometersPerHour
            SpeedUnit.MilesPerHour.name -> SpeedUnit.MilesPerHour
            else -> throw JsonParseException("Unknown SpeedUnit: ${json.asString}")
        }
    }
}
