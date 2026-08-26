package com.apex.dms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apex.dms.data.AppStore
import com.apex.dms.ui.navigation.DmsApp
import com.apex.dms.ui.theme.ApexDmsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApexDmsTheme {
                val store: AppStore = viewModel()
                DmsApp(store)
            }
        }
    }
}
