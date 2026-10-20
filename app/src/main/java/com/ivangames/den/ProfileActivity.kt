package com.ivangames.den

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvBigAvatar = findViewById<TextView>(R.id.tvBigAvatar)
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvUsername = findViewById<TextView>(R.id.tvUsername)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnLogoutProfile = findViewById<Button>(R.id.btnLogoutProfile)
        val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)

        // Получаем данные, переданные из ленты
        val name = intent.getStringExtra("name") ?: "Пользователь"
        val username = intent.getStringExtra("username") ?: "user"

        // Устанавливаем текст
        tvName.text = name
        tvUsername.text = "@$username"
        tvBigAvatar.text = name.take(1).uppercase()

        btnBack.setOnClickListener {
            finish() // Возвращаемся назад в ленту
        }

        btnLogoutProfile.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnEditProfile.setOnClickListener {
            // Пока заглушка, сделаем позже
        }
    }
}
