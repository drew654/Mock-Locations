package com.drew654.mocklocations.domain

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.drew654.mocklocations.domain.model.AccuracyLevel
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.domain.model.LocationTargetAdapter
import com.drew654.mocklocations.domain.model.MapStyle
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.RoutePoint
import com.drew654.mocklocations.domain.model.SpeedUnit
import com.drew654.mocklocations.domain.model.SpeedUnitTypeAdapter
import com.drew654.mocklocations.domain.model.SpeedUnitValue
import com.drew654.mocklocations.domain.model.getAccuracyLevelByName
import com.drew654.mocklocations.domain.model.getMapStyleByName
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        // Affects UI
        val MOCK_CONTROL_STATE_JSON = stringPreferencesKey("mock_control_state_json")

        // App data and status
        val CURRENT_MOCKED_LOCATION_JSON = stringPreferencesKey("current_mocked_location_json")

        // Saved routes
        val SAVED_ROUTES_JSON = stringPreferencesKey("saved_routes_json")

        // User preferences
        val CLEAR_ROUTE_ON_STOP = booleanPreferencesKey("clear_route_on_stop")
        val MAP_STYLE = stringPreferencesKey("map_style")
        val SPEED_UNIT_VALUE_JSON = stringPreferencesKey("speed_unit_value_json")
        val SPEED_SLIDER_UPPER_END = intPreferencesKey("speed_slider_upper_end")
        val SPEED_SLIDER_LOWER_END = intPreferencesKey("speed_slider_lower_end")
        val IS_CAMERA_FOLLOWING_MOCKED_LOCATION = booleanPreferencesKey("is_camera_following_mocked_location")
        val IS_CAMERA_CURRENTLY_FOLLOWING_MOCKED_LOCATION = booleanPreferencesKey("is_camera_currently_following_mocked_location")
        val IS_GOING_TO_WAIT_AT_ROUTE_FINISH = booleanPreferencesKey("is_going_to_wait_at_route_finish")
        val ACCURACY_LEVEL = stringPreferencesKey("accuracy_level")
        val LOCATION_UPDATE_DELAY = floatPreferencesKey("location_update_delay")
    }
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocationTarget::class.java, LocationTargetAdapter())
        .registerTypeAdapter(SpeedUnit::class.java, SpeedUnitTypeAdapter())
        .create()

    suspend fun resetToDefault() {
        setIsUsingCrosshairs(true)
        context.dataStore.edit { preferences ->
            preferences.remove(CLEAR_ROUTE_ON_STOP)
            preferences.remove(MAP_STYLE)
            preferences.remove(SPEED_UNIT_VALUE_JSON)
            preferences.remove(SPEED_SLIDER_UPPER_END)
            preferences.remove(SPEED_SLIDER_LOWER_END)
            preferences.remove(IS_CAMERA_FOLLOWING_MOCKED_LOCATION)
            preferences.remove(IS_CAMERA_CURRENTLY_FOLLOWING_MOCKED_LOCATION)
            preferences.remove(IS_GOING_TO_WAIT_AT_ROUTE_FINISH)
            preferences.remove(ACCURACY_LEVEL)
            preferences.remove(LOCATION_UPDATE_DELAY)
        }
    }

    val mockControlStateFlow: Flow<MockControlState> =
        context.dataStore.data.map { preferences ->
            val json = preferences[MOCK_CONTROL_STATE_JSON] ?: ""

            if (json.isEmpty()) {
                MockControlState()
            } else {
                try {
                    gson.fromJson(json, MockControlState::class.java)
                } catch (_: Exception) {
                    MockControlState()
                }
            }
        }

    suspend fun setMockControlState(state: MockControlState) {
        context.dataStore.edit { preferences ->
            preferences[MOCK_CONTROL_STATE_JSON] =
                gson.toJson(state, MockControlState::class.java)
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
        if (json.isEmpty()) SpeedUnitValue(value = 30.0, speedUnit = SpeedUnit.MilesPerHour)
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

    private fun generateUniqueRouteName(
        baseName: String,
        existingNames: Set<String>
    ): String {
        if (baseName !in existingNames) return baseName

        var index = 1
        while (true) {
            val newName = "$baseName ($index)"
            if (newName !in existingNames) return newName
            index++
        }
    }

    suspend fun mergeRoutes(routes: List<LocationTarget.SavedRoute>) {
        context.dataStore.edit { preferences ->
            val existingJson = preferences[SAVED_ROUTES_JSON] ?: ""
            if (existingJson.isEmpty()) {
                preferences[SAVED_ROUTES_JSON] = gson.toJson(routes)
            } else {
                val type = object : TypeToken<MutableList<LocationTarget.SavedRoute>>() {}.type
                val currentList = gson.fromJson<MutableList<LocationTarget.SavedRoute>>(existingJson, type)

                val existingNames = currentList.map { it.name }.toMutableSet()
                val updatedRoutes = routes.map { route ->
                    val uniqueName = generateUniqueRouteName(route.name, existingNames)
                    existingNames.add(uniqueName)
                    route.copy(name = uniqueName)
                }

                currentList.addAll(updatedRoutes)

                preferences[SAVED_ROUTES_JSON] = gson.toJson(currentList)
            }
        }
    }

    suspend fun replaceRoutes(routes: List<LocationTarget.SavedRoute>) {
        context.dataStore.edit { preferences ->
            preferences[SAVED_ROUTES_JSON] = gson.toJson(routes)
        }
    }

    private suspend fun setIsUsingCrosshairs(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            val current = preferences[MOCK_CONTROL_STATE_JSON]
                ?.let { gson.fromJson(it, MockControlState::class.java) }
                ?: MockControlState()

            val updated = current.copy(isUsingCrosshairs = enabled)

            preferences[MOCK_CONTROL_STATE_JSON] =
                gson.toJson(updated, MockControlState::class.java)
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

    val isCameraCurrentlyFollowingMockedLocationFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_CAMERA_CURRENTLY_FOLLOWING_MOCKED_LOCATION] ?: true
    }

    suspend fun setIsCameraCurrentlyFollowingMockedLocation(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_CAMERA_CURRENTLY_FOLLOWING_MOCKED_LOCATION] = value
        }
    }

    val isGoingToWaitAtRouteFinishFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_GOING_TO_WAIT_AT_ROUTE_FINISH] ?: false
    }

    suspend fun setIsGoingToWaitAtRouteFinish(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_GOING_TO_WAIT_AT_ROUTE_FINISH] = value
        }
    }

    val accuracyLevelFlow: Flow<AccuracyLevel> = context.dataStore.data.map { preferences ->
        getAccuracyLevelByName(preferences[ACCURACY_LEVEL] ?: AccuracyLevel.Perfect.name)!!
    }

    suspend fun setAccuracyLevel(accuracyLevel: AccuracyLevel) {
        context.dataStore.edit { preferences ->
            preferences[ACCURACY_LEVEL] = accuracyLevel.name
        }
    }

    val locationUpdateDelayFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[LOCATION_UPDATE_DELAY] ?: 1f
    }

    suspend fun setLocationUpdateDelay(value: Float) {
        context.dataStore.edit { preferences ->
            preferences[LOCATION_UPDATE_DELAY] = value
        }
    }
}
