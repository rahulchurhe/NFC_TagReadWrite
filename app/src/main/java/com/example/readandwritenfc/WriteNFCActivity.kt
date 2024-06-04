package com.example.readandwritenfc

import android.media.RingtoneManager
import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.Ndef
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.readandwritenfc.databinding.ActivityWriteNfcBinding
import java.io.IOException

class WriteNFCActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {
    private lateinit var binding: ActivityWriteNfcBinding
    private var mNfcAdapter: NfcAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteNfcBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)

    }

    override fun onResume() {
        super.onResume()

        if (mNfcAdapter != null) {
            val options = Bundle()
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

            // Enable ReaderMode for all types of card and disable platform sounds
            mNfcAdapter!!.enableReaderMode(
                this,
                this,
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V or NfcAdapter.FLAG_READER_NFC_BARCODE or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                options
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (mNfcAdapter != null) mNfcAdapter!!.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        val mNdef = Ndef.get(tag)
        if (mNdef != null) {
            val mNdefMessage = mNdef.cachedNdefMessage
            val mRecord = NdefRecord.createTextRecord("en", binding.edNFC.text.toString())
            val mMsg = NdefMessage(mRecord)

            try {
                mNdef.connect()
                mNdef.writeNdefMessage(mMsg)

                runOnUiThread {
                    Toast.makeText(
                        applicationContext, "Write to NFC Success $mMsg", Toast.LENGTH_SHORT
                    ).show()
                }

                try {
                    val notification =
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val r = RingtoneManager.getRingtone(
                        applicationContext, notification
                    )
                    r.play()
                } catch (e: Exception) {
                    // Some error playing sound
                }
            } catch (e: FormatException) {
                // if the NDEF Message to write is malformed
            } catch (e: TagLostException) {
                // Tag went out of range before operations were complete
            } catch (e: IOException) {
                // if there is an I/O failure, or the operation is cancelled
            } catch (e1: SecurityException) {
                try {
                    mNdef.close()
                } catch (e: IOException) {
                    // if there is an I/O failure, or the operation is cancelled
                }
            }
        }
    }
}