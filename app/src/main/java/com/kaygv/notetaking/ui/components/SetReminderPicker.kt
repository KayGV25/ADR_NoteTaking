package com.kaygv.notetaking.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetReminderPicker(
    initialTime: Long? = null,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = remember {
        Calendar.getInstance().apply {
            initialTime?.let { timeInMillis = it }
        }
    }

    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis
    )

    val timeState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE)
    )

    var selectedHour by remember { mutableIntStateOf(timeState.hour) }
    var selectedMinute by remember { mutableIntStateOf(timeState.minute) }

    var showTimePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(4.dp)
        ) {
            Crossfade(showTimePicker) { isTime ->
                if (isTime) {
                    Column {
                        TimePicker(
                            initialHour = selectedHour,
                            initialMinute = selectedMinute,
                            onTimeSelected = { hour, minute ->
                                selectedHour = hour
                                selectedMinute = minute
                            }
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Back",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        showTimePicker = false
                                    },
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "Set Reminder",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        val selectedDate = dateState.selectedDateMillis
                                            ?: return@clickable

                                        val cal = Calendar.getInstance().apply {
                                            timeInMillis = selectedDate
                                            set(Calendar.HOUR_OF_DAY, selectedHour)
                                            set(Calendar.MINUTE, selectedMinute)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }

                                        onConfirm(cal.timeInMillis)
                                        showTimePicker = false
                                    },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Column {
                        DatePicker(
                            state = dateState,
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        onDismiss()
                                    },
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "Next",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        showTimePicker = true
                                    },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}