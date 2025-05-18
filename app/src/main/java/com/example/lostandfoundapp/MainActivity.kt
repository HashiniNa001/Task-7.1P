package com.example.lostandfoundapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lostandfoundapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.buttonCreateAdvert.setOnClickListener {
            startActivity(Intent(this, CreateAdvertActivity::class.java))
        }

        binding.buttonShowItems.setOnClickListener {
            startActivity(Intent(this, ItemListActivity::class.java))
        }

        binding.buttonShowMap.setOnClickListener {
            handleMapButtonClick()
        }
    }

    private fun handleMapButtonClick() {
        // Disable the button immediately to prevent multiple clicks
        binding.buttonShowMap.isEnabled = false
        binding.buttonShowMap.text = "Loading..."

        scope.launch {
            try {
                // Start the map activity
                startActivity(Intent(this@MainActivity, MapActivity::class.java))
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error opening map: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Re-enable the button on error
                    binding.buttonShowMap.isEnabled = true
                    binding.buttonShowMap.text = "SHOW ON MAP"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure button is enabled when returning to this activity
        binding.buttonShowMap.isEnabled = true
        binding.buttonShowMap.text = "SHOW ON MAP"
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel any ongoing coroutines
        scope.launch {
            // Clean up any resources if needed
        }
    }
}
