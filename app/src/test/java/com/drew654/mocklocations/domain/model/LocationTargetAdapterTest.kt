package com.drew654.mocklocations.domain.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LocationTargetAdapterTest {
    private lateinit var gson: Gson

    @Before
    fun setUp() {
        gson = GsonBuilder()
            .registerTypeAdapter(LocationTarget::class.java, LocationTargetAdapter())
            .create()
    }

    @Test
    fun `serialize and deserialize Empty location target`() {
        val locationTarget: LocationTarget = LocationTarget.Empty
        val json = gson.toJson(locationTarget, LocationTarget::class.java)

        assertTrue(json.contains("\"type\":\"Empty\""))

        val deserialized = gson.fromJson(json, LocationTarget::class.java)
        assertEquals(LocationTarget.Empty, deserialized)
    }

    @Test
    fun `serialize and deserialize SinglePoint location target`() {
        val point = LatLng(30.613716193675007, -96.33953779935837)
        val locationTarget: LocationTarget = LocationTarget.SinglePoint(point)

        val json = gson.toJson(locationTarget, LocationTarget::class.java)
        assertTrue(json.contains("\"type\":\"SinglePoint\""))

        val deserialized = gson.fromJson(json, LocationTarget::class.java)
        assertTrue(deserialized is LocationTarget.SinglePoint)
        assertEquals(locationTarget, deserialized)
    }

    @Test
    fun `serialize and deserialize Route location target`() {
        val routeSegments = listOf(
            RouteSegment(points = listOf(LatLng(30.613716193675007, -96.33953779935837))),
            RouteSegment(points = listOf(LatLng(30.61254844344945, -96.33846625685692))),
            RouteSegment(points = listOf(LatLng(30.61186111760771, -96.33693136274815))),
            RouteSegment(points = listOf(LatLng(30.609536523531006, -96.33468501269817)))
        )
        val locationTarget: LocationTarget = LocationTarget.Route(routeSegments)

        val json = gson.toJson(locationTarget, LocationTarget::class.java)
        assertTrue(json.contains("\"type\":\"Route\""))

        val deserialized = gson.fromJson(json, LocationTarget::class.java)
        assertTrue(deserialized is LocationTarget.Route)
        assertEquals(locationTarget, deserialized)
    }

    @Test
    fun `serialize and deserialize SavedRoute location target`() {
        val locationTarget = LocationTarget.SavedRoute(
            name = "Trigon to MAC",
            routeSegments = listOf(
                RouteSegment(points = listOf(LatLng(30.613716193675007, -96.33953779935837))),
                RouteSegment(points = listOf(LatLng(30.61254844344945, -96.33846625685692))),
                RouteSegment(points = listOf(LatLng(30.61186111760771, -96.33693136274815))),
                RouteSegment(points = listOf(LatLng(30.609536523531006, -96.33468501269817)))
            )
        )

        val json = gson.toJson(locationTarget, LocationTarget::class.java)
        assertTrue(json.contains("\"type\":\"SavedRoute\""))
        assertTrue(json.contains("\"name\":\"Trigon to MAC\""))

        val deserialized = gson.fromJson(json, LocationTarget::class.java)
        assertTrue(deserialized is LocationTarget.SavedRoute)
        assertEquals("Trigon to MAC", (deserialized as LocationTarget.SavedRoute).name)
        assertEquals(locationTarget, deserialized)
    }

    @Test
    fun `deserialize with unknown type returns Empty`() {
        val json = "{\"type\":\"UnknownType\",\"data\":{}}"
        val deserialized = gson.fromJson(json, LocationTarget::class.java)
        assertEquals(LocationTarget.Empty, deserialized)
    }
}
