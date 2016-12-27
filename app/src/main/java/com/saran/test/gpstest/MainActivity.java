package com.saran.test.gpstest;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

/*Fabric crashlytics is used*/

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSION_REQUEST_CODE = 11;
    private GoogleMap gMap;
    private Marker marker;
    MapFragment mapFragmet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.map_layout);

        mapFragmet = (MapFragment)getFragmentManager().findFragmentById(R.id.map1);
        mapFragmet.getMapAsync(this);
    }


    private void setLocation(){
        try {

            gMap.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = false;
            boolean netwrok_enabled = false;
            try{
                gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }catch (Exception e){
                e.printStackTrace();
            }

            try {
                netwrok_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            }catch (Exception e){
                e.printStackTrace();
            }
            if(!gps_enabled && !netwrok_enabled){
                new AlertDialog.Builder(this)
                        .setTitle("GPS request")
                        .setMessage("GPS is not enabled")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                MainActivity.this.startActivity(intent);
                            }
                        }).show();
            }else {
                List<String> providers = locationManager.getProviders(true);
                Location bestLocation = null;
                for (String provider : providers) {
                    Location l = locationManager.getLastKnownLocation(provider);
                    if (l == null) {
                        continue;
                    }
                    if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                        // Found best last known location: %s", l);
                        bestLocation = l;
                    }
                }

                //Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng latLng = new LatLng(bestLocation.getLatitude(),bestLocation.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                markerOptions.title("Current Position");

                if(marker!=null){
                    marker = null;
                }

                marker = gMap.addMarker(markerOptions);

                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                Geocoder geocoder;
                List<android.location.Address> addresses;
                geocoder = new Geocoder(this, Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(bestLocation.getLatitude(),bestLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName();
                }catch (IOException e){
                    e.printStackTrace();
                }

            }
            } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
//        client.disconnect();
        super.onPause();
    }


    public void getPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
    }


    public boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else
            return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setLocation();
                } else {
                    Toast.makeText(this,"Permission not granted",Toast.LENGTH_SHORT);
                }
                return;
            }
        }
    }


    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        if(checkPermissions()){

//            try{
//                googleMap.setMyLocationEnabled(true);
////                Location location = googleMap.getMyLocation();
//                LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
//
//                List<String> providers = locationManager.getProviders(true);
//
//                Criteria criteria = new Criteria();
//                String provider = locationManager.getBestProvider(criteria, false);
//                Location location = locationManager.getLastKnownLocation(provider);
//
//                if (location != null) {
//                    Double lat = location.getLatitude();
//                    Double lng = location.getLongitude();
//                }
//
//            }catch (SecurityException e){
//                e.printStackTrace();
//            }

            setLocation();
        }else {
            getPermissions();
        }
    }
}
