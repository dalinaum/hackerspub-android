package pub.hackers.android.ui.screens.compose

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import pub.hackers.android.domain.model.Post
import javax.inject.Inject
import javax.inject.Singleton

data class ReplyPostedEvent(
    val replyTargetId: String,
    val reply: Post,
)

@Singleton
class ReplyPostedSignal @Inject constructor() {
    private val _events = MutableSharedFlow<ReplyPostedEvent>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<ReplyPostedEvent> = _events.asSharedFlow()

    fun emit(event: ReplyPostedEvent) {
        _events.tryEmit(event)
    }
}
