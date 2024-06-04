package com.example.readandwritenfc

import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.readandwritenfc.databinding.ActivityReadNfcBinding
import com.qifan.readnfcmessage.parser.NdefMessageParser
import java.io.IOException

class ReadNFCActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {
    private lateinit var binding: ActivityReadNfcBinding
    private var mNfcAdapter: NfcAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadNfcBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if(mNfcAdapter==null){
            Toast.makeText(this,"NFC not supported", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this,"NFC Supported", Toast.LENGTH_SHORT).show()
        }
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
                NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V or
                        NfcAdapter.FLAG_READER_NFC_BARCODE or
                        NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
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
            try {
                mNdef.connect()

                // Check if the tag is NDEF formatted
                if (mNdef.isConnected) {
                    val mNdefMessage = mNdef.ndefMessage
                    parserNDEFMessage(mNdefMessage)
                    // Iterate through NDEF records to extract data
                    for (record in mNdefMessage.records) {
                        // Assuming the record contains text
                        val payload = record.payload
                        // Decode payload to string using UTF-8 encoding
                        val text = String(payload, charset("UTF-8"))

                        // Do something with the read text
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                "Read NFC Tag: $text",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    // Tag is not NDEF formatted
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            "NFC Tag is not NDEF formatted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: IOException) {
                // Handle I/O exception
                e.printStackTrace()
            } catch (e: FormatException) {
                // Handle FormatException
                e.printStackTrace()
            } finally {
                // Close the connection
                try {
                    mNdef.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun parserNDEFMessage(messages: NdefMessage) {
        val builder = StringBuilder()
        val records = NdefMessageParser.parse(messages)
        val size = records.size

        for (i in 0 until size) {
            val record = records[i]
            val str = record.str()
            builder.append(str).append("\n")
        }

        binding.tvReadNFC.text = builder.toString()
    }
}