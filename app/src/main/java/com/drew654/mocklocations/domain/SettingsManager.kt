package com.drew654.mocklocations.domain

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.drew654.mocklocations.domain.model.LocationTarget
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val CLEAR_ROUTE_ON_STOP = booleanPreferencesKey("clear_route_on_stop")
        val SPEED_METERS_PER_SEC = doublePreferencesKey("speed_meters_per_sec")
        val SAVED_ROUTES_JSON = stringPreferencesKey("saved_routes_json")
        val USE_CROSSHAIRS = booleanPreferencesKey("use_crosshairs")
    }

    private val gson = Gson()

    val clearRouteOnStopFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CLEAR_ROUTE_ON_STOP] ?: true
    }

    suspend fun setClearRouteOnStop(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CLEAR_ROUTE_ON_STOP] = enabled
        }
    }

    val speedMetersPerSecFlow: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[SPEED_METERS_PER_SEC] ?: 30.0
    }

    suspend fun setSpeedMetersPerSec(speed: Double) {
        context.dataStore.edit { preferences ->
            preferences[SPEED_METERS_PER_SEC] = speed
        }
    }

    val savedRoutesFlow: Flow<List<LocationTarget.SavedRoute>> = context.dataStore.data
        .map { preferences ->
            val json = preferences[SAVED_ROUTES_JSON] ?: ""
            if (json.isEmpty()) {
                emptyList()
            } else {
                try {
                    val type = object : TypeToken<List<LocationTarget.SavedRoute>>() {}.type
                    gson.fromJson(json, type)
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }

    suspend fun saveRoute(route: LocationTarget.SavedRoute) {
        context.dataStore.edit { preferences ->
            val existingJson = preferences[SAVED_ROUTES_JSON] ?: ""
            val currentList = if (existingJson.isNotEmpty()) {
                try {
                    val type = object : TypeToken<MutableList<LocationTarget.SavedRoute>>() {}.type
                    gson.fromJson<MutableList<LocationTarget.SavedRoute>>(existingJson, type)
                } catch (e: Exception) {
                    mutableListOf()
                }
            } else {
                mutableListOf()
            }

            currentList.add(route)

            preferences[SAVED_ROUTES_JSON] = gson.toJson(currentList)
        }
    }

    suspend fun deleteRoute(route: LocationTarget.SavedRoute) {
        context.dataStore.edit { preferences ->
            val existingJson = preferences[SAVED_ROUTES_JSON] ?: ""
            if (existingJson.isNotEmpty()) {
                val type = object : TypeToken<MutableList<LocationTarget.SavedRoute>>() {}.type
                val currentList = gson.fromJson<MutableList<LocationTarget.SavedRoute>>(existingJson, type)

                currentList.removeAll { it.name == route.name && it.points == route.points }

                preferences[SAVED_ROUTES_JSON] = gson.toJson(currentList)
            }
        }
    }

    val useCrosshairsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_CROSSHAIRS] ?: true
    }

    suspend fun setUseCrosshairs(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_CROSSHAIRS] = enabled
        }
    }
}
