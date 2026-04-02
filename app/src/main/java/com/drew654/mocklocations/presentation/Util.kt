package com.drew654.mocklocations.presentation

import android.location.Location
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import com.drew654.mocklocations.domain.model.RoutePoint
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class NoRippleInteractionSource : MutableInteractionSource {
    override val interactions: Flow<Interaction> = emptyFlow()
    override suspend fun emit(interaction: Interaction) {}
    override fun tryEmit(interaction: Interaction) = true
}

fun mToMiles(m: Double) = m * 0.000621371

fun mToKm(m: Double) = m / 1000.0

fun Float.toTrimmedString(): String {
    val symbols = DecimalFormatSymbols(Locale.ROOT)
    val decimalFormat = DecimalFormat("#.##", symbols)
    return decimalFormat.format(this)
}

fun Float.round(decimals: Int = 2): Float = "%.${decimals}f".format(this).toFloat()

fun Location.toRoutePoint(): RoutePoint {
    return RoutePoint(
        latLng = LatLng(latitude, longitude),
        bearing = bearing
    )
}

fun Location.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

fun Modifier.conditional(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}
