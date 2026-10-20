package com.ivangames.den

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class FeedActivity : AppCompatActivity() {

    private val serverUrl = "https://den-8d9h.onrender.com"
    private var token = ""
    private var username = ""
    private var name = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        token = intent.getStringExtra("token") ?: ""
        username = intent.getStringExtra("username") ?: ""
        name = intent.getStringExtra("name") ?: ""

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navView)
        val btnMenu = findViewById<Button>(R.id.btnMenu)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val etNewPost = findViewById<EditText>(R.id.etNewPost)
        val btnSendPost = findViewById<Button>(R.id.btnSendPost)
        val postsContainer = findViewById<LinearLayout>(R.id.postsContainer)

        etNewPost.hint = "Что нового, $name?"

        // Открытие меню
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(navView)
        }

        // Клики по пунктам меню
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_feed -> { }
                R.id.nav_profile -> { }
                R.id.nav_friends -> { }
                R.id.nav_messages -> { }
                R.id.nav_settings -> { }
            }
            drawerLayout.closeDrawers()
            true
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnSendPost.setOnClickListener {
            val text = etNewPost.text.toString().trim()
            if (text.isNotEmpty()) {
                createPost(text, etNewPost)
            }
        }

        loadPosts(postsContainer)
    }

    private fun loadPosts(container: LinearLayout) {
        thread {
            try {
                val url = URL("$serverUrl/api/posts")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val postsArray = json.getJSONArray("posts")

                runOnUiThread {
                    container.removeAllViews()
                    if (postsArray.length() == 0) {
                        val tv = TextView(this)
                        tv.text = "Пока нет постов. Напиши первый!"
                        tv.setTextColor(0xFF888888.toInt())
                        container.addView(tv)
                    } else {
                        for (i in 0 until postsArray.length()) {
                            val post = postsArray.getJSONObject(i)
                            addPostToView(container, post)
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    val tv = TextView(this)
                    tv.text = "Ошибка загрузки: ${e.message}"
                    tv.setTextColor(0xFFFF0000.toInt())
                    container.addView(tv)
                }
            }
        }
    }

    private fun createPost(text: String, etNewPost: EditText) {
        thread {
            try {
                val url = URL("$serverUrl/api/posts")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.doOutput = true

                val jsonBody = JSONObject()
                jsonBody.put("text", text)

                val os = OutputStreamWriter(conn.outputStream)
                os.write(jsonBody.toString())
                os.flush()
                os.close()

                val responseCode = conn.responseCode
                if (responseCode in 200..299) {
                    runOnUiThread {
                        etNewPost.setText("")
                        loadPosts(findViewById(R.id.postsContainer))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addPostToView(container: LinearLayout, post: JSONObject) {
        val postLayout = LinearLayout(this)
        postLayout.orientation = LinearLayout.VERTICAL
        postLayout.setPadding(16, 16, 16, 16)
        postLayout.setBackgroundColor(0xFF1E1E1E.toInt())
        
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 16)
        postLayout.layoutParams = params

        val tvAuthor = TextView(this)
        tvAuthor.text = "${post.getString("authorName")} (@${post.getString("authorUsername")})"
        tvAuthor.setTextColor(0xFF4CAF50.toInt())
        tvAuthor.textSize = 14f

        val tvText = TextView(this)
        tvText.text = post.getString("text")
        tvText.setTextColor(0xFFFFFFFF.toInt())
        tvText.textSize = 16f
        tvText.setPadding(0, 8, 0, 0)

        postLayout.addView(tvAuthor)
        postLayout.addView(tvText)
        container.addView(postLayout)
    }
}
