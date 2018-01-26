package com.example.michele.mypartyorganizer_new;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapActivity extends AppCompatActivity implements LocationListener {

    private boolean locationResult;
    MapView mMapView;
    private Marker marker;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private LatLng latLng;
    private double latitude;
    private double longitude;
    private double latitude_intent;
    private double longitude_intent;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        latitude = 0;
        longitude = 0;
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        try {
            // Initialize the map
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        locationResult = checkLocationPermission();
        if (locationResult) {
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {
                    googleMap = mMap;
                    // For showing a move to my location button
                    if (ActivityCompat.checkSelfPermission(MapActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    // In order to get the position
                    googleMap.setMyLocationEnabled(true);
                    // Get LocationManager service
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager != null) {
                        // Listener for location updates from GPS
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, MapActivity.this);
                        // Listener for location updates from Internet
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, MapActivity.this);
                        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MIN_TIME, MIN_DISTANCE, MapActivity.this);
                    }
                    Intent i = getIntent();
                    latitude_intent = i.getDoubleExtra("latitude",0);
                    longitude_intent = i.getDoubleExtra("longitude",0);
                    // If the position had already been set show it as a marker
                    if (latitude_intent != 0 && longitude_intent != 0) {
                        LatLng latLng1 = new LatLng(latitude_intent, longitude_intent);
                        String myLat = String.valueOf(latitude_intent);
                        String myLong = String.valueOf(longitude_intent);
                        marker = googleMap.addMarker(new MarkerOptions().position(latLng1).title(myLat + " " + myLong));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1, 10));
                    }
                }
            });
        }
    }

    // Handle the map
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    // What to do when location changes
    @Override
    public void onLocationChanged(Location location) {
        // Get the new location
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        // Place camera on the new position with zoom = 10
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        googleMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);
    }

    // When the user search an address
    public void onMapSearch(View view) {
        // It works only if the location permission was granted
        boolean locationResult = checkOnlyLocationPermission();
        if (locationResult) {
            // Remove the previous marker if there is
            if (marker != null)
                marker.remove();
            // Get the address
            EditText locationSearch = (EditText) findViewById(R.id.editText);
            String location = locationSearch.getText().toString();
            List<Address> addressList = null;
            if (location != null || !location.equals("")) {
                // Search for the address
                Geocoder geocoder = new Geocoder(this);
                try {
                    addressList = geocoder.getFromLocationName(location, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addressList.isEmpty()) {
                    AlertDialog.Builder b = new AlertDialog.Builder(MapActivity.this);
                    b.setTitle(R.string.no_location_found_title);
                    b.setMessage(R.string.no_location_found);
                    b.setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    // Non fa niente
                                }
                            });
                    b.setIcon(android.R.drawable.ic_dialog_alert);
                    b.show();
                }
                else {
                    Address address = addressList.get(0);
                    latitude = address.getLatitude();
                    longitude = address.getLongitude();
                    latLng = new LatLng(latitude, longitude);
                    marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                }

            }
        }
    }

    // Add the location to the party
    public void addLocation(View v) {
        if (latitude == 0 && longitude == 0) {
            // No location
            AlertDialog.Builder build = new AlertDialog.Builder(MapActivity.this);
            build.setTitle(R.string.choose_location);
            build.setMessage(R.string.choose_location_dialog);
            build.setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int id) {
                            // Non fa niente
                        }
                    });
            build.setIcon(android.R.drawable.ic_dialog_alert);
            build.show();
        }
        else {
            AlertDialog.Builder build = new AlertDialog.Builder(MapActivity.this);
            build.setTitle(R.string.location_added);
            build.setMessage(R.string.location_added_dialog);
            build.setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int id) {
                            // Return the location
                            Intent i = new Intent();
                            i.putExtra("latitude", latitude);
                            i.putExtra("longitude", longitude);
                            setResult(Activity.RESULT_OK,i);
                            finish();
                        }
                    });
            build.setIcon(android.R.drawable.ic_dialog_info);
            build.show();
        }

    }

    // Check if the App has location permission, if not try to get it
    public boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // Should show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation
                AlertDialog.Builder build = new AlertDialog.Builder(MapActivity.this);
                build.setTitle(R.string.location_permission_title);
                build.setMessage(R.string.location_permission_text);
                build.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                ActivityCompat.requestPermissions(MapActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        });
                build.setIcon(android.R.drawable.ic_dialog_info);
                build.show();
            } else {
                // Don't need to show an explanation
                ActivityCompat.requestPermissions(MapActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
        else {
            return true;
        }
    }

    // Check if the App has location permission
    public boolean checkOnlyLocationPermission() {
        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        else {
            return true;
        }
    }

    // Check if location permission was granted, if yes update MapActivity
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted
                    if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMapView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap mMap) {
                                googleMap = mMap;
                                if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    return;
                                }
                                googleMap.setMyLocationEnabled(true);
                                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                if (locationManager != null) {
                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, MapActivity.this);
                                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, MapActivity.this);
                                    locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MIN_TIME, MIN_DISTANCE, MapActivity.this);
                                }
                            }
                        });
                    }
                } else {
                    // Permission was not granted, do nothing
                }
            }
        }
    }

    // Handle the location detection
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }

}
