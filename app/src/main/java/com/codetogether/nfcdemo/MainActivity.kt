package com.codetogether.nfcdemo

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.io.IOException

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var currentTag: Tag? = null  // Lưu thẻ hiện tại khi phát hiện

    // Biến trạng thái để theo dõi chế độ ghi dữ liệu
    private var writeMode: Boolean by mutableStateOf(false)
    private var dataToWrite: String by mutableStateOf("")  // Lưu nội dung sẽ ghi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lấy NFC Adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Kiểm tra xem NFC có được hỗ trợ hay không
        if (nfcAdapter == null) {
            // Thiết bị không hỗ trợ NFC
            return
        }

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                NFCReaderWriter(
                    nfcAdapter = nfcAdapter,
                    message = "Đưa thẻ NFC vào vùng đọc",
                    onStartWrite = { data ->
                        dataToWrite = data // Cập nhật dữ liệu cần ghi
                        writeMode = true   // Chuyển sang chế độ ghi
                    }
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
                // Nếu đang ở chế độ ghi, thực hiện ghi dữ liệu vào thẻ
                val success = writeNfcTag(currentTag, dataToWrite)

                // Hiển thị toast thông báo ghi dữ liệu thành công hoặc thất bại
                Toast.makeText(
                    this,
                    if (success) "Ghi dữ liệu thành công!" else "Ghi dữ liệu thất bại!",
                    Toast.LENGTH_SHORT
                ).show()
                setContent {
                    NFCReaderWriter(
                        nfcAdapter = nfcAdapter,
                        //message = if (success) "Ghi dữ liệu thành công!" else "Ghi dữ liệu thất bại!",
                        message = "Đưa thẻ NFC vào vùng đọc",
                        onStartWrite = { data ->
                            dataToWrite = data
                            writeMode = true
                        },
                        writeMode = writeMode
                    )
                }
                writeMode = false // Sau khi ghi xong, quay lại chế độ đọc
            } else {
                // Đọc thẻ nếu không ở chế độ ghi
                val nfcContent = readNfcTag(it) ?: "Không thể đọc dữ liệu từ thẻ"
                setContent {
                    NFCReaderWriter(
                        nfcAdapter = nfcAdapter,
                        message = nfcContent,
                        onStartWrite = { data ->
                            dataToWrite = data
                            writeMode = true
                        },
                        writeMode = writeMode
                    )
                }
            }
        }
    }
}

@Composable
fun NFCReaderWriter(
    nfcAdapter: NfcAdapter?,
    message: String,
    onStartWrite: (String) -> Unit,
    writeMode: Boolean = false
) {
    var nfcMessage by remember { mutableStateOf(message) }
    var inputText by remember { mutableStateOf("") }  // Text để nhập nội dung
    var isWriting by remember { mutableStateOf(writeMode) }

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
        Text(text = nfcMessage, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (isWriting) {
            // TextField để nhập dữ liệu
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Nhập dữ liệu vào thẻ NFC") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                onStartWrite(inputText)  // Bắt đầu chế độ ghi khi nhấn nút
                nfcMessage = "Đưa thẻ NFC vào vùng đọc để ghi..."
            }) {
                Text(text = "Bắt đầu ghi")
            }
        }


        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                isWriting = !isWriting
                nfcMessage = if (isWriting) "Nhập nội dung" else "Đưa thẻ NFC vào vùng đọc"
            },
        ) {
            if (isWriting) {
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Chuyển sang chế độ đọc")
                }

            } else {
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(text = "Chuyển sang chế độ ghi")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
            //Text(text = if (isWriting) "Chuyển sang chế độ đọc" else "Chuyển sang chế độ ghi")
        }
    }
}

fun readNfcTag(tag: Tag): String? {
    val ndef = Ndef.get(tag) ?: return null
    return try {
        ndef.connect()
        val message = ndef.ndefMessage ?: return "Thẻ không chứa dữ liệu NDEF"
        val records = message.records
        if (records.isNotEmpty()) {
            // Trả về nội dung của bản ghi đầu tiên và bỏ code ngôn ngữ
            String(records[0].payload, Charsets.UTF_8)
                .subSequence(3, records[0].payload.size).toString()
        } else {
            "Thẻ không có bản ghi dữ liệu"
        }
    } catch (e: IOException) {
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
    NFCReaderWriter(nfcAdapter = null, message = "Đưa thẻ NFC vào vùng đọc", onStartWrite = {})
}