package com.example.exercise1

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity("orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val size: String,
    val num: Int,
    val note: String?
)

@Dao
interface OrderDao{
    @Insert
    suspend fun insert(order: OrderEntity)

    @Query("SELECT * FROM `orders`")
    fun getAll(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM `orders` WHERE id = id")
    fun getById(id: Int): Flow<OrderEntity?>
}

@Database(
    entities = [OrderEntity::class],
    version = 1
)

abstract class AppDatabase: RoomDatabase(){
    abstract fun orderDao(): OrderDao
    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "order_db"
                ).build().also{
                    INSTANCE = it
                }
            }
        }
    }
}

class OrderRepository(private val dao: OrderDao){
    suspend fun insert(order: OrderEntity){
        dao.insert(order)
    }

    val drinks = dao.getAll()

    fun getOrderID(id: Int): Flow<OrderEntity?>{
        return dao.getById(id)
    }
}

class SharedViewModel : ViewModel() {
    var size by mutableStateOf("")
    var num by mutableStateOf(1)
    var note by mutableStateOf<String?>(null)

    fun setOrder(size: String, num: Int, note: String?){
        this.size = size
        this.num = num
        this.note = note
    }
}

class OrderViewModel(
    private val repository: OrderRepository): ViewModel() {
    fun insertOrder(size: String, num: Int, note: String?){
        viewModelScope.launch {
            repository.insert(
                OrderEntity(
                    size = size,
                    num = num,
                    note = note))
        }
    }
    val drinks = repository.drinks

    fun getDrinkID(id: Int): Flow<OrderEntity?>{
        return repository.getOrderID(id)
    }
}

class OrderViewModelFactory(context: Context): ViewModelProvider.Factory{
    private val dao = AppDatabase.getDatabase(context).orderDao()
    private val repository = OrderRepository(dao)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(OrderViewModel::class.java)){
            return OrderViewModel(repository) as T
        }
        throw IllegalArgumentException("I don't know this ViewModel Class")
    }
}