package edu.uw.ericchee.requestpermissionandfetchlocationprep.manager   // TODO: Don't forget to change this package to your own!!!!

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

private val TAG = SimpleLocationManager::class.java.simpleName

class SimpleLocationManager(private val context: Context){

    var onLocationUpdateListener: (Location) -> Unit = { }
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Returns true if has location permission
     */
    fun hasLocationPermission() = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    /**
     * Returns the last known location in the onLastLocation callback
     */
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(onLastLocation: (Location) -> Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            val lat = location?.latitude
            val long = location?.longitude
            Log.i(TAG, "$lat $long")
            if (location != null) {
                onLastLocation(location)
            }
        }
    }

    /**
     * Starts requesting location on an interval. Location will be notified via onLocationUpdateListener
     * @param intervalMillis Long represents how many milliseconds location interval should be fetched
     */
    @SuppressLint("MissingPermission")
    fun startRequestLocationUpdates(intervalMillis: Long = 5000) {
        val locationRequest = createLocationRequest(intervalMillis)
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    /**
     * Stops requesting location on an interval (this is to stop the call started by startRequestLocationUpdates())
     */
    fun stopLocationUpdates() { fusedLocationClient.removeLocationUpdates(locationCallback) }


    private fun createLocationRequest(intervalMillis: Long): LocationRequest {
        return LocationRequest.create().apply {
            interval = intervalMillis
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private val locationCallback: LocationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.i(TAG, "locationCallback - Retrieved location")
            locationResult.locations.forEach { location ->
                val lat = location.latitude
                val long = location.longitude
                Log.i(TAG, "$lat, $long")
                onLocationUpdateListener.invoke(location)
            }
        }
    }
}
