package com.example.openxcellpracticaltest.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.openxcellpracticaltest.model.Converters
import com.example.openxcellpracticaltest.model.ProductItem
import com.example.openxcellpracticaltest.model.ProductMedias

@Database(entities = [ProductItem::class], version = 1)
@TypeConverters(Converters::class)

abstract class ProductDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao?

    companion object {
        var database: ProductDatabase? = null

        fun getInstance(context: Context): ProductDatabase? {
            if (database == null) {
                database = Room.databaseBuilder(
                    context.applicationContext,
                    ProductDatabase::class.java,
                    "ProductDB"
                )
                    .fallbackToDestructiveMigration().build()
            }
            return database
        }
    }
}