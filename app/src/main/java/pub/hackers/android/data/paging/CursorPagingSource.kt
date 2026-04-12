package pub.hackers.android.data.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import pub.hackers.android.data.repository.HackersPubRepository

/**
 * Shape common to every cursor-based paginated endpoint in this codebase.
 * Adapter functions below translate the repository's result types into this.
 */
data class CursorPage<T>(
    val items: List<T>,
    val endCursor: String?,
    val hasNextPage: Boolean,
)

/**
 * Generic, repository-agnostic PagingSource for cursor-based APIs.
 * Callers provide a `fetch` lambda that takes a cursor and returns a page.
 */
class CursorPagingSource<T : Any>(
    private val fetch: suspend (after: String?) -> Result<CursorPage<T>>,
) : PagingSource<String, T>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, T> {
        val after = params.key // null on first page
        return fetch(after).fold(
            onSuccess = { page ->
                LoadResult.Page(
                    data = page.items,
                    prevKey = null, // forward-only feed
                    nextKey = if (page.hasNextPage) page.endCursor else null,
                )
            },
            onFailure = { LoadResult.Error(it) },
        )
    }

    // Forward-only feeds always refresh from head; we never seek into the middle.
    override fun getRefreshKey(state: PagingState<String, T>): String? = null
}

/**
 * Convenience factory — wraps [CursorPagingSource] with our standard [PagingConfig].
 *
 * - pageSize 20 matches the server's GraphQL `first: 20`.
 * - prefetchDistance 20 (one full page ahead) triggers the next fetch early
 *   enough that fast scrolling rarely stalls waiting for network.
 * - initialLoadSize 30 (1.5 pages) balances first-response latency against
 *   scroll buffer. Response payloads for this schema are large (~15 KB per
 *   post), so a smaller first fetch keeps cold-start snappy while
 *   prefetchDistance=20 starts page 2 almost immediately.
 * - placeholders disabled (existing UI has no skeleton support).
 */
fun <T : Any> cursorPager(
    pageSize: Int = 20,
    prefetchDistance: Int = 20,
    initialLoadSize: Int = 30,
    fetch: suspend (String?) -> Result<CursorPage<T>>,
): Pager<String, T> = Pager(
    config = PagingConfig(
        pageSize = pageSize,
        prefetchDistance = prefetchDistance,
        enablePlaceholders = false,
        initialLoadSize = initialLoadSize,
    ),
    pagingSourceFactory = { CursorPagingSource(fetch) },
)

// region Repository adapters
// Each paginated endpoint gets a one-liner converter so ViewModels can call
// `cursorPager { repository.foo(it) }` without repeating the `.map { CursorPage(...) }` boilerplate.

suspend fun HackersPubRepository.notificationsPage(after: String?) =
    getNotifications(after = after, refresh = (after == null))
        .map { CursorPage(it.notifications, it.endCursor, it.hasNextPage) }

suspend fun HackersPubRepository.personalTimelinePage(after: String?) =
    getPersonalTimeline(after = after, refresh = (after == null))
        .map { CursorPage(it.posts, it.endCursor, it.hasNextPage) }

suspend fun HackersPubRepository.publicTimelinePage(after: String?) =
    getPublicTimeline(after = after, refresh = (after == null))
        .map { CursorPage(it.posts, it.endCursor, it.hasNextPage) }

suspend fun HackersPubRepository.localTimelinePage(after: String?) =
    getLocalTimeline(after = after, refresh = (after == null))
        .map { CursorPage(it.posts, it.endCursor, it.hasNextPage) }

suspend fun HackersPubRepository.postRepliesPage(postId: String, after: String?) =
    getPostReplies(postId, after)
        .map { CursorPage(it.posts, it.endCursor, it.hasNextPage) }

suspend fun HackersPubRepository.actorPostsPage(handle: String, after: String?) =
    getActorPosts(handle, after)
        .map { CursorPage(it.posts, it.endCursor, it.hasNextPage) }

suspend fun HackersPubRepository.actorNotesPage(handle: String, after: String?) =
    getActorNotes(handle, after)
        .map { CursorPage(it.posts, it.endCursor, it.hasNextPage) }

suspend fun HackersPubRepository.actorArticlesPage(handle: String, after: String?) =
    getActorArticles(handle, after)
        .map { CursorPage(it.posts, it.endCursor, it.hasNextPage) }

// endregion
