package edu.uw.ericchee.requestpermissionandfetchlocationprep

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.hulu.ericchee.requestpermissionandfetchlocationprep.databinding.ActivityMainBinding
import edu.uw.ericchee.requestpermissionandfetchlocationprep.manager.SimpleLocationManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val simpleLocationManager by lazy { SimpleLocationManager(this) }

    private val locationUpdate: (Location) -> Unit = { location ->
        Log.i("echee", "location: $location")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).apply { setContentView(root) }
        with(binding) {
            simpleLocationManager.onLocationUpdateListener = locationUpdate

            btnFetchLocation.setOnClickListener {
                if (simpleLocationManager.hasLocationPermission()) {
                    // start fetching location
                    fetchLocation()
                } else {
                    // Give reason why you need permission
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("This App needs Location permissions")
                        .setMessage("In order to check the nearest stores near you, this app needs location permission")
                        .setPositiveButton("Got it") { _, _ ->
                            // Tell os to ask for permission
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                        .create()
                        .show()
                }
            }

            btnStop.setOnClickListener {
                simpleLocationManager.stopLocationUpdates()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        simpleLocationManager.stopLocationUpdates()
    }

    private fun fetchLocation() {
        simpleLocationManager.startRequestLocationUpdates()
    }

    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Handle Permission is granted
            Toast.makeText(this, "Permission is granted!", Toast.LENGTH_SHORT).show()
            fetchLocation()
        } else {
           // Handle Permission is denied
            Toast.makeText(this, ":( Dang okay I see how it is...", Toast.LENGTH_SHORT).show()
        }
    }
}
