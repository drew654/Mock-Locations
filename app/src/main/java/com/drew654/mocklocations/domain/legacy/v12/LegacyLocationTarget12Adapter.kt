package com.drew654.mocklocations.domain.legacy.v12

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class LegacyLocationTarget12Adapter : JsonSerializer<LegacyLocationTarget12>, JsonDeserializer<LegacyLocationTarget12> {
    override fun serialize(
        src: LegacyLocationTarget12?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        val jsonObject = JsonObject()
        val typeName = when (src) {
            is LegacyLocationTarget12.SinglePoint -> "SinglePoint"
            is LegacyLocationTarget12.Route -> "Route"
            is LegacyLocationTarget12.SavedRoute -> "SavedRoute"
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
    ): LegacyLocationTarget12? {
        val jsonObject = json?.asJsonObject

        val typeName = jsonObject?.get("type")?.asString
        val data = jsonObject?.get("data")

        return when (typeName) {
            "SinglePoint" -> context?.deserialize(data, LegacyLocationTarget12.SinglePoint::class.java)
            "Route" -> context?.deserialize(data, LegacyLocationTarget12.Route::class.java)
            "SavedRoute" -> context?.deserialize(data, LegacyLocationTarget12.SavedRoute::class.java)
            else -> LegacyLocationTarget12.Empty
        }
    }
}
