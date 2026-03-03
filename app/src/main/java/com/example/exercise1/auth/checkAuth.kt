package com.example.exercise1.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth

    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        object ResetPasswordSent : AuthState()
        data class Error(val message: String) : AuthState()
    }

    //------------------ ลงทะเบียนใช้งานด้วย Email ------------------
    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: FirebaseAuthException) {
                val message = when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "อีเมลนี้มีผู้ใช้งานแล้ว"
                    "ERROR_WEAK_PASSWORD" -> "รหัสผ่านอ่อนเกินไป (ต้อง 6 ตัวอักษรขึ้นไป)"
                    "ERROR_INVALID_EMAIL" -> "รูปแบบอีเมลไม่ถูกต้อง"
                    else -> "เกิดข้อผิดพลาดในการสมัครสมาชิก กรุณาลองใหม่"
                }
                _authState.value = AuthState.Error(message)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("เกิดข้อผิดพลาดของระบบ")
            }
        }
    }

    //------------------ ล็อกอินด้วย Email ------------------555
    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: FirebaseAuthException) {
                val message = when (e.errorCode) {
                    "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "อีเมลหรือรหัสผ่านไม่ถูกต้อง"
                    "ERROR_INVALID_EMAIL" -> "รูปแบบอีเมลไม่ถูกต้อง"
                    "ERROR_USER_DISABLED" -> "บัญชีนี้ถูกระงับการใช้งาน"
                    else -> "เข้าสู่ระบบไม่สำเร็จ กรุณาลองใหม่"
                }
                _authState.value = AuthState.Error(message)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("เกิดข้อผิดพลาดของระบบ")
            }
        }
    }

    //------------------ ล็อกอินด้วย Google ------------------
    fun loginWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credentialManager = CredentialManager.create(context)

                // อัปเดต Client ID เป็นตัวใหม่เรียบร้อยแล้วครับ
                val signInWithGoogleOption = GetSignInWithGoogleOption
//                    หา client id มาใส่เอง
                    .Builder("")
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(
                        googleIdTokenCredential.idToken, null
                    )
                    auth.signInWithCredential(firebaseCredential).await()
                    _authState.value = AuthState.Success
                }

            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error("ยกเลิกการล็อกอิน หรือเกิดข้อผิดพลาด: ${e.message}")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error: ${e.message}")
            }
        }
    }

    //------------------ รีเซตรหัสผ่าน ------------------
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.ResetPasswordSent
            } catch (e: FirebaseAuthException) {
                val message = when (e.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "ไม่พบบัญชีนี้ในระบบ"
                    "ERROR_INVALID_EMAIL"  -> "รูปแบบ Email ไม่ถูกต้อง"
                    else -> "เกิดข้อผิดพลาด กรุณาลองใหม่"
                }
                _authState.value = AuthState.Error(message)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("เกิดข้อผิดพลาดของระบบ")
            }
        }
    }

    //------------------ ออกจากระบบ ------------------
    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
