package com.ivangames.den

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvBigAvatar: TextView

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            // Тут мы меняем вид аватарки (пока просто текст, потом прикрутим картинку)
            tvBigAvatar.setBackgroundColor(0xFF333333.toInt())
            tvBigAvatar.text = "Фото"
            tvBigAvatar.textSize = 16f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvBigAvatar = findViewById(R.id.tvBigAvatar)
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvUsername = findViewById<TextView>(R.id.tvUsername)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnLogoutProfile = findViewById<Button>(R.id.btnLogoutProfile)
        val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)

        val name = intent.getStringExtra("name") ?: "Пользователь"
        val username = intent.getStringExtra("username") ?: "user"

        tvName.text = name
        tvUsername.text = "@$username"
        tvBigAvatar.text = name.take(1).uppercase()

        btnBack.setOnClickListener { finish() }

        val openGallery = { pickImage.launch("image/*") }
        tvBigAvatar.setOnClickListener { openGallery() }
        btnEditProfile.setOnClickListener { openGallery() }

btnLogoutProfile.setOnClickListener {
    // Очищаем память
    getSharedPreferences("den_prefs", MODE_PRIVATE).edit().clear().apply()
    val intent = Intent(this, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    finish()
}
    }
}
