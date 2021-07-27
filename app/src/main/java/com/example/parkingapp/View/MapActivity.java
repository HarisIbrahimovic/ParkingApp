package com.example.parkingapp.View;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.parkingapp.R;
import com.example.parkingapp.SelectedFragments.SelectedMarker;
import com.example.parkingapp.SelectedFragments.iFragment;
import com.example.parkingapp.SelectedFragments.iMapBuy;
import com.example.parkingapp.SelectedFragments.redeemFragment;
import com.example.parkingapp.ViewModel.MapViewModel;
import com.example.parkingapp.SelectedFragments.buyFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, iMapBuy {
    private GoogleMap map;
    private LatLng PanonianLakes;
    private LatLng Omega;
    private LatLng BCC;
    private LatLng Mejdan;
    private LatLng Skver;
    private Fragment fragment;
    private Fragment tfragment;
    private Fragment redeemFragment;
    private iFragment testFragment;
    private iFragment buyFragment;
    private MapViewModel viewModel;
    private FloatingActionButton settings;
    private FloatingActionButton logOut;
    private String placeName, placeSpots;
    private String markerName;
    private Button creditsButton;
    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        viewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        viewModel.init();
        setUpFragments();
        onClicks();
        observe();
        checkLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        setUpLatLng();
        map = googleMap;
        setUpMarkers(map);
    }

    private void setUpMarkers(GoogleMap map) {
        map.setOnMarkerClickListener(this);
        map.addMarker(new MarkerOptions().position(PanonianLakes).title("Panonica"));
        map.addMarker(new MarkerOptions().position(Mejdan).title("Mejdan"));
        map.addMarker(new MarkerOptions().position(Skver).title("Skver"));
        map.addMarker(new MarkerOptions().position(Omega).title("Omega"));
        map.addMarker(new MarkerOptions().position(BCC).title("BCC"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(Mejdan,14.0f));
    }

    private void setUpLatLng() {
        PanonianLakes = new LatLng(44.540648, 18.676792);
        Mejdan = new LatLng(44.537485, 18.665029);
        Omega = new LatLng(44.538589, 18.663335);
        Skver = new LatLng(44.540536, 18.674114);
        BCC = new LatLng(44.532223, 18.652187);
    }


    private void observe() {
        viewModel.getAccepted().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer==2){
                    viewModel.setAccepted(0);
                    Toast.makeText(getApplicationContext(),"You don't have enough credits!",Toast.LENGTH_SHORT).show();
                }
                else if(integer==1){
                    Toast.makeText(getApplicationContext(),"Spot taken",Toast.LENGTH_SHORT).show();
                    viewModel.setAccepted(0);
                    getSupportFragmentManager().beginTransaction().
                            remove(getSupportFragmentManager().findFragmentById(R.id.testFragment)).commit();

                }else if(integer==3){
                    Toast.makeText(getApplicationContext(),"No Subscription",Toast.LENGTH_SHORT).show();
                    viewModel.setAccepted(0);
                }
            }
        });
    }

    private void onClicks() {
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
                finish();
            }
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.signOut();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });
        creditsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redeemFragment= new redeemFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.testFragment,redeemFragment).commit();
                iFragment rFragment = (iFragment)redeemFragment;
                rFragment.setParentActivity(MapActivity.this);
            }
        });
    }

    private void setUpFragments() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fragment = new SelectedMarker();
        testFragment = (iFragment) fragment;
        testFragment.setParentActivity(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.markerFragment,fragment).commit();
        settings = findViewById(R.id.settingsButton);
        logOut = findViewById(R.id.logoutButton);
        creditsButton = findViewById(R.id.reedemButton);
        client = LocationServices.getFusedLocationProviderClient(this);
        markerName="Mejdan";
    }

    private void checkLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(),"No permission for location.",Toast.LENGTH_SHORT).show();
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
                    LatLng myLoc = new LatLng(location.getLatitude(),location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions().position(myLoc).title("Myself")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    map.addMarker(markerOptions);
                }
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        markerName = marker.getTitle();
        if(marker.getTitle().equals("Myself"))return false;
        testFragment.updateInfo(markerName);
        return false;
    }

    @Override
    public void getData(String emptySlots, String name) {
        placeName= name;
        placeSpots = emptySlots;
        tfragment = new buyFragment();
        buyFragment= (iFragment) tfragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.testFragment,tfragment).commit();
        buyFragment.setParentActivity(this);
        buyFragment.updateInfo(placeName);
    }

    @Override
    public void submitData(String hours) {
        if(hours!=null){
            viewModel.takeSpot(placeName, placeSpots, hours);
        }
    }

    @Override
    public void refresh() {
        testFragment.updateInfo(markerName);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                getCurrentLocation();
        }
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().findFragmentById(R.id.testFragment) != null) {
            getSupportFragmentManager()
                    .beginTransaction().
                    remove(getSupportFragmentManager().findFragmentById(R.id.testFragment)).commit();
        }else finish();
    }



}