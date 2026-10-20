package com.ivangames.den

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

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                tvStatus.text = "Заполни все поля!"
                return@setOnClickListener
            }
            tvStatus.text = "Отправляю на сервер..."
            sendRequest("/api/login", email, pass, tvStatus)
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                tvStatus.text = "Заполни все поля!"
                return@setOnClickListener
            }
            tvStatus.text = "Регистрирую..."
            sendRequest("/api/register", email, pass, tvStatus)
        }
    }

    private fun sendRequest(endpoint: String, email: String, password: String, tvStatus: TextView) {
        thread {
            try {
                val url = URL(serverUrl + endpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val jsonBody = JSONObject()
                jsonBody.put("email", email)
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
                        tvStatus.text = "Успех! Ответ: $response"
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
