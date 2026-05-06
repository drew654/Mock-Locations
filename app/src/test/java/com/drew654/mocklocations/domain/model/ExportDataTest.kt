package com.drew654.mocklocations.domain.model

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test

class ExportDataTest {
    @Test
    fun `data class properties are correctly initialized`() {
        val meta = ExportMeta(
            appVersionName = "0.5.0-alpha.1",
            appVersionCode = 16,
            exportedAt = "2026-05-06T13:22:01.836019Z"
        )
        val settings = ExportSettings(
            useCrosshairs = true,
            clearRouteOnStop = false,
            cameraFollowsMockedLocation = true,
            mapStyle = null,
            expandedControlsSpeedUnitValue = SpeedUnitValue(
                value = 20.64767837524414,
                speedUnit = SpeedUnit.MilesPerHour
            ),
            expandedControlsSpeedSliderLowerEnd = 0,
            expandedControlsSpeedSliderUpperEnd = 100,
            waitAtRouteFinish = false,
            locationAccuracyLevel = LocationAccuracyLevel.Perfect.name,
            locationUpdateDelay = 1.0f
        )
        val routes = listOf(
            LocationTarget.SavedRoute(
                name = "ILCB to Zachry",
                routeSegments = listOf(
                    RouteSegment(
                        points = listOf(
                            LatLng(30.612306062097367, -96.34331602603197)
                        )
                    ),
                    RouteSegment(
                        points = listOf(
                            LatLng(30.612524493890483, -96.34303774684668)
                        )
                    ),
                    RouteSegment(
                        points = listOf(
                            LatLng(30.61356932396127, -96.34143009781837)
                        )
                    ),
                    RouteSegment(
                        points = listOf(
                            LatLng(30.61593969890262, -96.34330227971077)
                        )
                    ),
                    RouteSegment(
                        points = listOf(
                            LatLng(30.616381162201414, -96.34344309568405)
                        )
                    ),
                    RouteSegment(
                        points = listOf(
                            LatLng(30.616450699700923, -96.34303774684668)
                        )
                    ),
                    RouteSegment(
                        points = listOf(
                            LatLng(30.618988062928008, -96.33933864533901)
                        )
                    ),
                    RouteSegment(
                        points = listOf(
                            LatLng(30.620379343031093, -96.34055402129889)
                        )
                    ),
                    RouteSegment(
                        points = listOf(
                            LatLng(30.6206470943526, -96.34038034826517)
                        )
                    )
                )
            ),
            LocationTarget.SavedRoute(
                name = "Trigon to MAC",
                routeSegments = listOf(
                    RouteSegment(
                        points = listOf(
                            LatLng(30.613716193675007, -96.33953779935837)
                        )
                    ),
                    RouteSegment(
                        points = listOf(
                            LatLng(30.61254844344945, -96.33846625685692)
                        )
                    ),
                    RouteSegment(
                        points = listOf(
                            LatLng(30.61186111760771, -96.33693136274815)
                        )
                    ),
                    RouteSegment(
                        points = listOf(
                            LatLng(30.609536523531006, -96.33468501269817)
                        )
                    )
                )
            ),
            LocationTarget.SavedRoute(
                name = "Reed Arena to UCG",
                routeSegments = listOf(
                    RouteSegment(
                        points = listOf(
                            LatLng(30.606806150741036, -96.34598951786757)
                        )
                    ),
                    RouteSegment(
                        points = listOf(
                            LatLng(30.60733797637694, -96.34496558457613)
                        )
                    ),
                    RouteSegment(
                        points = listOf(
                            LatLng(30.608358911817472, -96.34590335190296)
                        )
                    ),
                    RouteSegment(
                        points = listOf(
                            LatLng(30.612421770413736, -96.3398626819253)
                        )
                    ),
                    RouteSegment(
                        points = listOf(
                            LatLng(30.61148253806638, -96.33895710110664)
                        )
                    )
                )
            )
        )
        val exportData = ExportData(meta, settings, routes)

        assertEquals(meta, exportData.meta)
        assertEquals(settings, exportData.settings)
        assertEquals(routes, exportData.routes)
    }
}
