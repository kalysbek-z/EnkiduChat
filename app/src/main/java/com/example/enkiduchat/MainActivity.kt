package com.example.enkiduchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        already_have.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        register_button.setOnClickListener {
            performRegister()
        }
    }

    private fun performRegister() {
        val email = email_adress.text.toString()
        val password = password_field.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                applicationContext,
                "Please, write your email and password!",
                Toast.LENGTH_SHORT
            ).show()
        } else {

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                }
                .addOnFailureListener {
                    Log.d("Main", "${it.message}")
                    Toast.makeText(
                        applicationContext,
                        "Incorrect email format",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            saveUserToFirebaseDB()
        }
    }

    private fun saveUserToFirebaseDB() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_field.text.toString())
        ref.setValue(user)
            .addOnSuccessListener {
                val intent = Intent(this, LastestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
    }
}