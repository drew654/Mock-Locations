package com.drew654.mocklocations.data.repository

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class RouteRepository() {
    suspend fun getRoutePoints(start: LatLng, end: LatLng): List<LatLng> =
        withContext(Dispatchers.IO) {
            try {
                val url =
                    "https://router.project-osrm.org/route/v1/driving/" +
                            "${start.longitude},${start.latitude};" +
                            "${end.longitude},${end.latitude}" +
                            "?overview=full&geometries=geojson"

                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = OkHttpClient().newCall(request).execute()

                if (!response.isSuccessful) return@withContext emptyList()

                val body = response.body?.string() ?: return@withContext emptyList()

                val json = JSONObject(body)

                val coordinates = json
                    .getJSONArray("routes")
                    .getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONArray("coordinates")

                buildList {
                    for (i in 0 until coordinates.length()) {
                        val point = coordinates.getJSONArray(i)

                        add(
                            LatLng(
                                point.getDouble(1),
                                point.getDouble(0)
                            )
                        )
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
}
