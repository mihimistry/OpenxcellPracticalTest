package com.example.openxcellpracticaltest.data.room

import android.provider.ContactsContract.CommonDataKinds.Note
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.openxcellpracticaltest.model.ProductItem


@Dao
interface ProductDao {

    @Insert
    fun insertProduct(productList: List<ProductItem>?)

    @Query("SELECT * FROM product_item")
    fun getProducts(): List<ProductItem>

}