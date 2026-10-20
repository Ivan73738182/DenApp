package com.ivangames.den

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        // Пока просто заглушка для кнопок, чтобы проверить, что приложение запускается
        btnLogin.setOnClickListener {
            val name = etUsername.text.toString()
            tvStatus.text = "Привет, $name! (Скоро тут будет вход)"
        }

        btnRegister.setOnClickListener {
            tvStatus.text = "Скоро тут будет регистрация"
        }
    }
}
