package com.example.shubh.currentlocationapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * <h1><font color="orange">MapsActivity</font></h1>
 * Activity class for Rendering the Google Map.
 *
 * @author Shubham Chauhan
 */

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMapClickListener {
    private double mLatitude;
    private double mLongitude;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = (long) 0.000001;
    private static final long MIN_TIME_BW_UPDATES = 60000;
    private GoogleMap mGoogleMap = null;
    private EditText mEditTextSource;
    private EditText mEditTextDestination;
    private LatLng mOrigin = null;
    private LatLng mDest = null;
    private static int mCount = 0;
    private ArrayList<LatLng> mMarkerPoints;
    private MarkerOptions mOriginMarkerOption;
    private MarkerOptions mDestMarkerOption;
    private Marker mMarkerOrigin;
    private Marker mMarkerDest;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mEditTextSource = (EditText) findViewById(R.id.editText_source);
        mEditTextDestination = (EditText) findViewById(R.id.editText_destination);
        mMarkerPoints = new ArrayList<>();
        mOriginMarkerOption = new MarkerOptions();
        mDestMarkerOption = new MarkerOptions();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        getLocation();
        mapFragment.getMapAsync(this);
    }


    /**
     * Method is used to get address by latitude and longitude
     */

    public String getAddress(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        String address;

        try {
            List<Address> e = geocoder.getFromLocation(lat, lon, 1);
            if (e != null) {
                Address returnedAddress = e.get(0);
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


    /**
     * Method is called when map is ready .
     */

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
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

    }

    /**
     * Method is used for updating a current location .
     * this method is called when current location is changed.
     */
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
            mMarkerPoints.add(currentLocation);
            mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    LinearLayout info = new LinearLayout(MapsActivity.this);
                    info.setOrientation(LinearLayout.VERTICAL);

                    TextView title = new TextView(MapsActivity.this);
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(R.string.current_location);

                    TextView snippet = new TextView(MapsActivity.this);
                    snippet.setText(getAddress(mLatitude, mLongitude));

                    info.addView(title);
                    info.addView(snippet);

                    return info;
                }
            });
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

    /**
     * Method is used to find current location by both Network Provider and GPS.
     * At the first time it used Network Provider.
     * After that when location is updated it used gps to track your current location.
     */
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
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        Toast.makeText(this, "isNetwork Enables", Toast.LENGTH_SHORT).show();
                    }
                }
                //get the location by gps
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        //Log.d("GPS Enabled", "GPS Enabled");
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            Toast.makeText(this, "is Gps Enables", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method is called when request for a permission.
     */
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
                        mGoogleMap.setMyLocationEnabled(true);
                        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                    }
                }
        }
    }

    /**
     * Method is called when you click on the map.
     * this method is used for add marker where you clicked.
     */
    @Override
    public void onMapClick(LatLng latLng) {
        if (mEditTextSource.hasFocus()) {
            if (mOrigin != null) {
                mMarkerPoints.remove(mOrigin);
                mCount--;
            } else {
                mMarkerOrigin = mGoogleMap.addMarker(mOriginMarkerOption.position(latLng));
            }
            mOrigin = latLng;
            mMarkerOrigin.setPosition(mOrigin);
            mCount++;
            mEditTextSource.setText(getAddress(latLng.latitude, latLng.longitude));
            mMarkerPoints.add(mOrigin);
        } else if (mEditTextDestination.hasFocus()) {
            if (mDest != null) {
                mMarkerPoints.remove(mDest);
                mCount--;
            } else {
                mMarkerDest = mGoogleMap.addMarker(mDestMarkerOption.position(latLng));
            }
            mDest = latLng;
            mMarkerDest.setPosition(mDest);
            mCount++;
            mEditTextDestination.setText(getAddress(latLng.latitude, latLng.longitude));
            mMarkerPoints.add(mDest);
        }

        if (mCount >= 2) {
            String url = getDirectionsUrl(mOrigin, mDest);
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);
        }

    }

    /**
     * Method is used to get Direction URL by Origin and Destination LatLng.
     */
    public String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;
        String output = "json";
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    /**
     * Method is used to get JSON Data from Direction Url.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String downloadUrl(String strUrl) throws IOException {
        String data = null;
        HttpURLConnection urlConnection;
        URL url = new URL(strUrl);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.connect();

        try (InputStream iStream = urlConnection.getInputStream()) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            data = stringBuilder.toString();
            bufferedReader.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * this class is used to get JSON data in non-ui thread by AsyncTask.
     */
    private class DownloadTask extends AsyncTask<String, Integer, String> {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    /**
     * this class is used to Parsing the data in non-ui thread by AsyncTask.
     */
    private class ParserTask extends AsyncTask<Object, Object, List<List<HashMap<String, String>>>> {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(Object... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject((String) jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            for (int row = 0; row < result.size(); row++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(row);

                for (int column = 0; column < path.size(); column++) {
                    HashMap<String, String> point = path.get(column);

                    double lat = Double.parseDouble(String.valueOf(point.get("lat")));
                    double lng = Double.parseDouble(String.valueOf(point.get("lng")));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);
            }
            // Drawing polyline in the Google Map for the i-th route
            mGoogleMap.addPolyline(lineOptions);
        }
    }
}
