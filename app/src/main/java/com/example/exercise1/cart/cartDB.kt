package com.example.exercise1.cart

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// 1. เพิ่ม image และ quantity เข้าไปใน Entity
@Entity("carts")
data class CartEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int, // เก็บ ID จาก API ไว้ด้วยเผื่อใช้
    val title: String,
    val price: Double,
    val category: String,
    val image: String, // เพิ่มรูปภาพ (URL)
    var quantity: Int = 1 // เพิ่มจำนวนสินค้า
)

@Dao
interface CartDao{
    @Insert
    suspend fun insert(cart: CartEntity)

    @Query("SELECT * FROM `carts`")
    fun getAll(): Flow<List<CartEntity>>

    // เช็คว่ามีสินค้านี้ในตะกร้าหรือยัง (เผื่อบวกจำนวนเพิ่มแทนการเพิ่มแถวใหม่)
    @Query("SELECT * FROM `carts` WHERE productId = :pId LIMIT 1")
    suspend fun getCartItemByProductId(pId: Int): CartEntity?

    @Update
    suspend fun update(cart: CartEntity)

    @Delete
    suspend fun delete(cart: CartEntity)

    @Query("DELETE FROM carts")
    suspend fun clearCart()
}

// 2. เปลี่ยนชื่อ Class Database เป็น CartDatabase เพื่อไม่ให้ชนกับ Order
@Database(
    entities = [CartEntity::class],
    version = 1
)
abstract class CartDatabase: RoomDatabase(){
    abstract fun cartDao(): CartDao
    companion object{
        @Volatile
        private var INSTANCE: CartDatabase? = null
        fun getDatabase(context: Context): CartDatabase {
            return INSTANCE ?: synchronized(this){
                Room.databaseBuilder(
                    context.applicationContext,
                    CartDatabase::class.java,
                    "cart_db" // ชื่อไฟล์ DB
                ).build().also{
                    INSTANCE = it
                }
            }
        }
    }
}

class CartRepository(private val dao: CartDao){
    val allCartItems = dao.getAll()

    suspend fun addToCart(id: Int, title: String, price: Double, category: String, image: String){
        // เช็คก่อนว่ามีสินค้านี้ไหม ถ้ามีให้บวกจำนวน
        val existingItem = dao.getCartItemByProductId(id)
        if(existingItem != null){
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
            dao.update(updatedItem)
        } else {
            dao.insert(CartEntity(
                productId = id,
                title = title,
                price = price,
                category = category,
                image = image,
                quantity = 1
            ))
        }
    }

    suspend fun update(cart: CartEntity){
        dao.update(cart)
    }

    suspend fun delete(cart: CartEntity){
        dao.delete(cart)
    }

    suspend fun clearCart(){
        dao.clearCart()
    }
}

class CartViewModel(private val repository: CartRepository): ViewModel() {

    val cartItems: Flow<List<CartEntity>> = repository.allCartItems

    fun addToCart(id:Int, title: String, price: Double, category: String, image: String){
        viewModelScope.launch {
            repository.addToCart(id, title, price, category, image)
        }
    }

    // ฟังก์ชันสำหรับเพิ่ม/ลดจำนวนในหน้าตะกร้า
    fun updateQuantity(cart: CartEntity, newQuantity: Int){
        viewModelScope.launch {
            if(newQuantity > 0){
                repository.update(cart.copy(quantity = newQuantity))
            } else {
                repository.delete(cart) // ถ้าลดเหลือ 0 ให้ลบออก
            }
        }
    }

    fun deleteItem(cart: CartEntity){
        viewModelScope.launch {
            repository.delete(cart)
        }
    }

    fun clearAll(){
        viewModelScope.launch {
            repository.clearCart()
        }
    }
}

class CartViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            val database = CartDatabase.getDatabase(context)
            val repository = CartRepository(database.cartDao())
            return CartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}