package com.drew654.mocklocations.domain.model

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.drew654.mocklocations.R

sealed class MapStyle(val resourceId: Int, val name: String, val polyLineStroke: Color) {
    object Aubergine : MapStyle(R.raw.map_style_aubergine, "Aubergine", darkColorScheme().onSurface)
    object Dark : MapStyle(R.raw.map_style_dark, "Dark", darkColorScheme().onSurface)
    object Night : MapStyle(R.raw.map_style_night, "Night", darkColorScheme().onSurface)
    object Retro : MapStyle(R.raw.map_style_retro, "Retro", lightColorScheme().onSurface)
    object Silver : MapStyle(R.raw.map_style_silver, "Silver", lightColorScheme().onSurface)
    object Standard : MapStyle(R.raw.map_style_standard, "Standard", lightColorScheme().onSurface)
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
