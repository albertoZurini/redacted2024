package com.example.hce_app

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.hce_app.cardEmulation.KHostApduService
import com.example.hce_app.ui.theme.NFCDemoTheme

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private var currentTag: Tag? = null  // Lưu thẻ hiện tại khi phát hiện

    private var writeMode: Boolean by mutableStateOf(false)  // Trạng thái ghi dữ liệu
    private var dataToWrite: String by mutableStateOf("")    // Dữ liệu cần ghi

    private var nfcMessage: String by mutableStateOf("Hello")  // Trạng thái hiển thị thông điệp

    private var messageToCast: String by mutableStateOf("")

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
            intent.putExtra("ndefMessage", messageToCast)
            startService(intent)
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
}
