package com.drew654.mocklocations.domain.legacy.v14

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class LegacyLocationTarget14Adapter : JsonSerializer<LegacyLocationTarget14>, JsonDeserializer<LegacyLocationTarget14> {
    override fun serialize(
        src: LegacyLocationTarget14?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        val typeName = when (src) {
            is LegacyLocationTarget14.SinglePoint -> "SinglePoint"
            is LegacyLocationTarget14.Route -> "Route"
            is LegacyLocationTarget14.SavedRoute -> "SavedRoute"
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
    ): LegacyLocationTarget14? {
        val jsonObject = json?.asJsonObject

        val typeName = jsonObject?.get("type")?.asString
        val data = jsonObject?.get("data")

        return when (typeName) {
            "SinglePoint" -> context?.deserialize(data, LegacyLocationTarget14.SinglePoint::class.java)
            "Route" -> context?.deserialize(data, LegacyLocationTarget14.Route::class.java)
            "SavedRoute" -> context?.deserialize(data, LegacyLocationTarget14.SavedRoute::class.java)
            else -> LegacyLocationTarget14.Empty
        }
    }
}
