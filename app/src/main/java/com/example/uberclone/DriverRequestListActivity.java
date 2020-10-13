package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button btnGetRequsts;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ListView listView;
    private ArrayList<String>nearByDriveRequests;
    private ArrayAdapter adapter;
    private ArrayList<Double> passengersLatitudes;
    private ArrayList<Double> passengersLongitudes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);

        btnGetRequsts=findViewById(R.id.btngetRequsets);
        btnGetRequsts.setOnClickListener(this);

        listView = findViewById(R.id.requestListView);
        nearByDriveRequests = new ArrayList<>();
        passengersLatitudes=new ArrayList<>();
        passengersLongitudes=new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nearByDriveRequests);
        listView.setAdapter(adapter);

        nearByDriveRequests.clear();

        locationManager= (LocationManager) getSystemService(LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT<23||ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

            locationListener=new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                }
            };

        }

        listView.setOnItemClickListener(this);







    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.driver_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId()==R.id.driverLogOutItem){

            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {

                    if (e==null){

                        Intent intent=new Intent(DriverRequestListActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();

                    }

                }
            });

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {




        if (Build.VERSION.SDK_INT<23) {


            Location currentDriverLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestsListView(currentDriverLocation);
        }else if (Build.VERSION.SDK_INT>=23){

            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(DriverRequestListActivity.this,new String[]{Manifest
                        .permission.ACCESS_FINE_LOCATION},1000);

            }else {

               // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location currentDriverLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestsListView(currentDriverLocation);

            }

        }

    }

    private void updateRequestsListView(Location driverLocation) {

        //LatLng DriverLocation=new LatLng(location.getLatitude(),location.getLongitude());

        if (driverLocation != null) {


            final ParseGeoPoint driveCurrentLocation=new ParseGeoPoint(driverLocation.getLatitude(),driverLocation.getLongitude());

            ParseQuery<ParseObject> requestCarQuery=ParseQuery.getQuery("RequestCar");

            requestCarQuery.whereNear("passengerLocation",driveCurrentLocation);
            requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if (e == null) {
                        MediaPlayer mp3=MediaPlayer.create(DriverRequestListActivity.this,R.raw.uber_driver);
                        if (objects.size() > 0) {
                            if (nearByDriveRequests.size()>0){
                                nearByDriveRequests.clear();
                            }
                            if (passengersLatitudes.size()>0){
                                passengersLatitudes.clear();
                            }
                            if (passengersLongitudes.size()>0){
                                passengersLongitudes.clear();
                            }
                            for (ParseObject nearRequest : objects) {



                                ParseGeoPoint pLocation= (ParseGeoPoint) nearRequest.get("passengerLocation");
                                Double milesDistanceToPassenger = driveCurrentLocation.distanceInMilesTo(pLocation);

                                //NOT USER FRIENDLY 5.354084193581 * 10
                                //RESULT 53.2322
                                //53
                                float roundedDistanceValue = Math.round(milesDistanceToPassenger * 10) / 10;
                                nearByDriveRequests.add("There are " + roundedDistanceValue + " miles to " + nearRequest.get("username"));
                                passengersLatitudes.add(pLocation.getLatitude());
                                passengersLongitudes.add(pLocation.getLongitude());
                                mp3.start();

                            }


                        }else {

                            Toast.makeText(DriverRequestListActivity.this,"Sorry There are no requests yet!",Toast.LENGTH_LONG)
                                    .show();
mp3.stop();
                        }

                        adapter.notifyDataSetChanged();

                    }
                }
            });

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==1000 &&grantResults.length>0 &&grantResults[0]==PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                updateRequestsListView(currentDriverLocation);

            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        Toast.makeText(this,"Clicked",Toast.LENGTH_LONG).show();

    }
}