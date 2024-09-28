package com.codetogether.nfcdemo

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
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
import com.qifan.readnfcmessage.parser.NdefMessageParser
import java.io.IOException

class MainActivity : ComponentActivity() {
    private val TAG = "ReaderMainActivity"

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
                    Text(text = "Can't get NFCAdapter")
                }
            }
        }

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                Text("Hello")
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

    private fun selectApdu(aid: ByteArray): ByteArray {
        val commandApdu = ByteArray(5 + aid.size)
        commandApdu[0] = 0x00.toByte() // CLA
        commandApdu[1] = 0xA4.toByte() // INS
        commandApdu[2] = 0x04.toByte() // P1
        commandApdu[3] = 0x00.toByte() // P2
        commandApdu[4] = (aid.size and 0x0FF).toByte() // Lc
        System.arraycopy(aid, 0, commandApdu, 5, aid.size)
        // commandApdu[commandApdu.size - 1] = 0x00.toByte() // Le
        return commandApdu
    }

    private fun readBinaryAPDU(): ByteArray {
        val commandApdu = ByteArray(5)
        commandApdu[0] = 0x00.toByte()
        commandApdu[1] = 0xB0.toByte()
        commandApdu[2] = 0x00.toByte()
        commandApdu[3] = 0x00.toByte()
        commandApdu[4] = 0x10.toByte()
        return commandApdu
    }

    fun extractTextFromNDEF(rawData: ByteArray): String {
        // Check that the data is long enough to contain a meaningful NDEF record
        if (rawData.size < 3) {
            throw IllegalArgumentException("The NDEF data is too short.")
        }

        // The NDEF message starts with byte 0xD9 (indicating a message)
        if (rawData[2] != 0xD9.toByte()) {
            throw IllegalArgumentException("Invalid NDEF message start.")
        }

        // The TNF (Type Name Format) is stored in byte 6 and should be '0x54' for text
        if (rawData[6] != 0x54.toByte()) {
            throw IllegalArgumentException("Invalid NDEF record type.")
        }

        // The language code length is stored in byte 9, for example 0x02 for "en"
        val languageCodeLength = rawData[9].toInt()

        // The text starts after the language code (position 10 + language code length)
        val textStartIndex = 10 + languageCodeLength
        val textLength = rawData.size - textStartIndex - 1 // Remove the last padding byte (0x90)

        // Extract and return the text portion
        return rawData.copyOfRange(textStartIndex, textStartIndex + textLength).toString(Charsets.UTF_8)
    }

    val SelectAID: ByteArray = byteArrayOf(0xF0.toByte(), 0x39.toByte(), 0x41.toByte(), 0x48.toByte(), 0x14.toByte(), 0x81.toByte(), 0x00.toByte())
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        println("APDU message: " + selectApdu(SelectAID))

        val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        val isoDep: IsoDep = IsoDep.get(tag);
        if (isoDep != null) {
            try {
                isoDep.connect();
                var result = isoDep.transceive(selectApdu(SelectAID))
                if (!(result[0] == 0x90.toByte() && result[1] == 0x00.toByte())){
                    Log.wtf(TAG,"Error while selecting APDU")
                }

                result = isoDep.transceive(readBinaryAPDU())
                if (!(result[0] == 0x90.toByte() && result[1] == 0x00.toByte())){
                    Log.wtf(TAG,"Error while readming memory")
                }
                val output = extractTextFromNDEF(result)
                Log.i(TAG, "Output: "+output)
            } catch ( ex: IOException) {
                println("Exception " + ex)
            } finally {

                try {
                    isoDep.close();
                } catch (ignored: Exception) {
                    println("Ignored " + ignored)
                }
            }
        }

        /*
                tag?.let {
                    currentTag = it
                    val nfcContent = readNfcTag(it)
                    println(nfcContent)
                }


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
                }if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
                        val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
                        // Process the messages array.
                        parserNDEFMessage(messages)
                    }
                }
                */
    }

    private fun parserNDEFMessage(messages: List<NdefMessage>) {
        val builder = StringBuilder()
        val records = NdefMessageParser.parse(messages[0])
        val size = records.size

        for (i in 0 until size) {
            val record = records[i]
            val str = record.str()
            builder.append(str).append("\n")
        }
        println(builder.toString())
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
