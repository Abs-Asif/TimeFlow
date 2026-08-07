package com.dev.timeflow.View.utils.componets

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TaskTile(
    modifier: Modifier = Modifier,
    taskName: String,
    taskDescription: String?,
    taskTime: Long,
    taskIsCompleted: Boolean,
    taskImportance: String,
    taskNotification: Boolean,
    onUpdateTask: (Boolean) -> Unit,
    onClick : () -> Unit
) {
    var animate by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = if (animate) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "TileScale"
    )
    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(
                RoundedCornerShape(16.dp)
            )
            .clickable(
                onClick = {
                    onClick.invoke()
                }
            )
            .padding(
                vertical = 8.dp
            ),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            NewCheckBox(
                isSelected = taskIsCompleted
            ) {
                scope.launch {
                    animate = true
                    delay(200)
                    animate = false
                }
                onUpdateTask.invoke(it)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                modifier = modifier
                    .weight(1f)
                    .padding(
                        end = 4.dp,
                        top = 8.dp // align nicely with the top-aligned checkbox
                    ),
                text = taskName,
                textDecoration = if (taskIsCompleted) TextDecoration.LineThrough else TextDecoration.None,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Justify
            )
        }
    }
}
