package com.ivangames.den

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
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
        val storiesContainer = findViewById<LinearLayout>(R.id.storiesContainer)

        etNewPost.hint = "Что нового, $name?"
        addStories(storiesContainer)
        btnMenu.setOnClickListener { drawerLayout.openDrawer(navView) }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("name", name)
                    intent.putExtra("username", username)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawers()
            true
        }

btnLogout.setOnClickListener {
    // Очищаем память при выходе
    getSharedPreferences("den_prefs", MODE_PRIVATE).edit().clear().apply()
    startActivity(Intent(this, MainActivity::class.java))
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

    private fun addStories(container: LinearLayout) {
        val names = listOf("Моя", "Анна", "Игорь", "Мария", "Олег")
        for (i in names.indices) {
            val storyLayout = LinearLayout(this)
            storyLayout.orientation = LinearLayout.VERTICAL
            storyLayout.gravity = Gravity.CENTER
            storyLayout.setPadding(16, 0, 16, 0)

            val avatar = TextView(this)
            avatar.text = if (i == 0) "Моя" else names[i].take(1)
            avatar.setTextColor(Color.WHITE)
            avatar.textSize = if (i == 0) 14f else 24f
            avatar.gravity = Gravity.CENTER
            avatar.setBackgroundResource(R.drawable.avatar_bg)
            val size = (60 * resources.displayMetrics.density).toInt()
            avatar.layoutParams = LinearLayout.LayoutParams(size, size)
            avatar.setTypeface(null, Typeface.BOLD)

            if (i == 0) {
                avatar.setBackgroundColor(Color.parseColor("#E0E0E0"))
                avatar.setTextColor(Color.parseColor("#2196F3"))
                avatar.text = "+\nМоя"
            }

            val tvName = TextView(this)
            tvName.text = names[i]
            tvName.setTextColor(Color.BLACK)
            tvName.textSize = 12f
            tvName.gravity = Gravity.CENTER
            tvName.setPadding(0, 8, 0, 0)

            storyLayout.addView(avatar)
            storyLayout.addView(tvName)
            container.addView(storyLayout)
        }
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
                        tv.setTextColor(Color.GRAY)
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
                    tv.setTextColor(Color.RED)
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
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundResource(R.drawable.post_card_bg)
        card.setPadding(24, 24, 24, 24)
        
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 16)
        card.layoutParams = params

        val header = LinearLayout(this)
        header.orientation = LinearLayout.HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL

        val avatar = TextView(this)
        val authorName = post.getString("authorName")
        avatar.text = authorName.take(1)
        avatar.setTextColor(Color.WHITE)
        avatar.textSize = 16f
        avatar.gravity = Gravity.CENTER
        avatar.setBackgroundResource(R.drawable.avatar_bg)
        val size = (40 * resources.displayMetrics.density).toInt()
        avatar.layoutParams = LinearLayout.LayoutParams(size, size)
        avatar.setTypeface(null, Typeface.BOLD)

        val tvAuthor = TextView(this)
        tvAuthor.text = "$authorName\n@${post.getString("authorUsername")}"
        tvAuthor.setTextColor(Color.BLACK)
        tvAuthor.textSize = 14f
        tvAuthor.setPadding(16, 0, 0, 0)
        tvAuthor.setTypeface(null, Typeface.BOLD)

        header.addView(avatar)
        header.addView(tvAuthor)

        val tvText = TextView(this)
        tvText.text = post.getString("text")
        tvText.setTextColor(Color.BLACK)
        tvText.textSize = 16f
        tvText.setPadding(0, 16, 0, 16)

        val actions = LinearLayout(this)
        actions.orientation = LinearLayout.HORIZONTAL
        
        // Кнопка лайка
        val btnLike = TextView(this)
        val postId = post.getLong("id")
        val likesArray = post.getJSONArray("likes")
        var likesCount = likesArray.length()
        
        var isLiked = false
        for (i in 0 until likesArray.length()) {
            if (likesArray.getString(i) == username) {
                isLiked = true
                break
            }
        }

        btnLike.text = if (isLiked) "❤️ $likesCount" else "♡ $likesCount"
        btnLike.setTextColor(if (isLiked) Color.RED else Color.GRAY)
        btnLike.setPadding(0, 0, 24, 0)

        btnLike.setOnClickListener {
            thread {
                try {
                    val url = URL("$serverUrl/api/posts/$postId/like")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Authorization", "Bearer $token")
                    conn.doOutput = true
                    conn.outputStream.close()

                    if (conn.responseCode in 200..299) {
                        runOnUiThread {
                            loadPosts(findViewById(R.id.postsContainer))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val btnComment = TextView(this)
        btnComment.text = "💬 Комментарии"
        btnComment.setTextColor(Color.GRAY)

        actions.addView(btnLike)
        actions.addView(btnComment)

        card.addView(header)
        card.addView(tvText)
        card.addView(actions)
        
        container.addView(card)
    }
}
