package com.kaygv.notetaking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kaygv.notetaking.ui.navigation.AppNavGraph
import com.kaygv.notetaking.ui.theme.NoteTakingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val openNoteId = intent.getLongExtra("open_note_id", -1L)

        setContent {
            NoteTakingTheme {
                AppNavGraph(
                    openNoteId = openNoteId
                )
            }
        }
    }
}

