package com.example.nativekotlinlocationtracking

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class PrimeNumberActivity : AppCompatActivity() {

    private lateinit var calculateButton: Button
    private lateinit var primeNumberText: TextView
    private lateinit var timeTakenText: TextView
    private lateinit var progressBar: ProgressBar
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prime_number)

        // Initialize views
        calculateButton = findViewById(R.id.calculateButton)
        primeNumberText = findViewById(R.id.primeNumberText)
        timeTakenText = findViewById(R.id.timeTakenText)
        progressBar = findViewById(R.id.progressBar)

        // Set click listener
        calculateButton.setOnClickListener {
            getHighestPrime(250000)
        }

        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun isPrime(n: Int): Boolean {
        if (n < 2) {
            return false
        }
        for (i in 2 until n) {
            if (n % i == 0) {
                return false
            }
        }
        return true
    }

    private fun getHighestPrime(limit: Int) {
        // Cancel any existing job
        job?.cancel()

        // Show loading state
        setLoadingState(true)

        // Run calculation in background thread
        job = CoroutineScope(Dispatchers.Default).launch {
            val startTime = System.currentTimeMillis()
            var highestPrime = 2

            for (i in 2 until limit) {
                if (isPrime(i)) {
                    highestPrime = i
                }
            }

            val elapsedTime = System.currentTimeMillis() - startTime

            // Update UI on main thread
            withContext(Dispatchers.Main) {
                primeNumberText.text = highestPrime.toString()
                timeTakenText.text = formatElapsedTime(elapsedTime)
                setLoadingState(false)
            }
        }
    }

    private fun formatElapsedTime(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val millis = milliseconds % 1000
        return String.format("%d:%03d", seconds, millis)
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            calculateButton.text = "Calculating..."
            calculateButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
            primeNumberText.text = ""
            timeTakenText.text = ""
        } else {
            calculateButton.text = "Get prime number"
            calculateButton.isEnabled = true
            progressBar.visibility = View.GONE
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