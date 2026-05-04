package com.drew654.mocklocations.util

import com.drew654.mocklocations.domain.legacy.v14.LegacyLocationTarget14
import com.drew654.mocklocations.domain.legacy.v14.LegacyLocationTarget14Adapter
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.LocationTargetAdapter
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.drew654.mocklocations.domain.model.SpeedUnitTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder

object JsonUtils {
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocationTarget::class.java, LocationTargetAdapter())
        .registerTypeAdapter(SpeedUnit::class.java, SpeedUnitTypeAdapter())
        .registerTypeAdapter(LegacyLocationTarget14::class.java, LegacyLocationTarget14Adapter())
        .create()
}
