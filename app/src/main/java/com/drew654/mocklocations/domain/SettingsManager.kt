package com.drew654.mocklocations.domain

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val CLEAR_POINTS_ON_STOP = booleanPreferencesKey("clear_points_on_stop")
        val SPEED_METERS_PER_SEC = doublePreferencesKey("speed_meters_per_sec")
    }

    val clearPointsOnStopFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CLEAR_POINTS_ON_STOP] ?: true
    }

    suspend fun setClearPointsOnStop(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CLEAR_POINTS_ON_STOP] = enabled
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
}
