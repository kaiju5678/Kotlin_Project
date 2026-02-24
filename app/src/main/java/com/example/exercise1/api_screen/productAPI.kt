package com.example.exercise1.api_screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.converter.gson.GsonConverterFactory

data class Products(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val rating: Rating
)

data class Rating(
    val rate: Double,
    val count: Int
)

interface ApiService{
    @GET("products/{id}")
    suspend fun getProductbyID(
        @Path("id")id: Int
    ): retrofit2.Response<Products>

    @GET("products")
    suspend fun getAllProducts(): retrofit2.Response<List<Products>>
}

object RetrofitInstance{
    private const val BASE_URL = "https://fakestoreapi.com/"
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
){
    class Success<T> (data: T): Resource<T>(data)
    class Error<T>(message: String?, data: T? = null): Resource<T>(data, message)
    class Loading<T>: Resource<T>()
}

class ProductRepository{
    suspend fun fetchproductByID(id: Int): Resource<Products>{
        return try{
            val response = RetrofitInstance.api.getProductbyID(id)
            if (response.isSuccessful){
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty body")
            } else{
                Resource.Error("Error ${response.code()}")
            }
        } catch (e: Exception){
            Resource.Error(e.message)
        }
    }

    suspend fun fetchAllProducts(): Resource<List<Products>>{
        return try{
            val response = RetrofitInstance.api.getAllProducts()
            if (response.isSuccessful){
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty body")
            } else{
                Resource.Error("Error ${response.code()}")
            }
        } catch (e: Exception){
            Resource.Error(e.message)
        }
    }
    }

class ProductViewModel(private val repository: ProductRepository) : ViewModel(){
    private val _product = MutableLiveData<Resource<Products>>()
    private val _allProduct = MutableLiveData<Resource<List<Products>>>()
    val product: LiveData<Resource<Products>> = _product
    val allProduct: LiveData<Resource<List<Products>>> = _allProduct
    fun loadProduct(id: Int){
        _product.value = Resource.Loading()
        viewModelScope.launch {
            _product.value = repository.fetchproductByID(id)
        }
    }
    fun loadAllProduct(){
        _allProduct.value = Resource.Loading()
        viewModelScope.launch {
            _allProduct.value = repository.fetchAllProducts()
        }
    }
}

class ProductViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory{
    override  fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)){
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}