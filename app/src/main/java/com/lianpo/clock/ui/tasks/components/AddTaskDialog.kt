package com.lianpo.clock.ui.tasks.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddTaskDialog(
    onConfirm: (title: String, description: String, targetPomodoroCount: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetPomodoroCount by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加任务") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("任务标题") },
                    singleLine = true,
                    modifier = Modifier
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("任务描述") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetPomodoroCount,
                    onValueChange = { targetPomodoroCount = it.filter { c -> c.isDigit() } },
                    label = { Text("目标番茄钟数量") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val count = targetPomodoroCount.toIntOrNull() ?: 1
                    onConfirm(title.trim(), description.trim(), count.coerceAtLeast(1))
                },
                enabled = title.isNotBlank()
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = modifier
    )
}
