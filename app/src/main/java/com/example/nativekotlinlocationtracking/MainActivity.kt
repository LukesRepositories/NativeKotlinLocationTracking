package com.example.nativekotlinlocationtracking

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var getLocationButton: Button
    private lateinit var goToRestGetPagButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        getLocationButton = findViewById(R.id.goToLocationPageButton)
        goToRestGetPagButton = findViewById(R.id.goToRestGetPageButton)

        // Set click listeners
        getLocationButton.setOnClickListener {
            val intent = Intent(this, LocationActivity::class.java)
            startActivity(intent)
        }

        goToRestGetPagButton.setOnClickListener {
            val intent = Intent(this, RestGetActivity::class.java)
            startActivity(intent)
        }

    }

}