package com.example.lostandfoundapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lostandfoundapp.databinding.ActivityItemListBinding

class ItemListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemListBinding
    private lateinit var dbHelper: LostFoundDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = LostFoundDatabaseHelper(this)

        val items = dbHelper.getAllItems()
        val itemTitles = items.map { "${it.type}: ${it.description}" }

        binding.listViewItems.adapter = android.widget.ArrayAdapter(
            this, android.R.layout.simple_list_item_1, itemTitles
        )

        binding.listViewItems.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = items[position]
            val intent = Intent(this, ItemDetailActivity::class.java)
            intent.putExtra("ITEM_ID", selectedItem.id)
            startActivity(intent)
        }
    }
}