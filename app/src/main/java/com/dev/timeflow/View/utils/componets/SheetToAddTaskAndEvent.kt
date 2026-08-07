package com.dev.timeflow.View.utils.componets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetToAddEventAndTask(
    modifier: Modifier = Modifier,
    onDismiss : () -> Unit,
    isButtonEnabled : Boolean,
    onTaskSave : () -> Unit,
    onTaskNameChange : (String) -> Unit,
    taskName : String
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        ),
        onDismissRequest = {
            onDismiss.invoke()
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 36.dp)
        ) {
            Text(
                text = "Add New Task",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = taskName,
                onValueChange = {
                    onTaskNameChange.invoke(it)
                },
                label = { Text("Task name") },
                placeholder = { Text("What needs to be done?") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (isButtonEnabled) {
                            onTaskSave.invoke()
                            onDismiss.invoke()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                enabled = isButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    onTaskSave.invoke()
                    onDismiss.invoke()
                }
            ) {
                Text(
                    text = "Save Task",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
