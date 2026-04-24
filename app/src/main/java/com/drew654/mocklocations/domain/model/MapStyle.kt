package com.drew654.mocklocations.domain.model

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.drew654.mocklocations.R
import com.google.maps.android.compose.MapType

sealed class MapStyle(
    val resourceId: Int,
    val name: String,
    val polyLineStroke: Color,
    val mapType: MapType
) {
    object Standard : MapStyle(
        resourceId = R.raw.map_style_standard,
        name = "Standard",
        polyLineStroke = lightColorScheme().onSurface,
        mapType = MapType.NORMAL
    )

    object Night : MapStyle(
        resourceId = R.raw.map_style_night,
        name = "Night",
        polyLineStroke = darkColorScheme().onSurface,
        mapType = MapType.NORMAL
    )

    object Satellite : MapStyle(
        resourceId = R.raw.map_style_standard,
        name = "Satellite",
        polyLineStroke = darkColorScheme().onSurface,
        mapType = MapType.SATELLITE
    )

    object Hybrid : MapStyle(
        resourceId = R.raw.map_style_standard,
        name = "Hybrid",
        polyLineStroke = darkColorScheme().onSurface,
        mapType = MapType.HYBRID
    )

    object Terrain : MapStyle(
        resourceId = R.raw.map_style_standard,
        name = "Terrain",
        polyLineStroke = lightColorScheme().onSurface,
        mapType = MapType.TERRAIN
    )

    object Aubergine : MapStyle(
        resourceId = R.raw.map_style_aubergine,
        name = "Aubergine",
        polyLineStroke = darkColorScheme().onSurface,
        mapType = MapType.NORMAL
    )

    object Dark : MapStyle(
        resourceId = R.raw.map_style_dark,
        name = "Dark",
        polyLineStroke = darkColorScheme().onSurface,
        mapType = MapType.NORMAL
    )

    object Retro : MapStyle(
        resourceId = R.raw.map_style_retro,
        name = "Retro",
        polyLineStroke = lightColorScheme().onSurface,
        mapType = MapType.NORMAL
    )

    object Silver : MapStyle(
        resourceId = R.raw.map_style_silver,
        name = "Silver",
        polyLineStroke = lightColorScheme().onSurface,
        mapType = MapType.NORMAL
    )
}

fun getMapStyleByName(name: String): MapStyle? {
    return when (name) {
        MapStyle.Standard.name -> MapStyle.Standard
        MapStyle.Night.name -> MapStyle.Night
        MapStyle.Satellite.name -> MapStyle.Satellite
        MapStyle.Hybrid.name -> MapStyle.Hybrid
        MapStyle.Terrain.name -> MapStyle.Terrain
        MapStyle.Aubergine.name -> MapStyle.Aubergine
        MapStyle.Dark.name -> MapStyle.Dark
        MapStyle.Retro.name -> MapStyle.Retro
        MapStyle.Silver.name -> MapStyle.Silver
        else -> null
    }
}
