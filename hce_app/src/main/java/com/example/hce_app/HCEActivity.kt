package com.example.hce_app

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import android.util.Log
import android.nfc.Tag
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.hce_app.cardEmulation.KHostApduService

class HCEActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private var currentTag: Tag? = null  // Lưu thẻ hiện tại khi phát hiện
    private val TAG = "ReaderMainActivity"

    private var writeMode: Boolean by mutableStateOf(false)  // Trạng thái ghi dữ liệu
    private var dataToWrite: String by mutableStateOf("")    // Dữ liệu cần ghi

    private var nfcMessage: String by mutableStateOf("Hello")  // Trạng thái hiển thị thông điệp

    private var messageToCast: String by mutableStateOf("https://metamask.app.link/send/0x7c00dC7574605bb50ada16E75CC797eC7f17B7FE\\?value\\=1000000000000000")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lấy NFC Adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Kiểm tra xem NFC có được hỗ trợ hay không
        if (nfcAdapter == null || !supportNfcHceFeature()) {
            // Thiết bị không hỗ trợ NFC
            setContent {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Text(text = "Can't get NFCAdapter")
                }
            }
        }

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column {
                    Text(nfcMessage)
                    TextField(
                        value = messageToCast,
                        onValueChange = { messageToCast = it },
                        label = { Text("Label") }
                    )
                    Button(
                        onClick={
                            setNFCMessage()
                        }
                    ) {
                        Text("Set the message")
                    }
                }
            }
        }
    }

    private fun supportNfcHceFeature() =
        checkNFCEnable() && packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)

    private fun checkNFCEnable(): Boolean {
        return if (nfcAdapter == null) {
            false
        } else {
            nfcAdapter?.isEnabled == true
        }
    }

    private fun setNFCMessage() {
        if (TextUtils.isEmpty(messageToCast)) {
            Toast.makeText(
                this,
                "The message has not to be empty",
                Toast.LENGTH_LONG,
            ).show()
        } else {
            val intent = Intent(this, KHostApduService::class.java)
            intent.putExtra("ndefMessage", "https://metamask.app.link/send/0x7c00dC7574605bb50ada16E75CC797eC7f17B7FE\\?value\\=1000000000000000")
            startService(intent)
            Toast.makeText(
                this,
                "Message has been set",
                Toast.LENGTH_LONG,
            ).show()
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
    /*
        override fun onNewIntent(intent: Intent) {
            super.onNewIntent(intent)

            if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
                    val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
                    // Process the messages array.
                    parserNDEFMessage(messages)
                }
            }
        }

        private fun parserNDEFMessage(messages: List<NdefMessage>) {
            val builder = StringBuilder()
            println(messages.toString())
        }
    */
    private fun readNfcTag(tag: Tag) {
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                val message = ndef.ndefMessage ?: return
                val records = message.records
                if (records.isNotEmpty()) {
                    val payload = String(records[0].payload, Charsets.UTF_8)
                    nfcMessage = payload.substring(3)  // Skip the language code
                    Log.d(TAG, "Read data from NFC: $payload")
                } else {
                    nfcMessage = "No data found"
                }
            } catch (e: Exception) {
                nfcMessage = "Error reading NFC: ${e.message}"
                Log.e(TAG, "Error reading NFC", e)
            } finally {
                ndef.close()
            }
        } else {
            nfcMessage = "NDEF not supported on this tag"
        }
    }
}
