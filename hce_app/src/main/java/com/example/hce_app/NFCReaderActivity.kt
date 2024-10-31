package com.example.hce_app

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.hce_app.parser.NDEFTools
import java.io.IOException

class NFCReaderActivity : ComponentActivity() {
    private val TAG = "ReaderMainActivity"

    private var nfcAdapter: NfcAdapter? = null
    private var currentTag: Tag? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            setContent {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NFC ERROR! Adapter unavailable",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        } else {
            setContent {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CryptoPOS\nReceiver",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
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

    private fun processGottenURL(url: String) {
        // Now create the button with the hyperlink
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    val SelectAID: ByteArray = byteArrayOf(0xF0.toByte(), 0x39.toByte(), 0x41.toByte(), 0x48.toByte(), 0x14.toByte(), 0x81.toByte(), 0x00.toByte())

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        val isoDep: IsoDep? = IsoDep.get(currentTag)

        if (isoDep != null) {
            try {
                isoDep.connect()
                val result = isoDep.transceive(selectApdu(SelectAID))
                if (!(result[0] == 0x6A.toByte() && result[1] == 0x82.toByte())) {
                    Log.wtf(TAG, "Error while authenticating with the app!")
                }

                val readResult = isoDep.transceive(readBinaryAPDU())
                if (!(readResult[readResult.size - 2] == 0x90.toByte() && readResult[readResult.size - 1] == 0x00.toByte())) {
                    Log.wtf(TAG, "Error while reading memory")
                }
                val output = NDEFTools.ExtractTextFromNDEF(readResult)
                Log.i(TAG, "Output: $output")

                processGottenURL(output)

            } catch (ex: IOException) {
                Log.e(TAG, "IOException: ${ex.message}")
            } finally {
                try {
                    isoDep.close()
                } catch (ignored: Exception) {
                    Log.w(TAG, "Ignored exception while closing IsoDep")
                }
            }
        } else {
            Toast.makeText(this, "ISO Dep is not supported on this tag", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectApdu(aid: ByteArray): ByteArray {
        val commandApdu = ByteArray(5 + aid.size)
        commandApdu[0] = 0x00.toByte() // CLA
        commandApdu[1] = 0xA4.toByte() // INS
        commandApdu[2] = 0x04.toByte() // P1
        commandApdu[3] = 0x00.toByte() // P2
        commandApdu[4] = (aid.size and 0x0FF).toByte() // Lc
        System.arraycopy(aid, 0, commandApdu, 5, aid.size)
        return commandApdu
    }

    private fun readBinaryAPDU(): ByteArray {
        val commandApdu = ByteArray(5)
        commandApdu[0] = 0x00.toByte()
        commandApdu[1] = 0xB0.toByte()
        commandApdu[2] = 0x00.toByte()
        commandApdu[3] = 0x00.toByte()
        commandApdu[4] = 0xFE.toByte()
        return commandApdu
    }
}
