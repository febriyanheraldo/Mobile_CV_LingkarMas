package com.lingkarmas.app
import com.lingkarmas.app.data.Repo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.lingkarmas.app.ui.AppRoot
import com.lingkarmas.app.ui.theme.LingkarMasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Repo.init(applicationContext)
        setContent { LingkarMasTheme { AppRoot() } }
    }
}