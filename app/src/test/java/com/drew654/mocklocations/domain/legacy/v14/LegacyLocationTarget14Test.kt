package com.drew654.mocklocations.domain.legacy.v14

import com.drew654.mocklocations.domain.model.SpeedUnit
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LegacyLocationTarget14Test {
    private val point1 = LatLng(30.613716193675007, -96.33953779935837)
    private val point2 = LatLng(30.61254844344945, -96.33846625685692)
    private val legacySavedRoute14TrigonToMac = LegacyLocationTarget14.SavedRoute(
        name = "Trigon to MAC",
        points = listOf(
            LatLng(30.613716193675007, -96.33953779935837),
            LatLng(30.61254844344945, -96.33846625685692),
            LatLng(30.61186111760771, -96.33693136274815),
            LatLng(30.609536523531006, -96.33468501269817)
        )
    )

    @Test
    fun `Companion create returns correct type based on points size`() {
        assertEquals(LegacyLocationTarget14.Empty, LegacyLocationTarget14.create(emptyList()))

        val singleResult = LegacyLocationTarget14.create(listOf(point1))
        assertTrue(singleResult is LegacyLocationTarget14.SinglePoint)
        assertEquals(point1, (singleResult as LegacyLocationTarget14.SinglePoint).point)

        val routeResult = LegacyLocationTarget14.create(listOf(point1, point2))
        assertTrue(routeResult is LegacyLocationTarget14.Route)
    }

    @Test
    fun `isRoute returns true only for Route and SavedRoute`() {
        assertFalse(LegacyLocationTarget14.Empty.isRoute())
        assertFalse(LegacyLocationTarget14.SinglePoint(point1).isRoute())
        assertTrue(LegacyLocationTarget14.Route(listOf(point1, point2)).isRoute())
        assertTrue(LegacyLocationTarget14.SavedRoute("Test", listOf(point1, point2)).isRoute())
    }

    @Test
    fun `Route getDistance calculates distance correctly`() {

        val actualKm = legacySavedRoute14TrigonToMac.getDistance(SpeedUnit.KilometersPerHour)
        val expectedKm = 0.6672318071

        assertEquals(expectedKm, actualKm, 0.001)

        val actualMi = legacySavedRoute14TrigonToMac.getDistance(SpeedUnit.MilesPerHour)
        val expectedMi = 0.414598623476398

        assertEquals(expectedMi, actualMi, 0.001)
    }

    @Test
    fun `getDistanceText returns formatted string with units`() {
        val textKm = legacySavedRoute14TrigonToMac.getDistanceText(SpeedUnit.KilometersPerHour)
        assertTrue(textKm.endsWith(" km"))

        val textMs = legacySavedRoute14TrigonToMac.getDistanceText(SpeedUnit.MetersPerSecond)
        assertTrue(textMs.endsWith(" km"))

        val textMi = legacySavedRoute14TrigonToMac.getDistanceText(SpeedUnit.MilesPerHour)
        assertTrue(textMi.endsWith(" mi"))
    }
}
