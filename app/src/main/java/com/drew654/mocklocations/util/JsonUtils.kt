package com.drew654.mocklocations.util

import com.drew654.mocklocations.domain.legacy.v12.LegacyLocationTarget12
import com.drew654.mocklocations.domain.legacy.v12.LegacyLocationTarget12Adapter
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
        .registerTypeAdapter(LegacyLocationTarget12::class.java, LegacyLocationTarget12Adapter())
        .create()
}
