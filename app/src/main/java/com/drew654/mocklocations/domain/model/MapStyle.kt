package com.drew654.mocklocations.domain.model

import com.drew654.mocklocations.R

sealed class MapStyle(val resourceId: Int, val name: String) {
    object Aubergine : MapStyle(R.raw.map_style_aubergine, "Aubergine")
    object Dark : MapStyle(R.raw.map_style_dark, "Dark")
    object Night : MapStyle(R.raw.map_style_night, "Night")
    object Retro : MapStyle(R.raw.map_style_retro, "Retro")
    object Silver : MapStyle(R.raw.map_style_silver, "Silver")
    object Standard : MapStyle(R.raw.map_style_standard, "Standard")
}

fun getMapStyleByName(name: String): MapStyle? {
    return when (name) {
        MapStyle.Aubergine.name -> MapStyle.Aubergine
        MapStyle.Dark.name -> MapStyle.Dark
        MapStyle.Night.name -> MapStyle.Night
        MapStyle.Retro.name -> MapStyle.Retro
        MapStyle.Silver.name -> MapStyle.Silver
        MapStyle.Standard.name -> MapStyle.Standard
        else -> null
    }
}
