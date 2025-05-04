package com.example.lostandfoundapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lostandfoundapp.databinding.ActivityItemDetailBinding

class ItemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailBinding
    private lateinit var dbHelper: LostFoundDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = LostFoundDatabaseHelper(this)

        val itemId = intent.getIntExtra("ITEM_ID", -1)
        val item = dbHelper.getAllItems().find { it.id == itemId }

        item?.let {
            binding.textViewDescription.text = "${it.description}"
            binding.textViewDate.text = it.date
            binding.textViewLocation.text = it.location

            binding.buttonRemove.setOnClickListener {
                val success = dbHelper.deleteItem(itemId)
                if (success) {
                    Toast.makeText(this, "Item removed!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error removing item", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}