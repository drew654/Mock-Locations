package com.drew654.mocklocations.util

import com.drew654.mocklocations.domain.legacy.v14.LegacyLocationTarget14
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.RouteSegment
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class JsonUtilsTest {
    @Test
    fun `gson correctly serializes and deserializes SpeedUnit`() {
        val unit: SpeedUnit = SpeedUnit.MilesPerHour
        val json = JsonUtils.gson.toJson(unit, SpeedUnit::class.java)

        assertEquals("\"mph\"", json)

        val deserialized = JsonUtils.gson.fromJson(json, SpeedUnit::class.java)

        assertEquals(unit, deserialized)
    }

    @Test
    fun `gson correctly serializes and deserializes LocationTarget`() {
        val target: LocationTarget = LocationTarget.SinglePoint(LatLng(1.0, 2.0))
        val json = JsonUtils.gson.toJson(target, LocationTarget::class.java)

        assertTrue(json.contains("\"type\":\"SinglePoint\""))

        val deserialized = JsonUtils.gson.fromJson(json, LocationTarget::class.java)

        assertEquals(target, deserialized)
    }

    @Test
    fun `gson correctly serializes and deserializes LegacyLocationTarget14`() {
        val legacyTarget: LegacyLocationTarget14 = LegacyLocationTarget14.Route(listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0)))
        val json = JsonUtils.gson.toJson(legacyTarget, LegacyLocationTarget14::class.java)

        assertTrue(json.contains("\"type\":\"Route\""))

        val deserialized = JsonUtils.gson.fromJson(json, LegacyLocationTarget14::class.java)

        assertEquals(legacyTarget, deserialized)
    }

    @Test
    fun `gson handles complex LocationTarget Route`() {
        val segments = listOf(
            RouteSegment(listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0))),
            RouteSegment(listOf(LatLng(1.0, 1.0), LatLng(2.0, 2.0)))
        )
        val target: LocationTarget = LocationTarget.Route(segments)

        val json = JsonUtils.gson.toJson(target, LocationTarget::class.java)
        val deserialized = JsonUtils.gson.fromJson(json, LocationTarget::class.java)

        assertTrue(deserialized is LocationTarget.Route)
        assertEquals(2, (deserialized as LocationTarget.Route).routeSegments.size)
        assertEquals(target, deserialized)
    }
}
