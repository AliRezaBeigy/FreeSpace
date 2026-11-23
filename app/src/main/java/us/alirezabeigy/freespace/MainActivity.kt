package us.alirezabeigy.freespace

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import us.alirezabeigy.freespace.ui.FreeSpaceApp
import us.alirezabeigy.freespace.ui.theme.FreeSpaceTheme
import us.alirezabeigy.freespace.viewmodel.OptimizerViewModel

class MainActivity : ComponentActivity() {
    private val optimizerViewModel by viewModels<OptimizerViewModel>()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isTablet = resources.getBoolean(R.bool.is_tablet)
        if (!isTablet) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        enableEdgeToEdge()

        setContent {
            FreeSpaceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FreeSpaceApp(optimizerViewModel)
                }
            }
        }
    }
}
