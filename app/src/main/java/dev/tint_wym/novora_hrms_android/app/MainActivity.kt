package dev.tint_wym.novora_hrms_android.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NovoraTheme(dynamicColor = false) {
                NovoraApp()
            }
        }
    }
}
