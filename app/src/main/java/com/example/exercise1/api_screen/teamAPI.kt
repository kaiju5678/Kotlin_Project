package com.example.exercise1.api_screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers

// --- 1. Data Models ---
data class TeamResponse(
    val data: List<Team>
)

data class Team(
    val id: Int,
    val conference: String,
    val division: String,
    val city: String,
    val name: String,
    val full_name: String,
    val abbreviation: String
)

// --- 2. API Service Interface ---
interface TeamApiService {
    // TODO: นำ API Key จาก balldontlie.io มาใส่แทน YOUR_API_KEY_HERE
    // ตัวอย่าง: @Headers("Authorization: 12345abcde-api-key-...")
    @Headers("Authorization: API_SECRET")
    @GET("teams")
    suspend fun getAllTeams(): Response<TeamResponse>
}

// --- 3. Retrofit Instance ---
object TeamRetrofitInstance {
    private const val BASE_URL = "https://api.balldontlie.io/v1/"

    val api: TeamApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TeamApiService::class.java)
    }
}

// --- 4. Repository ---
class TeamRepository {
    // เราใช้ Resource class เดิมที่มีอยู่แล้วในโปรเจกต์ (จากไฟล์ productAPI.kt)
    suspend fun getTeams(): Resource<List<Team>> {
        return try {
            val response = TeamRetrofitInstance.api.getAllTeams()
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it.data)
                } ?: Resource.Error("No data found")
            } else {
                Resource.Error("API Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Exception: ${e.message}")
        }
    }
}

// --- 5. ViewModel ---
class TeamViewModel(private val repository: TeamRepository) : ViewModel() {
    private val _teams = MutableLiveData<Resource<List<Team>>>()
    val teams: LiveData<Resource<List<Team>>> = _teams

    fun loadTeams() {
        _teams.value = Resource.Loading()
        viewModelScope.launch {
            _teams.value = repository.getTeams()
        }
    }
}

// --- 6. ViewModel Factory ---
class TeamViewModelFactory(private val repository: TeamRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeamViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}