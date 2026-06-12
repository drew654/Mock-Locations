package com.drew654.mocklocations.util

import com.drew654.mocklocations.domain.legacy.v14.LegacyLocationTarget14
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.RouteSegment
import com.drew654.mocklocations.util.MigrationUtils.migrateSavedRouteTo15
import com.drew654.mocklocations.util.MigrationUtils.migrateSavedRoutesJsonTo15
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test

class MigrationUtilsTest {
    @Test
    fun `migrateSavedRouteTo15 returns SavedRoute`() {
        val legacySavedRoute14 = LegacyLocationTarget14.SavedRoute(
            name = "Trigon to MAC",
            points = listOf(
                LatLng(30.613716193675007, -96.33953779935837),
                LatLng(30.61254844344945, -96.33846625685692),
                LatLng(30.61186111760771, -96.33693136274815),
                LatLng(30.609536523531006, -96.33468501269817)
            )
        )
        val actual = migrateSavedRouteTo15(route = legacySavedRoute14)
        val expected = LocationTarget.SavedRoute(
            name = "Trigon to MAC",
            routeSegments = listOf(
                RouteSegment(points = listOf(LatLng(30.613716193675007, -96.33953779935837))),
                RouteSegment(points = listOf(LatLng(30.61254844344945, -96.33846625685692))),
                RouteSegment(points = listOf(LatLng(30.61186111760771, -96.33693136274815))),
                RouteSegment(points = listOf(LatLng(30.609536523531006, -96.33468501269817)))
            )
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `migrateSavedRoutesJsonTo15 returns migrated JSON string`() {
        val json14String = "[{\"name\":\"ILCB to Zachry\",\"points\":[{\"latitude\":30.612306062097367,\"longitude\":-96.34331602603197},{\"latitude\":30.612524493890483,\"longitude\":-96.34303774684668},{\"latitude\":30.61356932396127,\"longitude\":-96.34143009781837},{\"latitude\":30.61593969890262,\"longitude\":-96.34330227971077},{\"latitude\":30.616381162201414,\"longitude\":-96.34344309568405},{\"latitude\":30.616450699700923,\"longitude\":-96.34303774684668},{\"latitude\":30.618988062928008,\"longitude\":-96.33933864533901},{\"latitude\":30.620379343031093,\"longitude\":-96.34055402129889},{\"latitude\":30.6206470943526,\"longitude\":-96.34038034826517}]},{\"name\":\"Trigon to MAC\",\"points\":[{\"latitude\":30.613716193675007,\"longitude\":-96.33953779935837},{\"latitude\":30.61254844344945,\"longitude\":-96.33846625685692},{\"latitude\":30.61186111760771,\"longitude\":-96.33693136274815},{\"latitude\":30.609536523531006,\"longitude\":-96.33468501269817}]},{\"name\":\"Reed Arena to UCG\",\"points\":[{\"latitude\":30.606806150741036,\"longitude\":-96.34598951786757},{\"latitude\":30.60733797637694,\"longitude\":-96.34496558457613},{\"latitude\":30.608358911817472,\"longitude\":-96.34590335190296},{\"latitude\":30.612421770413736,\"longitude\":-96.3398626819253},{\"latitude\":30.61148253806638,\"longitude\":-96.33895710110664}]}]"
        val actualJsonString = migrateSavedRoutesJsonTo15(json14String)
        val actualElement = JsonParser.parseString(actualJsonString)

        val json15String = "[{\"name\":\"ILCB to Zachry\",\"routeSegments\":[{\"points\":[{\"latitude\":30.612306062097367,\"longitude\":-96.34331602603197}]},{\"points\":[{\"latitude\":30.612524493890483,\"longitude\":-96.34303774684668}]},{\"points\":[{\"latitude\":30.61356932396127,\"longitude\":-96.34143009781837}]},{\"points\":[{\"latitude\":30.61593969890262,\"longitude\":-96.34330227971077}]},{\"points\":[{\"latitude\":30.616381162201414,\"longitude\":-96.34344309568405}]},{\"points\":[{\"latitude\":30.616450699700923,\"longitude\":-96.34303774684668}]},{\"points\":[{\"latitude\":30.618988062928008,\"longitude\":-96.33933864533901}]},{\"points\":[{\"latitude\":30.620379343031093,\"longitude\":-96.34055402129889}]},{\"points\":[{\"latitude\":30.6206470943526,\"longitude\":-96.34038034826517}]}]},{\"name\":\"Trigon to MAC\",\"routeSegments\":[{\"points\":[{\"latitude\":30.613716193675007,\"longitude\":-96.33953779935837}]},{\"points\":[{\"latitude\":30.61254844344945,\"longitude\":-96.33846625685692}]},{\"points\":[{\"latitude\":30.61186111760771,\"longitude\":-96.33693136274815}]},{\"points\":[{\"latitude\":30.609536523531006,\"longitude\":-96.33468501269817}]}]},{\"name\":\"Reed Arena to UCG\",\"routeSegments\":[{\"points\":[{\"latitude\":30.606806150741036,\"longitude\":-96.34598951786757}]},{\"points\":[{\"latitude\":30.60733797637694,\"longitude\":-96.34496558457613}]},{\"points\":[{\"latitude\":30.608358911817472,\"longitude\":-96.34590335190296}]},{\"points\":[{\"latitude\":30.612421770413736,\"longitude\":-96.3398626819253}]},{\"points\":[{\"latitude\":30.61148253806638,\"longitude\":-96.33895710110664}]}]}]"
        val expectedElement = JsonParser.parseString(json15String)

        assertEquals(expectedElement, actualElement)
    }
}
