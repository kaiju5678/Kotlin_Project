package com.example.exercise1

import android.media.Rating
import okhttp3.Response
import org.intellij.lang.annotations.Pattern
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
    val  rating: Rating
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
}