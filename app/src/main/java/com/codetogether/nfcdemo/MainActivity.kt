package com.codetogether.nfcdemo

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Surface
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
import java.io.IOException

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private var currentTag: Tag? = null  // Lưu thẻ hiện tại khi phát hiện

    private var writeMode: Boolean by mutableStateOf(false)  // Trạng thái ghi dữ liệu
    private var dataToWrite: String by mutableStateOf("")    // Dữ liệu cần ghi

    private var nfcMessage: String by mutableStateOf("Đưa thẻ NFC vào vùng đọc")  // Trạng thái hiển thị thông điệp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lấy NFC Adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Kiểm tra xem NFC có được hỗ trợ hay không
        if (nfcAdapter == null) {
            // Thiết bị không hỗ trợ NFC
            setContent {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Text(text = "Thiết bị không hỗ trợ NFC")
                }
            }
        }

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                NFCReaderWriter(
                    message = nfcMessage,
                    onStartWrite = { data ->
                        dataToWrite = data // Cập nhật dữ liệu cần ghi
                        writeMode = true   // Chuyển sang chế độ ghi
                        nfcMessage = "Đưa thẻ NFC vào vùng đọc để ghi..."  // Cập nhật giao diện
                    },
                    writeMode = writeMode,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableNfcForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        disableNfcForegroundDispatch()
    }

    private fun enableNfcForegroundDispatch() {
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    private fun disableNfcForegroundDispatch() {
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        tag?.let {
            currentTag = it  // Lưu thẻ hiện tại
            if (writeMode) {
                // Thực hiện ghi dữ liệu vào thẻ
                val success = writeNfcTag(currentTag, dataToWrite)
                nfcMessage = if (success) "Ghi dữ liệu thành công!" else "Ghi dữ liệu thất bại!"
                writeMode = false  // Quay lại chế độ đọc
            } else {
                // Đọc thẻ nếu không ở chế độ ghi
                val nfcContent = readNfcTag(it) ?: "Không thể đọc dữ liệu từ thẻ"
                nfcMessage = nfcContent  // Cập nhật thông điệp trên giao diện
            }
        }
    }
}

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


fun readNfcTag(tag: Tag): String? {
    val ndef = Ndef.get(tag) ?: return null
    return try {
        ndef.connect()
        val message = ndef.ndefMessage ?: return "Thẻ không chứa dữ liệu NDEF"
        val records = message.records
        val payload = String(records[0].payload, Charsets.UTF_8)
        Log.d("NFC", "Dữ liệu từ thẻ: $payload")
        if (records.isNotEmpty()) {
            // Trả về nội dung của bản ghi đầu tiên và bỏ code ngôn ngữ
            payload.subSequence(3, records[0].payload.size).toString()
        } else {
            Log.d("NFC", "Thẻ không có bản ghi dữ liệu")
            "Thẻ không có bản ghi dữ liệu"
        }
    } catch (e: IOException) {
        Log.d("NFC", "Không thể đọc dữ liệu từ thẻ")
        "Không thể đọc dữ liệu từ thẻ"
    } finally {
        ndef.close()
    }
}

fun writeNfcTag(tag: Tag?, data: String): Boolean {
    tag ?: return false  // Nếu không có thẻ, trả về false
    val ndef = Ndef.get(tag) ?: return false

    return try {
        ndef.connect()
        if (!ndef.isWritable) {
            return false
        }

        val message = NdefMessage(
            arrayOf(NdefRecord.createTextRecord("en", data))
        )

        ndef.writeNdefMessage(message)
        true
    } catch (e: IOException) {
        false
    } finally {
        ndef.close()
    }
}

@Preview
@Composable
fun NFCReaderWriterPreview() {
    NFCReaderWriter(message = "Đưa thẻ NFC vào vùng đọc", onStartWrite = {})
}