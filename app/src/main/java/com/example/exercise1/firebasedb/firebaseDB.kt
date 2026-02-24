package com.example.exercise1.firebasedb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Order(
    val id: String = "",
    val size: String = "",
    val qty: Int = 0,
    val note: String? = null
)

class FirestoreOrderDataSource {
    private val collection = Firebase.firestore.collection("orders")
    suspend fun insert(order: Order){
        collection.add(order).await()
    }

    suspend fun update(order:Order){
        collection.document( order.id).update(
                mapOf(
                    "size" to order.size,
                    "qty" to order.qty,
                    "note" to order.note
                )
        ).await()
    }

    suspend fun delete(orderID: String){
        collection.document(orderID).delete().await()
    }

    fun getAll(): Flow<List<Order>> = callbackFlow{
        val listener = collection.addSnapshotListener { snapshot,error ->
            if (error != null){
                close(error); return@addSnapshotListener
            }
            val orders = snapshot?.documents?.mapNotNull { doc->
                doc.toObject(Order::class.java)?.copy(id = doc.id)
            }?:emptyList()
            trySend(orders)
        }
        awaitClose { listener.remove() }
    }

    class OrderRepository(
        private val dataSource: FirestoreOrderDataSource = FirestoreOrderDataSource()
    ){
        val orders = dataSource.getAll()


        suspend fun  insert(order: Order){
            dataSource.insert(order)
        }

        suspend fun  update(order: Order){
            dataSource.update(order)
        }

        suspend fun  delete(orderID: String){
            dataSource.delete(orderID)
        }
    }
}

class OrderViewModel(
    private val repository: FirestoreOrderDataSource.OrderRepository = FirestoreOrderDataSource.OrderRepository()
): ViewModel() {

    val orders = repository.orders

    fun insertOrder(
        size:String,
        qty:Int,
        note:String?
    ){
        viewModelScope.launch {
            repository.insert(
                Order(size = size, qty = qty, note = note)
            )
        }
    }

    fun updateOrder(order:Order){
        viewModelScope.launch {
            repository.update(order)
        }
    }

    fun deleteOrder(orderId:String){
        viewModelScope.launch {
            repository.delete(orderId)
        }
    }

}