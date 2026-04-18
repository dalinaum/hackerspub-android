package pub.hackers.android.ui.screens.editprofile

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private const val MAX_AVATAR_EDGE_PX = 1024
private const val JPEG_QUALITY = 85

/**
 * Reads [uri] from [contentResolver], downscales to at most [MAX_AVATAR_EDGE_PX] on the longer
 * edge, re-encodes as JPEG, and returns a `data:image/jpeg;base64,...` URL.
 *
 * Matches the web client's flow: the server fetches the avatar from the data URL on
 * updateAccount. Runs on Dispatchers.IO because decode + re-encode is blocking.
 */
suspend fun encodeAvatarAsDataUrl(
    contentResolver: ContentResolver,
    uri: Uri,
): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
        val bitmap = contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Cannot open input stream for $uri" }
            BitmapFactory.decodeStream(input)
                ?: error("Failed to decode image")
        }

        val scaled = downscale(bitmap, MAX_AVATAR_EDGE_PX)
        val bytes = ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            out.toByteArray()
        }
        if (scaled !== bitmap) scaled.recycle()
        bitmap.recycle()

        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        "data:image/jpeg;base64,$base64"
    }
}

private fun downscale(source: Bitmap, maxEdge: Int): Bitmap {
    val longer = maxOf(source.width, source.height)
    if (longer <= maxEdge) return source
    val scale = maxEdge.toFloat() / longer
    val newWidth = (source.width * scale).toInt().coerceAtLeast(1)
    val newHeight = (source.height * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
}
