package pub.hackers.android.ui.screens.timeline

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimelineRefreshTrigger @Inject constructor() {
    private val _refreshAt = MutableStateFlow(0L)
    val refreshAt: StateFlow<Long> = _refreshAt.asStateFlow()

    fun requestRefresh() {
        _refreshAt.value = System.currentTimeMillis()
    }
}
