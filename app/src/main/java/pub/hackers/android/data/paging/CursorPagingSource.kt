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
 * Defaults match existing manual pagination behavior:
 * - pageSize 20 matches the server's GraphQL `first: 20`
 * - prefetchDistance 3 matches the existing "load more when within 3 from end" threshold
 * - initialLoadSize 20 avoids the default 3*pageSize (60) first-page fetch
 * - placeholders disabled (existing UI has no skeleton support)
 */
fun <T : Any> cursorPager(
    pageSize: Int = 20,
    prefetchDistance: Int = 3,
    fetch: suspend (String?) -> Result<CursorPage<T>>,
): Pager<String, T> = Pager(
    config = PagingConfig(
        pageSize = pageSize,
        prefetchDistance = prefetchDistance,
        enablePlaceholders = false,
        initialLoadSize = pageSize,
    ),
    pagingSourceFactory = { CursorPagingSource(fetch) },
)

// region Repository adapters
// Each paginated endpoint gets a one-liner converter so ViewModels can call
// `cursorPager { repository.foo(it) }` without repeating the `.map { CursorPage(...) }` boilerplate.

suspend fun HackersPubRepository.notificationsPage(after: String?) =
    getNotifications(after = after, refresh = (after == null))
        .map { CursorPage(it.notifications, it.endCursor, it.hasNextPage) }

// endregion
