package com.example.lostandfoundapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lostandfoundapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonCreateAdvert.setOnClickListener {
            startActivity(Intent(this, CreateAdvertActivity::class.java))
        }

        binding.buttonShowItems.setOnClickListener {
            startActivity(Intent(this, ItemListActivity::class.java))
        }
    }
}