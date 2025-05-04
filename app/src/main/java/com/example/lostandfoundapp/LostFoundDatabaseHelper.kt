package com.example.lostandfoundapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.lostandfoundapp.model.Item

class LostFoundDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "LostFound.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "items"
        const val COLUMN_ID = "id"
        const val COLUMN_TYPE = "type"
        const val COLUMN_NAME = "name"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_DATE = "date"
        const val COLUMN_LOCATION = "location"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_TYPE TEXT,
            $COLUMN_NAME TEXT,
            $COLUMN_PHONE TEXT,
            $COLUMN_DESCRIPTION TEXT,
            $COLUMN_DATE TEXT,
            $COLUMN_LOCATION TEXT
        )"""
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertItem(type: String, name: String, phone: String, description: String, date: String, location: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TYPE, type)
            put(COLUMN_NAME, name)
            put(COLUMN_PHONE, phone)
            put(COLUMN_DESCRIPTION, description)
            put(COLUMN_DATE, date)
            put(COLUMN_LOCATION, location)
        }
        val result = db.insert(TABLE_NAME, null, values)
        return result != -1L
    }

    fun getAllItems(): List<Item> {
        val itemList = mutableListOf<Item>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        with(cursor) {
            while (moveToNext()) {
                val item = Item(
                    getInt(getColumnIndexOrThrow(COLUMN_ID)),
                    getString(getColumnIndexOrThrow(COLUMN_TYPE)),
                    getString(getColumnIndexOrThrow(COLUMN_NAME)),
                    getString(getColumnIndexOrThrow(COLUMN_PHONE)),
                    getString(getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    getString(getColumnIndexOrThrow(COLUMN_DATE)),
                    getString(getColumnIndexOrThrow(COLUMN_LOCATION))
                )
                itemList.add(item)
            }
        }
        cursor.close()
        return itemList
    }

    fun deleteItem(id: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
        return result > 0
    }
}