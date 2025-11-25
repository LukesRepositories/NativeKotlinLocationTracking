package com.example.nativekotlinlocationtracking

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class RestGetActivity : AppCompatActivity() {

    private lateinit var getDataButton: Button
    private lateinit var responseText: TextView
    private lateinit var timeTakenText: TextView
    private lateinit var descriptionText: TextView
    private var job: Job? = null
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rest_get)

        // Initialise views
        getDataButton = findViewById(R.id.getDataButton)
        responseText = findViewById(R.id.responseText)
        timeTakenText = findViewById(R.id.timeTakenText)
        descriptionText = findViewById(R.id.descriptionText)

        // Set description text
        descriptionText.text = "Sends a REST GET to https://dummyjson.com/products/1"

        // Set click listener
        getDataButton.setOnClickListener {
            getData()
        }

        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun getData() {
        // Cancel any existing job
        job?.cancel()

        // Show loading state
        setLoadingState(true)

        // Run network request in background thread
        job = CoroutineScope(Dispatchers.IO).launch {
            val startTime = System.currentTimeMillis()

            try {
                val request = Request.Builder()
                    .url("https://dummyjson.com/products/1")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                val elapsedTime = System.currentTimeMillis() - startTime

                if (response.isSuccessful && responseBody != null) {
                    val jsonObject = JSONObject(responseBody)
                    val responseString = jsonObject.getString("title")

                    // Update UI on main thread
                    withContext(Dispatchers.Main) {
                        responseText.text = "returned GET: $responseString"
                        timeTakenText.text = "Time taken: ${formatElapsedTime(elapsedTime)}"
                        setLoadingState(false)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        responseText.text = "Error: ${response.code}"
                        timeTakenText.text = "Time taken: ${formatElapsedTime(elapsedTime)}"
                        setLoadingState(false)
                    }
                }
            } catch (e: IOException) {
                val elapsedTime = System.currentTimeMillis() - startTime
                withContext(Dispatchers.Main) {
                    responseText.text = "Error: ${e.message}"
                    timeTakenText.text = "Time taken: ${formatElapsedTime(elapsedTime)}"
                    setLoadingState(false)
                }
            }
        }
    }

    private fun formatElapsedTime(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val millis = milliseconds % 1000
        return String.format("%d.%03d seconds", seconds, millis)
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            getDataButton.text = "Loading..."
            getDataButton.isEnabled = false
            responseText.text = ""
            timeTakenText.text = ""
        } else {
            getDataButton.text = "Get REST API Data"
            getDataButton.isEnabled = true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}
