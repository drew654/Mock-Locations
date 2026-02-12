package com.drew654.mocklocations.domain.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class LocationTargetAdapter : JsonSerializer<LocationTarget>, JsonDeserializer<LocationTarget> {
    override fun serialize(
        src: LocationTarget?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        val jsonObject = JsonObject()
        val typeName = when (src) {
            is LocationTarget.SinglePoint -> "SinglePoint"
            is LocationTarget.Route -> "Route"
            is LocationTarget.SavedRoute -> "SavedRoute"
            else -> "Empty"
        }

        jsonObject.addProperty("type", typeName)
        jsonObject.add("data", context?.serialize(src))
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocationTarget? {
        val jsonObject = json?.asJsonObject

        val typeName = jsonObject?.get("type")?.asString
        val data = jsonObject?.get("data")

        return when (typeName) {
            "SinglePoint" -> context?.deserialize(data, LocationTarget.SinglePoint::class.java)
            "Route" -> context?.deserialize(data, LocationTarget.Route::class.java)
            "SavedRoute" -> context?.deserialize(data, LocationTarget.SavedRoute::class.java)
            else -> LocationTarget.Empty
        }
    }
}
