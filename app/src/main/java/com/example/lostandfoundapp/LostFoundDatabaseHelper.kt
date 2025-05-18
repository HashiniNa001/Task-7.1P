package com.example.lostandfoundapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.lostandfoundapp.model.Item

class LostFoundDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "LostFound.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_NAME = "items"
        const val COLUMN_ID = "id"
        const val COLUMN_TYPE = "type"
        const val COLUMN_NAME = "name"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_DATE = "date"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_TYPE TEXT,
            $COLUMN_NAME TEXT,
            $COLUMN_PHONE TEXT,
            $COLUMN_DESCRIPTION TEXT,
            $COLUMN_DATE TEXT,
            $COLUMN_LOCATION TEXT,
            $COLUMN_LATITUDE REAL,
            $COLUMN_LONGITUDE REAL
        )"""
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_LATITUDE REAL")
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_LONGITUDE REAL")
        }
    }

    fun insertItem(
        type: String,
        name: String,
        phone: String,
        description: String,
        date: String,
        location: String,
        latitude: Double?,
        longitude: Double?
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TYPE, type)
            put(COLUMN_NAME, name)
            put(COLUMN_PHONE, phone)
            put(COLUMN_DESCRIPTION, description)
            put(COLUMN_DATE, date)
            put(COLUMN_LOCATION, location)
            put(COLUMN_LATITUDE, latitude)
            put(COLUMN_LONGITUDE, longitude)
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
                    getString(getColumnIndexOrThrow(COLUMN_LOCATION)),
                    getDoubleOrNull(getColumnIndexOrThrow(COLUMN_LATITUDE)),
                    getDoubleOrNull(getColumnIndexOrThrow(COLUMN_LONGITUDE))
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

    private fun android.database.Cursor.getDoubleOrNull(columnIndex: Int): Double? {
        return if (isNull(columnIndex)) null else getDouble(columnIndex)
    }
}
