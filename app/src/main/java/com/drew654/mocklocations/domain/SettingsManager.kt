package com.drew654.mocklocations.domain

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.LocationTargetAdapter
import com.drew654.mocklocations.domain.model.MapStyle
import com.drew654.mocklocations.domain.model.RoutePoint
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.drew654.mocklocations.domain.model.SpeedUnitTypeAdapter
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import com.drew654.mocklocations.domain.model.getMapStyleByName
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val ACTIVE_LOCATION_TARGET_JSON = stringPreferencesKey("active_location_target_json")
        val IS_MOCKING = booleanPreferencesKey("is_mocking")
        val IS_PAUSED = booleanPreferencesKey("is_paused")
        val CURRENT_MOCKED_LOCATION_JSON = stringPreferencesKey("current_mocked_location_json")
        val CLEAR_ROUTE_ON_STOP = booleanPreferencesKey("clear_route_on_stop")
        val SAVED_ROUTES_JSON = stringPreferencesKey("saved_routes_json")
        val IS_USING_CROSSHAIRS = booleanPreferencesKey("is_using_crosshairs")
        val MAP_STYLE = stringPreferencesKey("map_style")
        val SPEED_UNIT_VALUE_JSON = stringPreferencesKey("speed_unit_value_json")
        val SPEED_SLIDER_UPPER_END = intPreferencesKey("speed_slider_upper_end")
        val SPEED_SLIDER_LOWER_END = intPreferencesKey("speed_slider_lower_end")
        val IS_CAMERA_FOLLOWING_MOCKED_LOCATION = booleanPreferencesKey("is_camera_following_mocked_location")
    }
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocationTarget::class.java, LocationTargetAdapter())
        .registerTypeAdapter(SpeedUnit::class.java, SpeedUnitTypeAdapter())
        .create()

    val activeLocationTargetFlow: Flow<LocationTarget> = context.dataStore.data.map { preferences ->
        val json = preferences[ACTIVE_LOCATION_TARGET_JSON] ?: ""
        if (json.isEmpty()) LocationTarget.Empty
        else gson.fromJson(json, LocationTarget::class.java)
    }

    suspend fun setActiveLocationTarget(target: LocationTarget) {
        context.dataStore.edit {
            it[ACTIVE_LOCATION_TARGET_JSON] = gson.toJson(target, LocationTarget::class.java)
        }
    }

    val isMockingFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_MOCKING] ?: false
    }

    suspend fun setIsMocking(isMocking: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_MOCKING] = isMocking
        }
    }

    val isPausedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_PAUSED] ?: false
    }

    suspend fun setIsPaused(isPaused: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_PAUSED] = isPaused
        }
    }

    suspend fun toggleIsPaused() {
        context.dataStore.edit { preferences ->
            val current = preferences[IS_PAUSED] ?: false
            preferences[IS_PAUSED] = !current
        }
    }

    val currentMockedLocationFlow: Flow<RoutePoint?> = context.dataStore.data.map { preferences ->
        val json = preferences[CURRENT_MOCKED_LOCATION_JSON] ?: ""
        if (json.isEmpty()) null
        else gson.fromJson(json, RoutePoint::class.java)
    }

    suspend fun setCurrentMockedLocation(location: RoutePoint?) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_MOCKED_LOCATION_JSON] = gson.toJson(location)
        }
    }

    val clearRouteOnStopFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CLEAR_ROUTE_ON_STOP] ?: true
    }

    suspend fun setClearRouteOnStop(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CLEAR_ROUTE_ON_STOP] = enabled
        }
    }

    val speedUnitValueFlow: Flow<SpeedUnitValue> = context.dataStore.data.map { preferences ->
        val json = preferences[SPEED_UNIT_VALUE_JSON] ?: ""
        if (json.isEmpty()) SpeedUnitValue(value = 30.0, speedUnit = SpeedUnit.MetersPerSecond)
        else gson.fromJson(json, SpeedUnitValue::class.java)
    }

    suspend fun setSpeedUnitValue(speedUnitValue: SpeedUnitValue) {
        context.dataStore.edit { preferences ->
            preferences[SPEED_UNIT_VALUE_JSON] = gson.toJson(speedUnitValue)
        }
    }

    val savedRoutesFlow: Flow<List<LocationTarget.SavedRoute>> =
        context.dataStore.data.map { preferences ->
            val json = preferences[SAVED_ROUTES_JSON] ?: ""
            if (json.isEmpty()) {
                emptyList()
            } else {
                try {
                    val type = object : TypeToken<List<LocationTarget.SavedRoute>>() {}.type
                    gson.fromJson(json, type)
                } catch (_: Exception) {
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
                } catch (_: Exception) {
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
                val currentList =
                    gson.fromJson<MutableList<LocationTarget.SavedRoute>>(existingJson, type)

                currentList.removeAll { it.name == route.name && it.points == route.points }

                preferences[SAVED_ROUTES_JSON] = gson.toJson(currentList)
            }
        }
    }

    val isUsingCrosshairsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_USING_CROSSHAIRS] ?: true
    }

    suspend fun setIsUsingCrosshairs(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_USING_CROSSHAIRS] = enabled
        }
    }

    val mapStyleFlow: Flow<MapStyle?> = context.dataStore.data.map { preferences ->
        getMapStyleByName(preferences[MAP_STYLE] ?: "")
    }

    suspend fun setMapStyle(mapStyle: MapStyle?) {
        context.dataStore.edit { preferences ->
            preferences[MAP_STYLE] = mapStyle?.name ?: ""
        }
    }

    val speedSliderUpperEndFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SPEED_SLIDER_UPPER_END] ?: 100
    }

    suspend fun setSpeedSliderUpperEnd(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[SPEED_SLIDER_UPPER_END] = value
        }
    }

    val speedSliderLowerEndFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SPEED_SLIDER_LOWER_END] ?: 0
    }

    suspend fun setSpeedSliderLowerEnd(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[SPEED_SLIDER_LOWER_END] = value
        }
    }

    val isCameraFollowingMockedLocation: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_CAMERA_FOLLOWING_MOCKED_LOCATION] ?: true
    }

    suspend fun setIsCameraFollowingMockedLocation(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_CAMERA_FOLLOWING_MOCKED_LOCATION] = value
        }
    }
}
