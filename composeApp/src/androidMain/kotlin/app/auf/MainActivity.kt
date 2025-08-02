package app.auf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Pass the API key from BuildConfig to the StateManager.
        val stateManager = StateManager(BuildConfig.OPENAI_API_KEY)
        stateManager.loadCatalogue()

        setContent {
            App(stateManager)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    // For the preview, a dummy key can be used as it won't be making real calls.
    App(StateManager("dummy_preview_key"))
}