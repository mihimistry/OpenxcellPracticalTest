package com.example.openxcellpracticaltest.data.room

import android.provider.ContactsContract.CommonDataKinds.Note
import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.openxcellpracticaltest.model.ProductItem


@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(productList: List<ProductItem>?)

    @Query("DELETE FROM product_item")
    fun deleteAllProducts()

    @Query("SELECT * FROM product_item")
    fun getProducts(): List<ProductItem>

}