package pub.hackers.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val CONFIRM_BEFORE_DELETE = booleanPreferencesKey("confirm_before_delete")
        private val CONFIRM_BEFORE_SHARE = booleanPreferencesKey("confirm_before_share")
        private val TIMELINE_MAX_LENGTH = intPreferencesKey("timeline_max_length")
    }

    val confirmBeforeDelete: Flow<Boolean> = context.preferencesDataStore.data.map { prefs ->
        prefs[CONFIRM_BEFORE_DELETE] ?: true
    }

    val confirmBeforeShare: Flow<Boolean> = context.preferencesDataStore.data.map { prefs ->
        prefs[CONFIRM_BEFORE_SHARE] ?: false
    }

    val timelineMaxLength: Flow<Int> = context.preferencesDataStore.data.map { prefs ->
        prefs[TIMELINE_MAX_LENGTH] ?: 0 // 0 = unlimited
    }

    suspend fun setConfirmBeforeDelete(value: Boolean) {
        context.preferencesDataStore.edit { prefs ->
            prefs[CONFIRM_BEFORE_DELETE] = value
        }
    }

    suspend fun setConfirmBeforeShare(value: Boolean) {
        context.preferencesDataStore.edit { prefs ->
            prefs[CONFIRM_BEFORE_SHARE] = value
        }
    }

    suspend fun setTimelineMaxLength(value: Int) {
        context.preferencesDataStore.edit { prefs ->
            prefs[TIMELINE_MAX_LENGTH] = value
        }
    }
}
