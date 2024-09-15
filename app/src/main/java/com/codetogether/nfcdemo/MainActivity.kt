package com.codetogether.nfcdemo

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

// Đọc dữ liệu từ thẻ NFC
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

// Ghi dữ liệu lên thẻ NFC
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
