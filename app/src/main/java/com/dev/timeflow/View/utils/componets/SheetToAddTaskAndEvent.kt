package com.dev.timeflow.View.utils.componets

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Signature
import com.dev.timeflow.Data.Model.ImportanceChipModel
import com.dev.timeflow.Data.Model.SavingModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SheetToAddEventAndTask(
    modifier: Modifier = Modifier,
    onDismiss : () -> Unit,
    onSwitchState : (Boolean) -> Unit,
    onTimeState : (Boolean) -> Unit,
    onPermissionState : (Boolean) -> Unit,
    selectedSavingType : Int,
    isButtonEnabled : Boolean,
    timerState : TimePickerState,
    fromTimePickerState : TimePickerState,
    toTimePickerState: TimePickerState,
    fromDatePickerState: DatePickerState,
    toDatePickerState: DatePickerState,
    onFromTimePicker: () -> Unit,
    onToTimePicker : () -> Unit,
    onTaskSave : () -> Unit,
    onEventSave : () -> Unit,
    onFromTileClick : () -> Unit,
    onToTileClick : () -> Unit,
    onSelectedImportantChipChange : (Int) -> Unit,
    onTaskNameChange : (String) -> Unit,
    onTaskDescriptionChange : (String) -> Unit,
    changeSavingType: (Int) -> Unit,
    savingChipList : List<SavingModel>,
    importanceChip : List<ImportanceChipModel>,
    hapticFeedback: HapticFeedback,
    switchState : Boolean,
    showTimeState : Boolean,
    taskName : String,
    taskDescription : String,
    selectedImportantChip : Int,
) {
    val localContext = LocalContext.current

    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        ),
        onDismissRequest = {
            onDismiss.invoke()
        }
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp
                ),
            horizontalArrangement = Arrangement.End
        ) {
            ToggleButton(
                colors = ToggleButtonDefaults.toggleButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                checked = switchState,
                onCheckedChange = {
                    onSwitchState.invoke(
                        it
                    )
                    if (it) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                    localContext,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            if (!hasPermission) {
                                onSwitchState.invoke(false)
                                onTimeState.invoke(false)
                                Toast.makeText(
                                    localContext,
                                    "Please grant notification permission to enable reminders",
                                    Toast.LENGTH_LONG
                                ).show()
                                onPermissionState.invoke(true)
                            } else {
                                onTimeState.invoke(
                                    true
                                )
                            }
                        } else {
                            onTimeState.invoke(true)
                        }
                    } else {
                        onTimeState.invoke(false)
                    }

                    hapticFeedback.performHapticFeedback(
                        hapticFeedbackType = HapticFeedbackType.Confirm
                    )
                }
            ) {
                Icon(
                    imageVector = Lucide.Bell,
                    contentDescription = null
                )
            }
        }
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            TextField(
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Lucide.Signature,
                        contentDescription = null
                    )
                },
                placeholder = {
                    Text(
                        text = "Task name"
                    )
                },
                value = taskName,
                onValueChange = {
                    onTaskNameChange.invoke(it)
                },
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                )
            )

            Spacer(
                modifier = modifier.height(8.dp)
            )

            AnimatedContent(
                targetState = switchState,
                transitionSpec = {
                    scaleIn() togetherWith scaleOut()
                },
                label = "SwitchStateAnimation"
            ) {
                if (it) {
                    Button(
                        modifier = modifier.fillMaxWidth(),
                        onClick = {
                            onTimeState.invoke(
                                !showTimeState
                            )
                        }
                    ) {
                        Text(
                            text = "${
                                LocalTime.of(timerState.hour, timerState.minute).format(
                                    DateTimeFormatter.ofPattern("hh : mm a")
                                )
                            }"
                        )
                    }
                }
            }
            Button(
                enabled = isButtonEnabled,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 8.dp
                    ),
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    onTaskSave.invoke()
                    onDismiss.invoke()
                }
            ) {
                Text(
                    modifier = modifier.padding(
                        vertical = 8.dp
                    ),
                    text = "Save"
                )
            }
        }
    }
}
