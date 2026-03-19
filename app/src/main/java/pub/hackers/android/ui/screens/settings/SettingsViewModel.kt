package pub.hackers.android.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.apolloStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pub.hackers.android.data.local.PreferencesManager
import pub.hackers.android.data.local.SessionManager
import pub.hackers.android.data.repository.HackersPubRepository
import javax.inject.Inject

data class SettingsUiState(
    val userName: String? = null,
    val userHandle: String? = null,
    val userAvatar: String? = null,
    val appVersion: String = "",
    val isSignedOut: Boolean = false,
    val message: String? = null,
    val confirmBeforeDelete: Boolean = true,
    val confirmBeforeShare: Boolean = false,
    val timelineMaxLength: Int = 0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val repository: HackersPubRepository,
    private val apolloClient: ApolloClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
        loadAppVersion()
        loadPreferences()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            val name = sessionManager.userName.first()
            val handle = sessionManager.userHandle.first()
            val avatar = sessionManager.userAvatar.first()

            _uiState.update {
                it.copy(
                    userName = name,
                    userHandle = handle,
                    userAvatar = avatar
                )
            }
        }
    }

    private fun loadAppVersion() {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val version = packageInfo.versionName ?: "Unknown"
            val versionCode = packageInfo.longVersionCode
            _uiState.update { it.copy(appVersion = "$version ($versionCode)") }
        } catch (_: Exception) {
            _uiState.update { it.copy(appVersion = "Unknown") }
        }
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            val confirmDelete = preferencesManager.confirmBeforeDelete.first()
            val confirmShare = preferencesManager.confirmBeforeShare.first()
            val maxLength = preferencesManager.timelineMaxLength.first()

            _uiState.update {
                it.copy(
                    confirmBeforeDelete = confirmDelete,
                    confirmBeforeShare = confirmShare,
                    timelineMaxLength = maxLength
                )
            }
        }
    }

    fun setConfirmBeforeDelete(value: Boolean) {
        _uiState.update { it.copy(confirmBeforeDelete = value) }
        viewModelScope.launch { preferencesManager.setConfirmBeforeDelete(value) }
    }

    fun setConfirmBeforeShare(value: Boolean) {
        _uiState.update { it.copy(confirmBeforeShare = value) }
        viewModelScope.launch { preferencesManager.setConfirmBeforeShare(value) }
    }

    fun setTimelineMaxLength(value: Int) {
        _uiState.update { it.copy(timelineMaxLength = value) }
        viewModelScope.launch { preferencesManager.setTimelineMaxLength(value) }
    }

    fun signOut() {
        viewModelScope.launch {
            val sessionId = sessionManager.sessionToken.first()
            if (sessionId != null) {
                repository.revokeSession(sessionId)
            }
            sessionManager.clearSession()
            apolloClient.apolloStore.clearAll()
            _uiState.update { it.copy(isSignedOut = true) }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            try {
                apolloClient.apolloStore.clearAll()
                _uiState.update { it.copy(message = "Cache cleared") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Failed to clear cache") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
