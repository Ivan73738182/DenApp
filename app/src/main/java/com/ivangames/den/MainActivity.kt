package com.ivangames.den

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val serverUrl = "https://den-8d9h.onrender.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etName = findViewById<EditText>(R.id.etName)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (username.isEmpty() || pass.isEmpty()) {
                tvStatus.text = "Для входа нужны юз и пароль!"
                return@setOnClickListener
            }
            tvStatus.text = "Вхожу..."
            sendRequest("/api/login", "", username, pass, tvStatus)
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (name.isEmpty() || username.isEmpty() || pass.isEmpty()) {
                tvStatus.text = "Заполни все поля!"
                return@setOnClickListener
            }
            tvStatus.text = "Регистрирую..."
            sendRequest("/api/register", name, username, pass, tvStatus)
        }
    }

    private fun sendRequest(endpoint: String, name: String, username: String, password: String, tvStatus: TextView) {
        thread {
            try {
                val url = URL(serverUrl + endpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val jsonBody = JSONObject()
                if (name.isNotEmpty()) jsonBody.put("name", name)
                jsonBody.put("username", username)
                jsonBody.put("password", password)

                val os = OutputStreamWriter(conn.outputStream)
                os.write(jsonBody.toString())
                os.flush()
                os.close()

                val responseCode = conn.responseCode
                val responseStream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
                val response = responseStream.bufferedReader().use { it.readText() }

                runOnUiThread {
                    if (responseCode in 200..299) {
                        try {
                            val json = JSONObject(response)
                            val token = json.getString("token")
                            val serverName = json.getString("name")
                            val serverUsername = json.getString("username")

                            // Переключаемся на Ленту
                            val intent = Intent(this@MainActivity, FeedActivity::class.java)
                            intent.putExtra("token", token)
                            intent.putExtra("name", serverName)
                            intent.putExtra("username", serverUsername)
                            startActivity(intent)
                            finish()
                        } catch (e: Exception) {
                            tvStatus.text = "Ошибка перехода: ${e.message}"
                        }
                    } else {
                        tvStatus.text = "Ошибка ($responseCode): $response"
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    tvStatus.text = "Ошибка сети: ${e.message}"
                }
            }
        }
    }
}
