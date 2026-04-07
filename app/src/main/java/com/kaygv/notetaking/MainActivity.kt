package com.kaygv.notetaking

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableLongStateOf
import com.kaygv.notetaking.ui.navigation.AppNavGraph
import com.kaygv.notetaking.ui.theme.NoteTakingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val openNoteIdState = mutableLongStateOf(-1L)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        openNoteIdState.longValue =
            intent.getLongExtra("open_note_id", -1L)

        setContent {
            NoteTakingTheme {
                AppNavGraph(
                    openNoteId = openNoteIdState.longValue
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        openNoteIdState.longValue =
            intent.getLongExtra("open_note_id", -1L)
    }
}