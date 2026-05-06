package com.drew654.mocklocations.domain.model

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocationTargetTest {
    private val point1 = LatLng(30.613716193675007, -96.33953779935837)
    private val point2 = LatLng(30.61254844344945, -96.33846625685692)
    private val segment1 = RouteSegment(listOf(point1))
    private val segment2 = RouteSegment(listOf(point1, point2))
    private val trigonToMacRoute = LocationTarget.SavedRoute(
        name = "Trigon to MAC",
        routeSegments = listOf(
            RouteSegment(points = listOf(LatLng(30.613716193675007, -96.33953779935837))),
            RouteSegment(points = listOf(LatLng(30.61254844344945, -96.33846625685692))),
            RouteSegment(points = listOf(LatLng(30.61186111760771, -96.33693136274815))),
            RouteSegment(points = listOf(LatLng(30.609536523531006, -96.33468501269817)))
        )
    )
    private val academicPlazaToZachryToEastLawn = LocationTarget.SavedRoute(
        name = "MSC to Zachry to East Lawn",
        routeSegments = listOf(
            RouteSegment(
                points = listOf(
                    LatLng(30.61505849751468, -96.34181801229715)
                )
            ),
            RouteSegment(
                points = listOf(
                    LatLng(30.614743, -96.342352),
                    LatLng(30.614746, -96.342355),
                    LatLng(30.614796, -96.342398),
                    LatLng(30.614828, -96.342425),
                    LatLng(30.61504, -96.342604),
                    LatLng(30.61541, -96.342912),
                    LatLng(30.615719, -96.343174),
                    LatLng(30.615806, -96.343228),
                    LatLng(30.615862, -96.34327),
                    LatLng(30.615882, -96.343295),
                    LatLng(30.615904, -96.343335),
                    LatLng(30.615913, -96.34338),
                    LatLng(30.615996, -96.343383),
                    LatLng(30.616191, -96.343392),
                    LatLng(30.61624, -96.343395),
                    LatLng(30.616279, -96.343396),
                    LatLng(30.616402, -96.343402),
                    LatLng(30.6164, -96.343335),
                    LatLng(30.616407, -96.343251),
                    LatLng(30.616426, -96.343157),
                    LatLng(30.616447, -96.343082),
                    LatLng(30.616468, -96.343021),
                    LatLng(30.616513, -96.342939),
                    LatLng(30.616661, -96.342733),
                    LatLng(30.616718, -96.342652),
                    LatLng(30.616826, -96.342501),
                    LatLng(30.616871, -96.342437),
                    LatLng(30.617042, -96.342196),
                    LatLng(30.617189, -96.341985),
                    LatLng(30.617262, -96.34188),
                    LatLng(30.617755, -96.341211),
                    LatLng(30.617794, -96.341158),
                    LatLng(30.617829, -96.341101),
                    LatLng(30.617966, -96.340898),
                    LatLng(30.618295, -96.340407),
                    LatLng(30.618472, -96.340144),
                    LatLng(30.61884, -96.33961),
                    LatLng(30.618977, -96.339414),
                    LatLng(30.619021, -96.339351),
                    LatLng(30.619074, -96.339397),
                    LatLng(30.619369, -96.339653),
                    LatLng(30.619531, -96.339794),
                    LatLng(30.619621, -96.339879),
                    LatLng(30.619668, -96.339921),
                    LatLng(30.620441, -96.340626),
                    LatLng(30.620757, -96.340913)
                )
            ),
            RouteSegment(
                points = listOf(
                    LatLng(30.620757, -96.340913),
                    LatLng(30.620441, -96.340626),
                    LatLng(30.619668, -96.339921),
                    LatLng(30.619621, -96.339879),
                    LatLng(30.619531, -96.339794),
                    LatLng(30.619369, -96.339653),
                    LatLng(30.619074, -96.339397),
                    LatLng(30.619021, -96.339351),
                    LatLng(30.619061, -96.339293),
                    LatLng(30.619458, -96.338712),
                    LatLng(30.619542, -96.33859),
                    LatLng(30.619631, -96.338461),
                    LatLng(30.619726, -96.338332),
                    LatLng(30.61977, -96.338235),
                    LatLng(30.619907, -96.338035),
                    LatLng(30.620005, -96.337888),
                    LatLng(30.620058, -96.337809),
                    LatLng(30.620071, -96.337789),
                    LatLng(30.620079, -96.337775),
                    LatLng(30.620133, -96.337682),
                    LatLng(30.620167, -96.337618),
                    LatLng(30.620198, -96.337547),
                    LatLng(30.620227, -96.337474),
                    LatLng(30.620231, -96.337464),
                    LatLng(30.620239, -96.337444),
                    LatLng(30.620256, -96.337398),
                    LatLng(30.620279, -96.337325),
                    LatLng(30.620284, -96.337307),
                    LatLng(30.620309, -96.337207),
                    LatLng(30.620329, -96.337107),
                    LatLng(30.620345, -96.337011),
                    LatLng(30.620357, -96.336914),
                    LatLng(30.620364, -96.336839),
                    LatLng(30.62037, -96.336758),
                    LatLng(30.620371, -96.336643),
                    LatLng(30.620366, -96.336546),
                    LatLng(30.62036, -96.33644),
                    LatLng(30.620351, -96.33636),
                    LatLng(30.620346, -96.336324),
                    LatLng(30.620338, -96.336268),
                    LatLng(30.620318, -96.336165),
                    LatLng(30.620308, -96.336124),
                    LatLng(30.620294, -96.336067),
                    LatLng(30.620258, -96.335946),
                    LatLng(30.620217, -96.33583),
                    LatLng(30.62018, -96.335739),
                    LatLng(30.62014, -96.335652),
                    LatLng(30.620102, -96.335574),
                    LatLng(30.620058, -96.335499),
                    LatLng(30.620013, -96.33543),
                    LatLng(30.619963, -96.335357),
                    LatLng(30.619882, -96.335248),
                    LatLng(30.61982, -96.335179),
                    LatLng(30.619788, -96.335144),
                    LatLng(30.619756, -96.335111),
                    LatLng(30.619686, -96.335042),
                    LatLng(30.61965, -96.33501)
                )
            )
        )
    )

    @Test
    fun `Companion create returns correct type based on segment count`() {
        assertEquals(LocationTarget.Empty, LocationTarget.create(emptyList()))

        val singleResult = LocationTarget.create(listOf(segment1))
        assertTrue(singleResult is LocationTarget.SinglePoint)
        assertEquals(point1, (singleResult as LocationTarget.SinglePoint).point)

        val routeResult = LocationTarget.create(listOf(segment1, segment2))
        assertTrue(routeResult is LocationTarget.Route)
    }

    @Test
    fun `isRoute returns true only for Route and SavedRoute`() {
        assertFalse(LocationTarget.Empty.isRoute())
        assertFalse(LocationTarget.SinglePoint(point1).isRoute())
        assertTrue(LocationTarget.Route(listOf(segment2)).isRoute())
        assertTrue(LocationTarget.SavedRoute("Test", listOf(segment2)).isRoute())
    }

    @Test
    fun `getLastPoint returns correct coordinates`() {
        assertNull(LocationTarget.Empty.getLastPoint())
        assertEquals(point1, LocationTarget.SinglePoint(point1).getLastPoint())

        val route = LocationTarget.Route(listOf(segment1, segment2))
        assertEquals(point2, route.getLastPoint())

        assertEquals(LatLng(30.61965, -96.33501), academicPlazaToZachryToEastLawn.getLastPoint())
    }

    @Test
    fun `getAllPoints returns flattened list`() {
        assertTrue(LocationTarget.Empty.getAllPoints().isEmpty())

        val single = LocationTarget.SinglePoint(point1)
        assertEquals(listOf(point1), single.getAllPoints())

        val route = LocationTarget.Route(listOf(segment1, segment2))
        assertEquals(3, route.getAllPoints().size)
        assertEquals(point2, route.getAllPoints().last())
        assertEquals(listOf(point1, point1, point2), route.getAllPoints())

        val savedRoute = LocationTarget.SavedRoute("Test", listOf(segment1, segment2))
        assertEquals(3, savedRoute.getAllPoints().size)
        assertEquals(point2, route.getAllPoints().last())
        assertEquals(listOf(point1, point1, point2), savedRoute.getAllPoints())
    }

    @Test
    fun `Route getDistance calculates distance correctly`() {
        val distKm = academicPlazaToZachryToEastLawn.getDistance(SpeedUnit.KilometersPerHour)
        assertEquals(1.747045497, distKm, 0.001)

        val distMi = academicPlazaToZachryToEastLawn.getDistance(SpeedUnit.MilesPerHour)
        assertEquals(1.085563743, distMi, 0.001)
    }

    @Test
    fun `getDistanceText returns formatted string with units`() {
        val textKm = trigonToMacRoute.getDistanceText(SpeedUnit.KilometersPerHour)
        assertTrue(textKm.endsWith(" km"))

        val textMs = trigonToMacRoute.getDistanceText(SpeedUnit.MetersPerSecond)
        assertTrue(textMs.endsWith(" km"))

        val textMi = trigonToMacRoute.getDistanceText(SpeedUnit.MilesPerHour)
        assertTrue(textMi.endsWith(" mi"))
    }
}
