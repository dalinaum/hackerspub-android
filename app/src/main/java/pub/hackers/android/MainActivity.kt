package pub.hackers.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import pub.hackers.android.ui.HackersPubApp
import pub.hackers.android.ui.theme.HackersPubTheme
import pub.hackers.android.ui.theme.LocalAppColors

data class DeepLinkData(
    val token: String,
    val code: String
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var deepLinkData by mutableStateOf<DeepLinkData?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
        enableEdgeToEdge()
        setContent {
            HackersPubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LocalAppColors.current.background
                ) {
                    HackersPubApp(deepLinkData = deepLinkData)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "hackerspub" && data.host == "verify") {
            val token = data.getQueryParameter("token")
            val code = data.getQueryParameter("code")
            if (token != null && code != null) {
                deepLinkData = DeepLinkData(token = token, code = code)
            }
        }
    }
}
