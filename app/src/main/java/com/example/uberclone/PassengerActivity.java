package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class PassengerActivity extends FragmentActivity implements OnMapReadyCallback , View.OnClickListener{

    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private Button btnRequestCar;
    private boolean isUberCancelled=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        btnRequestCar=findViewById(R.id.btnRequestCar);
        btnRequestCar.setOnClickListener(this);

        ParseQuery<ParseObject> carRequsetQuery=ParseQuery.getQuery("RequestCar");
        carRequsetQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        carRequsetQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if (objects.size()>0&&e==null){

                    isUberCancelled=false;
                    btnRequestCar.setText("Cancel your UBER Requst!");

                }

            }
        });

        findViewById(R.id.btnLogOutFromPassengerActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {

                        if (e==null){

                            Intent intent=new Intent(PassengerActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();


                        }

                    }
                });

            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager= (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

               updateCameraPassengerLocation(location);

            }
        };

//        if (Build.VERSION.SDK_INT<23){
//
//
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

        if (ContextCompat.checkSelfPermission(PassengerActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(PassengerActivity.this,new String[]{Manifest
                    .permission.ACCESS_FINE_LOCATION},1000);

        }else {

try {
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

    Location currentPassengerLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    assert currentPassengerLocation != null;
    updateCameraPassengerLocation(currentPassengerLocation);

}catch (Exception e){

    e.printStackTrace();

}

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==1000 &&grantResults.length>0 &&grantResults[0]==PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                assert currentPassengerLocation != null;
                updateCameraPassengerLocation(currentPassengerLocation);

            }
        }

    }
    private void updateCameraPassengerLocation(Location pLocation){




            LatLng passengerLocation = new LatLng(pLocation.getLatitude(), pLocation.getLongitude());
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 15));
            mMap.addMarker(new MarkerOptions().position(passengerLocation).title("You Are Here!!!"));


    }

    @Override
    public void onClick(View view) {
        if (isUberCancelled){

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location passengerCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (passengerCurrentLocation != null) {

                ParseObject requestCar = new ParseObject("RequestCar");
                requestCar.put("username", ParseUser.getCurrentUser().getUsername());

                ParseGeoPoint userLocation = new ParseGeoPoint(passengerCurrentLocation.getLatitude(), passengerCurrentLocation.getLongitude());

                requestCar.put("passengerLocation", userLocation);

                requestCar.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {

                        if (e == null) {

                            Toast.makeText(PassengerActivity.this, "CAR REQUEST SENT!", Toast.LENGTH_LONG).show();

                            btnRequestCar.setText(R.string.cancel_uber);
                            isUberCancelled=false;


                        }

                    }
                });

            } else {

                Toast.makeText(this, "NO CONNECTION!!!", Toast.LENGTH_LONG).show();
            }
        }

        }else {

           ParseQuery<ParseObject>carRequstQuery=ParseQuery.getQuery("RequestCar");
           carRequstQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
           carRequstQuery.findInBackground(new FindCallback<ParseObject>() {
               @Override
               public void done(List<ParseObject> requsetList, ParseException e) {

                   if (requsetList.size()>0&&e==null){

                       isUberCancelled=true;
                       btnRequestCar.setText(R.string.requset_a_new_car);

                       for (ParseObject uberRequest:requsetList){

                           uberRequest.deleteInBackground(new DeleteCallback() {
                               @Override
                               public void done(ParseException e) {

                                   if (e==null){

                                       Toast.makeText(PassengerActivity.this,"Requests Deleted",Toast.LENGTH_LONG).show();

                                   }

                               }
                           });

                       }

                   }

               }
           });

        }

    }
}