package com.example.hackathonexperiment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    SearchView searchView;
    private List<LatLng> polygonPoints = new ArrayList<>();
    private Polygon polygon;
    private Button button, delBtn;
    private List<com.google.android.gms.maps.model.Circle> circles = new ArrayList<>(); // List to hold circles

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.drawButton);
        delBtn = findViewById(R.id.deleteButton);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawPolygon();
            }
        });
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearPolygons();
            }
        });
    }

    private void clearPolygons() {
        // Remove the polygon if it exists
        if (polygon != null) {
            polygon.remove();
            polygon = null; // Clear the reference
        }

        // Remove all circles
        for (com.google.android.gms.maps.model.Circle circle : circles) {
            circle.remove();
        }
        circles.clear(); // Clear the list of circles
    }

    // Method to search for a location
    private void searchLocation(String location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(location, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                // Move the camera to the new location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                // Add a marker at the new location
                mMap.addMarker(new MarkerOptions().position(latLng).title(address.getAddressLine(0)));
                mMap.addCircle(new CircleOptions()
                        .center(latLng) // Set the center to the searched location
                        .radius(100) // Radius in meters (adjust as needed)
                        .strokeColor(0xFF0000FF) // Blue outline
                        .fillColor(0x220000FF) // Light blue fill
                        .strokeWidth(5)); // Width of the circle outline
            } else {
                Log.d("SearchLocation", "Location not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("SearchLocation", "Geocoder service not available");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermission();
        mMap.setOnMapClickListener(latLng -> {
            // Add the point to the polygon points list
            polygonPoints.add(latLng);
            // Draw a circle at the touched position
            com.google.android.gms.maps.model.Circle circle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(5) // radius in meters
                    .strokeColor(Color.BLUE) // border color
                    .fillColor(Color.argb(128, 0, 0, 255))); // fill color with transparency
            circles.add(circle); // Add the circle to the list
        });
    }

    private void drawPolygon() {
        // Clear any existing polygons
        if (polygon != null) {
            polygon.remove();
        }

        // Create a polygon on the map using the points
        PolygonOptions polygonOptions = new PolygonOptions()
                .addAll(polygonPoints) // Add the points to the polygon
                .strokeColor(Color.RED) // Set the outline color
                .fillColor(0x220FF000) // Set the fill color with transparency
                .strokeWidth(5); // Set the stroke width

        // Add the polygon to the map
        polygon = mMap.addPolygon(polygonOptions);
    }

    // Check if location permission is granted
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, get the location
            getCurrentLocation();
        }
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                // Permission denied, show a message to the user
            }
        }
    }

    private void getCurrentLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // You can also request permissions here
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Create a LatLng object for the current location
                        LatLng userLocation = new LatLng(latitude, longitude);

                        // Move the camera to the user's location
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                        // Add a marker at the user's location
                        mMap.addMarker(new MarkerOptions().position(userLocation).title("My Location"));
                    } else {
                        // Handle case where location is null
                        Log.d("Location", "Location is null");
                    }
                });
    }
}
