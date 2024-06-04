package com.example.readandwritenfc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.readandwritenfc.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRead.setOnClickListener {
            startActivity(Intent(this, ReadNFCActivity::class.java))
        }

        binding.btnWrite.setOnClickListener {
            startActivity(Intent(this, WriteNFCActivity::class.java))
        }

    }

}