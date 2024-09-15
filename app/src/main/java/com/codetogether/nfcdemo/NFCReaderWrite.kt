package com.codetogether.nfcdemo

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun NFCReaderWriter(
    message: String,
    onStartWrite: (String) -> Unit,
    writeMode: Boolean = false,
) {
    var inputText by remember { mutableStateOf("") }  // Dữ liệu nhập vào thẻ
    var isWriting by remember { mutableStateOf(writeMode) }  // Trạng thái ghi dữ liệu
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = if (isWriting) "Chế độ ghi" else "Chế độ đọc",
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = message,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )  // Hiển thị message truyền vào
        Spacer(modifier = Modifier.height(16.dp))

        if (isWriting) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Nội dung ghi vào thẻ") },
                enabled = !writeMode
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // Ẩn bàn phím khi nhấn nút ghi
                    val inputMethodManager =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(
                        (context as Activity).currentFocus?.windowToken,
                        0
                    )

                    onStartWrite(inputText)
                },
                enabled = inputText.isNotBlank() && !writeMode
            ) {
                Text(text = "Bắt đầu ghi")
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                isWriting = !isWriting
            },
            enabled = !writeMode
        ) {
            if (isWriting) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Chuyển sang chế độ đọc")
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Chuyển sang chế độ ghi")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun NFCReaderWriterPreview() {
    NFCReaderWriter(message = "Đưa thẻ NFC vào vùng đọc", onStartWrite = {})
}