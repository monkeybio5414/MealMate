package com.comp3040.mealmate.Activity

import com.google.firebase.auth.FirebaseAuth

class FirebaseAuthWrapper(private val firebaseAuth: FirebaseAuth) {
    fun signIn(email: String, password: String) =
        firebaseAuth.signInWithEmailAndPassword(email, password)
}
