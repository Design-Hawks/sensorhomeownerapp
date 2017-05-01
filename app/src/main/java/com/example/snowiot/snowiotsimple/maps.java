/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.snowiot.snowiotsimple;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class maps extends AppCompatActivity implements
        OnMapReadyCallback {

    int zoomInOnMapOnceFlag = 0;

    private Button mContactSensorOwner;

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;
    GoogleMap drivewayMap;                                                                                   //necessary to be able to use google maps functions
    Location userGPSLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderApi mFusedLocationProviderApi = LocationServices.FusedLocationApi;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);


        FirebaseDatabase Database = FirebaseDatabase.getInstance();

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);                    //"map" is fragment ID set
        mapFragment.getMapAsync(this);



    }

    @Override
    public void onMapReady(GoogleMap drivewayMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        this.drivewayMap = drivewayMap;

        /*LatLng sydney = new LatLng(-33.852, 151.211);
        drivewayMap.addMarker(new MarkerOptions().position(sydney)
                .title("Marker in Sydney"));
        drivewayMap.moveCamera(CameraUpdateFactory.newLatLng(sydney)); */

        loadDrivewayLocations();

        DatabaseReference userTypeRef = FirebaseDatabase.getInstance().getReference("driveways/" + ((GlobalVariables) this.getApplication()).getUserUID() + "/type");

        drivewayMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                View v = getLayoutInflater().inflate(R.layout.map_information_window, null);

                TextView markerInfo = (TextView) v.findViewById(R.id.markerInfo);
                ImageView infoWindowPicture = (ImageView) v.findViewById(R.id.infoWindowPicture);

//                Picasso.with(getApplicationContext()).load("http://i.imgur.com/ZMuAG6F.png").into(infoWindowPicture); //pass image into imgview

                infoWindowPicture.setImageResource(R.drawable.needsplowing);

                markerInfo.setText(marker.getSnippet());

                return v;
            }
        });

    }

    public void loadDrivewayLocations() {

        DatabaseReference drivewaysRef = FirebaseDatabase.getInstance().getReference("driveways");
        final String userUID = ((GlobalVariables) getApplication()).getUserUID();

        drivewaysRef.addChildEventListener(new ChildEventListener() {
            HashMap<String, Driveways> userMarkers = new HashMap<String, Driveways>();

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Driveways driveway = dataSnapshot.getValue(Driveways.class);

                    if ((dataSnapshot.getKey().equals(userUID))&&(zoomInOnMapOnceFlag == 0)) {
                        drivewayMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(driveway.getLatitude(), driveway.getLongitude()), 14));          //zoom in on user's marker
                        zoomInOnMapOnceFlag = 1;   //makes it so that the map doesn't zoom in everytime there is an update

                        if ((driveway.getType().equals("sensor")) && (driveway.getStatus() == 1)) {
                            drivewayMap.addMarker(new MarkerOptions().position(new LatLng(driveway.getLatitude(), driveway.getLongitude()))
                                    .title(dataSnapshot.getKey())                                                                                       //get node name, which should be user UID
                                    .snippet(driveway.getName() + " at:" + driveway.address.getStreet() + ", " + driveway.address.getCity() + ", " + driveway.address.getState())    //Tutorial on this code by "GDD Recife" on youtube
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));                                                                //Green means operating but not in need of service
                        } else if ((driveway.getType().equals("sensor")) && (driveway.getStatus() == 2)) {
                            drivewayMap.addMarker(new MarkerOptions().position(new LatLng(driveway.getLatitude(), driveway.getLongitude()))
                                    .title(dataSnapshot.getKey())
                                    .snippet(driveway.getName() + " at: " + driveway.address.getStreet() + ", " + driveway.address.getCity() + ", " + driveway.address.getState())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));                                                                  //Red means operating and in need of service
                        } else if ((driveway.getType().equals("sensor")) && (driveway.getStatus() == 3)) {
                            drivewayMap.addMarker(new MarkerOptions().position(new LatLng(driveway.getLatitude(), driveway.getLongitude()))
                                    .title(dataSnapshot.getKey())
                                    .snippet(driveway.getName() + " at: " + driveway.address.getStreet() + ", " + driveway.address.getCity() + ", " + driveway.address.getState())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));                                                                 //Blue means operating and already being serviced by a snowplow
                        }
                        userMarkers.put(dataSnapshot.getKey(), dataSnapshot.getValue(Driveways.class));
                    }

                       else if (driveway.getType().equals("plower") && (driveway.getStatus() != 0) && (!(dataSnapshot.getKey().equals(userUID)))) {
                            drivewayMap.addMarker(new MarkerOptions().position(new LatLng(driveway.getLatitude(), driveway.getLongitude()))                                 //Status: 0 = not on duty, 1 = on duty and standby, 2 = on duty but busy
                                    .title(dataSnapshot.getKey())
                                    .snippet(driveway.getName())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.plower)));
                        userMarkers.put(dataSnapshot.getKey(), dataSnapshot.getValue(Driveways.class));
                    }
                }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Driveways updatedDrivewayData = dataSnapshot.getValue(Driveways.class);

//                if (userMarkers.containsKey(dataSnapshot.getKey())) {

                    userMarkers.put(dataSnapshot.getKey(), updatedDrivewayData);
                    drivewayMap.clear();

                    for (String key : userMarkers.keySet()) {
                        Driveways driveway = userMarkers.get(key);
//
//                        Toast.makeText(getApplicationContext(), key, Toast.LENGTH_LONG).show();
//
                        if ((key.equals(userUID))) {
                            if ((driveway.getType().equals("sensor")) && (driveway.getStatus() == 1)) {
                                drivewayMap.addMarker(new MarkerOptions().position(new LatLng(driveway.getLatitude(), driveway.getLongitude()))
                                        .title(key)                                                                                       //get node name, which should be user UID
                                        .snippet(driveway.getName() + " at:" + driveway.address.getStreet() + ", " + driveway.address.getCity() + ", " + driveway.address.getState())    //Tutorial on this code by "GDD Recife" on youtube
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));                                                                //Green means operating but not in need of service
                            } else if ((driveway.getType().equals("sensor")) && (driveway.getStatus() == 2)) {
                                drivewayMap.addMarker(new MarkerOptions().position(new LatLng(driveway.getLatitude(), driveway.getLongitude()))
                                        .title(key)
                                        .snippet(driveway.getName() + " at: " + driveway.address.getStreet() + ", " + driveway.address.getCity() + ", " + driveway.address.getState())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));                                                                  //Red means operating and in need of service
                            } else if ((driveway.getType().equals("sensor")) && (driveway.getStatus() == 3)) {
                                drivewayMap.addMarker(new MarkerOptions().position(new LatLng(driveway.getLatitude(), driveway.getLongitude()))
                                        .title(key)
                                        .snippet(driveway.getName() + " at: " + driveway.address.getStreet() + ", " + driveway.address.getCity() + ", " + driveway.address.getState())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));                                                                 //Blue means operating and already being serviced by a snowplow
                            }
                        }

                        else if (driveway.getType().equals("plower") && (driveway.getStatus() != 0)) {
                            drivewayMap.addMarker(new MarkerOptions().position(new LatLng(driveway.getLatitude(), driveway.getLongitude()))                                 //Status: 0 = not on duty, 1 = on duty and standby, 2 = on duty but busy
                                    .title(key)
                                    .snippet(driveway.getName())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.plower)));
                        }
                    }

//                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





//        drivewaysRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Iterable<DataSnapshot> dataSnapshots = dataSnapshot.getChildren();
//                drivewayMap.clear();
//                for (DataSnapshot dataSnapshot1 : dataSnapshots) {
//                    Driveways driveway = dataSnapshot1.getValue(Driveways.class);
//
//                    if ((dataSnapshot1.getKey().equals(userUID))&&(driveway.getStatus() != 0)&&(zoomInOnMapOnceFlag == 0)) {
//                        drivewayMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(driveway.getLatitude(), driveway.getLongitude()), 14));          //zoom in on user's marker
//                        zoomInOnMapOnceFlag = 1;   //makes it so that the map doesn't zoom in everytime there is an update
//
//                        if ((driveway.getType().equals("sensor")) && (driveway.getStatus() == 1)) {
//                            drivewayMap.addMarker(new MarkerOptions().position(new LatLng(driveway.getLatitude(), driveway.getLongitude()))
//                                    .title(dataSnapshot1.getKey())                                                                                       //get node name, which should be user UID
//                                    .snippet(driveway.getName() + " at:" + driveway.address.getStreet() + ", " + driveway.address.getCity() + ", " + driveway.address.getState())    //Tutorial on this code by "GDD Recife" on youtube
//                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));                                                                //Green means operating but not in need of service
//                        } else if ((driveway.getType().equals("sensor")) && (driveway.getStatus() == 2)) {
//                            drivewayMap.addMarker(new MarkerOptions().position(new LatLng(driveway.getLatitude(), driveway.getLongitude()))
//                                    .title(dataSnapshot1.getKey())
//                                    .snippet(driveway.getName() + " at: " + driveway.address.getStreet() + ", " + driveway.address.getCity() + ", " + driveway.address.getState())
//                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));                                                                  //Red means operating and in need of service
//                        } else if ((driveway.getType().equals("sensor")) && (driveway.getStatus() == 3)) {
//                            drivewayMap.addMarker(new MarkerOptions().position(new LatLng(driveway.getLatitude(), driveway.getLongitude()))
//                                    .title(dataSnapshot1.getKey())
//                                    .snippet(driveway.getName() + " at: " + driveway.address.getStreet() + ", " + driveway.address.getCity() + ", " + driveway.address.getState())
//                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));                                                                 //Blue means operating and already being serviced by a snowplow
//                        } else if (driveway.getType().equals("plower") && (driveway.getStatus() != 0) && (!(dataSnapshot1.getKey().equals(userUID)))) {
//                            drivewayMap.addMarker(new MarkerOptions().position(new LatLng(driveway.getLatitude(), driveway.getLongitude()))                                 //Status: 0 = not on duty, 1 = on duty and standby, 2 = on duty but busy
//                                    .title(dataSnapshot1.getKey())
//                                    .snippet(driveway.getName())
//                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.plower)));
//                        }
//
//                    }
//
//                    else {
//                        if (driveway.getType().equals("plower") && (driveway.getStatus() != 0) && (!(dataSnapshot1.getKey().equals(userUID)))) {
//                            drivewayMap.addMarker(new MarkerOptions().position(new LatLng(driveway.getLatitude(), driveway.getLongitude()))                                 //Status: 0 = not on duty, 1 = on duty and standby, 2 = on duty but busy
//                                    .title(dataSnapshot1.getKey())
//                                    .snippet(driveway.getName())
//                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.plower)));
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }


}