package com.example.shubh.currentlocationapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener,GoogleMap.OnMapClickListener {
    private double mLatitude;
    private double mLongitude;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = (long) 0.000001;
    private static final long MIN_TIME_BW_UPDATES = 60000;
    private GoogleMap mGoogleMap = null;
    private EditText mEditTextSource;
    private EditText mEditTextDestination;

    // AIzaSyCUTMsxe0fumDIwSs_X4yhjtqH-e3LY5Kw

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mEditTextSource=(EditText)findViewById(R.id.editText_source);
        mEditTextDestination=(EditText)findViewById(R.id.editText_destination);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        getLocation();
        mapFragment.getMapAsync(this);
    }


    //getting Address
    public String getAddress(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        String address = "";

        try {
            List e = geocoder.getFromLocation(lat, lon, 1);
            if (e != null) {
                Address returnedAddress = (Address) e.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); ++i) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }

                address = strReturnedAddress.toString();
            } else {
                address = "No Address returned!";
            }
        } catch (IOException var9) {
            var9.printStackTrace();
            address = "Can\'t get Address!";
        }

        return address;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setOnMapClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //TODO: Consider calling
            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.INTERNET}, 10);
            return;
        }

        loadMap(googleMap);
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

    }

    private void loadMap(GoogleMap googleMap) {
        LatLng redFort = new LatLng(28.6562, 77.2410);
        googleMap.addMarker(new MarkerOptions()
                .title("Red Fort")
                .snippet("" + getAddress(28.6562, 77.2410))
                .position(redFort));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(redFort, 13));
    }

    @Override
    public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        LatLng currentLocation = new LatLng(mLatitude, mLongitude);
        if (mGoogleMap != null) {
            mGoogleMap.addMarker(new MarkerOptions()
                    .title("Current Location")
                    .snippet("" + getAddress(mLatitude, mLongitude))
                    .position(currentLocation));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13));
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getLocation() {
        try {

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                getLocation();

            } else {
                // First get location from Network Provider
                Location location = null;
                if (isNetworkEnabled) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        //TODO: Consider calling
                        requestPermissions(
                                new String[]{
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.INTERNET}, 10);
                    }

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MapsActivity.MIN_TIME_BW_UPDATES, MapsActivity.MIN_DISTANCE_CHANGE_FOR_UPDATES, MapsActivity.this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            Toast.makeText(this, "isNetwork Enables", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                //get the location by gps
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        //Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                Toast.makeText(this, "is Gps Enables", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mGoogleMap != null) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        getLocation();
                        loadMap(mGoogleMap);
                        mGoogleMap.setMyLocationEnabled(true);
                        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                    }

                    return;
                }

        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        /*if(latLngList.size() > 0){
            //refreshMap(mMap);
            latLngList.clear();
        }
        latLngList.add(latLng);
        Log.d(TAG, "Marker number " + latLngList.size());*/
        //mGoogleMap.addMarker(yourLocationMarker);
        mGoogleMap.addMarker(new MarkerOptions().position(latLng));
        if(mEditTextSource.hasFocus())
        mEditTextSource.setText(getAddress(latLng.latitude,latLng.longitude));
        else if(mEditTextDestination.hasFocus())
            mEditTextDestination.setText(getAddress(latLng.latitude,latLng.longitude));
        //LatLng defaultLocation = yourLocationMarker.getPosition();
        //LatLng destinationLocation = latLng;
        //use Google Direction API to get the route between these Locations
        //String directionApiPath = Helper.getUrl(String.valueOf(defaultLocation.latitude), String.valueOf(defaultLocation.longitude),
                //String.valueOf(destinationLocation.latitude), String.valueOf(destinationLocation.longitude));
        //Log.d(TAG, "Path " + directionApiPath);
        //getDirectionFromDirectionApiServer(directionApiPath);
    }
}
