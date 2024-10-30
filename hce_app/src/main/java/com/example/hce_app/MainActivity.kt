package com.example.hce_app

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
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

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null

    private var nfcMessage: String by mutableStateOf("Hello")

    private var address: String by mutableStateOf("0x97324859b73833dC6ACAFd216B1DB57EfDac9Fb7")
    private var amount: String by mutableStateOf("1000000000")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null || !supportNfcHceFeature()) {
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
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address where to receive the payment") }
                    )
                    TextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount to be paid") }
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
        // Combine all the data into a metamask url
        val urlToCast = "https://metamask.app.link/send/$address?value=$amount";
        if (TextUtils.isEmpty(urlToCast)) {
            Toast.makeText(
                this,
                "The message has not to be empty",
                Toast.LENGTH_LONG,
            ).show()
        } else {
            Toast.makeText(
                this,
                urlToCast,
                Toast.LENGTH_LONG,
            ).show()
            val intent = Intent(this, KHostApduService::class.java)
            intent.putExtra("ndefMessage", urlToCast)
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

}
