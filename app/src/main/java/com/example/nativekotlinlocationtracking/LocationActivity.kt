package com.example.nativekotlinlocationtracking

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class LocationActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var getLocationButton: Button
    private lateinit var errorContainer: LinearLayout
    private lateinit var errorText: TextView
    private lateinit var openSettingsButton: Button
    private lateinit var locationContainer: LinearLayout
    private lateinit var latitudeText: TextView
    private lateinit var longitudeText: TextView
    private lateinit var accuracyText: TextView
    private lateinit var altitudeText: TextView
    private lateinit var speedText: TextView
    private lateinit var bearingText: TextView
    private lateinit var timeTakenText: TextView
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        // Initialise location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialise views
        getLocationButton = findViewById(R.id.goToLocationPageButton)
        errorContainer = findViewById(R.id.errorContainer)
        errorText = findViewById(R.id.errorText)
        openSettingsButton = findViewById(R.id.openSettingsButton)
        locationContainer = findViewById(R.id.locationContainer)
        latitudeText = findViewById(R.id.latitudeText)
        longitudeText = findViewById(R.id.longitudeText)
        accuracyText = findViewById(R.id.accuracyText)
        altitudeText = findViewById(R.id.altitudeText)
        speedText = findViewById(R.id.speedText)
        bearingText = findViewById(R.id.bearingText)
        timeTakenText = findViewById(R.id.timeTakenText)

        // Set click listeners
        getLocationButton.setOnClickListener {
            getCurrentLocation()
        }

        openSettingsButton.setOnClickListener {
            openAppSettings()
        }

        // Check permissions on start
        checkLocationPermissions()
    }

    private fun checkLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                hideError()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                // Show explanation and request permission
                showPermissionRationale()
            }
            else -> {
                // Request permission
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Required")
            .setMessage("This app needs location permission to show your current location.")
            .setPositiveButton("Grant Permission") { _, _ ->
                requestLocationPermission()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hideError()
                } else {
                    showError("Location permission denied. Please enable it in settings.", true)
                }
            }
        }
    }

    private fun getCurrentLocation() {
        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showError("Location permission not granted", true)
            requestLocationPermission()
            return
        }

        // Show loading state
        setLoadingState(true)
        hideError()
        locationContainer.visibility = View.GONE

        try {
            val cancellationTokenSource = CancellationTokenSource()

            // Timing starts here
            val startTime = System.currentTimeMillis()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                setLoadingState(false)

                // Stop timing
                val elapsedTime = System.currentTimeMillis() - startTime
                timeTakenText.text = "${elapsedTime} ms"

                if (location != null) {
                    displayLocation(location) // displayLocation Function below this function
                } else {
                    showError("Unable to get location. Please try again.", false)
                }
            }.addOnFailureListener { exception ->
                setLoadingState(false)
                showError("Error getting location: ${exception.message}", false)
            }
        } catch (e: Exception) {
            setLoadingState(false)
            showError("Error getting location: ${e.message}", true)
        }
    }

    private fun displayLocation(location: Location) {
        locationContainer.visibility = View.VISIBLE

        latitudeText.text = location.latitude.toString()
        longitudeText.text = location.longitude.toString()
        accuracyText.text = "${String.format("%.2f", location.accuracy)} metres"
        altitudeText.text = "${String.format("%.2f", location.altitude)} metres"
        speedText.text = "${String.format("%.2f", location.speed)} m/s"
        bearingText.text = "${String.format("%.2f", location.bearing)}°"

    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            getLocationButton.text = "Getting Location..."
            getLocationButton.isEnabled = false
        } else {
            getLocationButton.text = "Get My Location"
            getLocationButton.isEnabled = true
        }
    }

    private fun showError(message: String, showSettingsButton: Boolean) {
        errorContainer.visibility = View.VISIBLE
        errorText.text = message
        openSettingsButton.visibility = if (showSettingsButton) View.VISIBLE else View.GONE
    }

    private fun hideError() {
        errorContainer.visibility = View.GONE
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}
