package com.swhite.javagpsdemo;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.swhite.javagpsdemo.databinding.ActivityMaps2Binding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //GPS and map variables.
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback locationCallback;

    //Final variables.
    private static final int GPS_INTERVAL = 30 * 1000;
    private static final int REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.swhite.javagpsdemo.databinding.ActivityMaps2Binding binding = ActivityMaps2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Creating the client, request and callback to be used for getting location.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = createLocationRequest();
        createLocationCallback();

    }

    //Start location updates when app is brought back into foreground.
    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    //Stops location updates when app is no longer in foreground.
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    //Stop GPS updates.
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    //Sets up the inital map.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker to 0,0 on the map as a default location.
        LatLng defaultLoc = new LatLng(0, 0);
        mMap.addMarker(new MarkerOptions().position(defaultLoc).title("Default"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLoc));
    }

    //Begin location updates.
    @SuppressLint("MissingPermission") //Suppress warning because we're checking this ourselves.
    private void startLocationUpdates() {
        if(checkLocationPermissions()) {
            fusedLocationClient.requestLocationUpdates(mLocationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        } else {
            requestPermissions();
        }
    }

    //Creates a location requests and sets the GPS interval.
    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(GPS_INTERVAL);
        return locationRequest;
    }

    //Creates a location callback, update with new location if we get one.
    private void createLocationCallback(){
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocation(location);
                }
            }
        };
    }

    //Update GPS location on the map.
    private void updateLocation(Location location){

        //Get the co-ords of the location.
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        //Log for debug purposes.
        Log.d("Co-ords", latitude + " " + longitude);

        //Clear the map before adding new marker.
        mMap.clear();

        //Create a new map location and adjust the camera.
        LatLng newLoc = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(newLoc).title("Current Location: " +
                latitude + " " + longitude ));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newLoc));
    }

    //Checks foreground location permissions given to the app.
    private boolean checkLocationPermissions(){
        boolean hasFineLocation = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarseLocation = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return hasFineLocation && hasCoarseLocation;
    }

    //Requests foreground location permissions from the user.
    private void requestPermissions(){
        ActivityCompat.requestPermissions(MapsActivity.this, new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_CODE);
    }
}