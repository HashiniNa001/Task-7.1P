package com.example.lostandfoundapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lostandfoundapp.databinding.ActivityCreateAdvertBinding

class CreateAdvertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAdvertBinding
    private lateinit var dbHelper: LostFoundDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAdvertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = LostFoundDatabaseHelper(this)

        binding.buttonSave.setOnClickListener {
            val type = if (binding.radioLost.isChecked) "Lost" else "Found"
            val name = binding.editTextName.text.toString()
            val phone = binding.editTextPhone.text.toString()
            val description = binding.editTextDescription.text.toString()
            val date = binding.editTextDate.text.toString()
            val location = binding.editTextLocation.text.toString()

            val success = dbHelper.insertItem(type, name, phone, description, date, location)
            if (success) {
                Toast.makeText(this, "Item saved!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error saving item", Toast.LENGTH_SHORT).show()
            }
        }
    }
}