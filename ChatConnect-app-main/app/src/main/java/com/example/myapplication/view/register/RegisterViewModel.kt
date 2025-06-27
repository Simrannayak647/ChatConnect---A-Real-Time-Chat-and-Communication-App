package com.example.myapplication.view.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

class RegisterViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth

    private val _email = MutableLiveData("")
    val email: LiveData<String> = _email

    private val _password = MutableLiveData("")
    val password: LiveData<String> = _password

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    fun setError(message: String) {
        _errorMessage.value = message
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun registerUser(home: () -> Unit, username: String) {
        if (_loading.value == false) {
            val email: String = _email.value ?: ""
            val password: String = _password.value ?: ""

            _loading.value = true

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    _loading.value = false
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            val user = hashMapOf(
                                "uid" to uid,
                                "email" to email,
                                "username" to username
                            )
                            Firebase.firestore.collection("users").document(uid).set(user)
                                .addOnSuccessListener {
                                    // ✅ Only navigate when Firestore save is successful
                                    home()
                                }
                                .addOnFailureListener {
                                    // ❌ Failed to write user to Firestore
                                    setError("Registration succeeded but saving user failed: ${it.message}")
                                }
                        }
                        else {
                            setError("User ID not found after registration.")
                        }

                    }

                    else {
                        setError(task.exception?.message ?: "Registration failed. Try again.")
                    }
                }
        }
    }
}
