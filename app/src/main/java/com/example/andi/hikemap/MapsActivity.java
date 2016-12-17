package com.example.andi.hikemap;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.andi.hikemap.GlobalDefinitions.MARKER_DISTANCES;
import static com.example.andi.hikemap.GlobalDefinitions.MARKER_LATITUDES;
import static com.example.andi.hikemap.GlobalDefinitions.MARKER_LONGITUDES;
import static com.example.andi.hikemap.GlobalDefinitions.SAVED_DATA;
import static com.example.andi.hikemap.R.id.map;


public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {


    private static final String TAG = "MAP!!!!!";
    private static final int FIREBASE_REQUEST_CODE = 676;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1; // random integer that stands for a permission
    private static final int CAMERA_ANIMATION_TIME_MS = 300;
    private static final int DELETE_MARKER_DISTANCE = 30;


    private static final float MIN_LOCATION_REFRESH_DISTANCE_M = 1;  // in meters
    private static final long MIN_LOCATION_REFRESH_TIME_MS = 2 * 1000;        // in ms
    private static final int POLYLINE_COLOR_DEFINED_ROUTE = 0x8F0009FF;   // semitransparent blue
    private static final int POLYLINE_COLOR_WALKED_ROUTE = 0x8F00FF09;   // semitransparent green


    // Attributes that are stored in the bundle in order to recreate the activity after
    // it was destroyed
    private static final String ZOOM_LEVEL = "zoom";  // The keys for the key value pair store

    private double[] mMarkerLongitudes; // The actual data
    private double[] mMarkerLatitudes;
    private float mInitCreateRouteModeZoom = 15.f;    // Set zoom when the activity is started for the
    // first time, then it changes to the current zoom level


    private final List<Marker> mMarkerList = new ArrayList<>();
    private float mZIndex = 0.0f; // Defines the layer on which the markers lie
    private int mMarkerNo = 0;  // Number of created markers


    private Polyline mDefinedRoutePolyline; // The route the user defines
    private Polyline mWalkedRoutePolyline;  // The route the user actually walks
    private final List<Location> mWalkedLocationsList = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient = null;
    private LocationManager mLocationManager = null;
    private LocationListener mLocationListener;
    private Location mLastLocation;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private boolean mNavigationModeOn = false;


    private float mInitNavigationModeZoom = 18.f;
    private float mInitNavigationModeTilt = 60.f;

    private boolean mVoiceOutputOn = false;
    private boolean mMapVisibility = false;

    private TextToSpeech mTextToSpeech;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enableGPS();

        if (savedInstanceState != null) {
            //Toast.makeText(this, "restored map", Toast.LENGTH_LONG).show();
        }

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            mInitCreateRouteModeZoom = savedInstanceState.getFloat(ZOOM_LEVEL);
            mMarkerLatitudes = savedInstanceState.getDoubleArray(MARKER_LATITUDES);
            mMarkerLongitudes = savedInstanceState.getDoubleArray(MARKER_LONGITUDES);
        }


        setContentView(R.layout.activity_maps);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setCustomView(R.layout.custom_action_bar_layout);
        View view = getActionBar().getCustomView();
        mTextToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation == null) {
                mLastLocation = new Location(LocationManager.GPS_PROVIDER);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Location security exception");
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        Button buttonTopLeft = (Button) findViewById(R.id.buttonTopLeft);
        buttonTopLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonTopLeftClicked(view);
            }
        });
        buttonTopLeft.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onButtonTopLeftLongClicked(view);
                return true;
            }
        });
        Button buttonStartStopNavigation = (Button) findViewById(R.id.start_stop_navigation);
        buttonStartStopNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleNavigationMode(view);
            }
        });
        Button buttonTopCenter = (Button) findViewById(R.id.buttonTopCenter);
        buttonTopCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start new activity
                Bundle bundle = new Bundle();
                saveInstanceStateToBundle(bundle);
                Intent firebaseIntent = new Intent(MapsActivity.this, FirebaseActivity.class);
                firebaseIntent.putExtra(SAVED_DATA, bundle);
                startActivityForResult(firebaseIntent, FIREBASE_REQUEST_CODE);

            }
        });

        //checkForPermission();

        final Handler mHandlerSpeakDistance = new Handler();
        final Handler mHandlerSpeakDirection = new Handler();
        mHandlerSpeakDistance.postDelayed(new Runnable() {
            private long time = 0;

            @Override
            public void run() {
                if (mVoiceOutputOn && mNavigationModeOn) {
                    mTextToSpeech.speak(String.format("%.0f", calcDistanceToMarker(mMarkerList.get(0))) + "meters", TextToSpeech.QUEUE_ADD, null);
                }
                    time += 30000;
                    Log.d("TimerExample", "Going for... " + time + ": ");
                    mHandlerSpeakDistance.postDelayed(this, 30000);

            }
        }, 10000);
        mHandlerSpeakDirection.postDelayed(new Runnable() {
            private long time = 0;

            @Override
            public void run() {

                if (mVoiceOutputOn && mNavigationModeOn) {
                    Location locationNextMarker = new Location(LocationManager.GPS_PROVIDER);
                    locationNextMarker.setLatitude(mMarkerList.get(0).getPosition().latitude);
                    locationNextMarker.setLongitude(mMarkerList.get(0).getPosition().longitude);
                    float angle = mLastLocation.bearingTo(locationNextMarker) - mLastLocation.getBearing();
                    if (angle < 0) {
                        angle += 360.f;
                    }
                    String msg = "";
                    if (angle > 90.f && angle < 135.f) {
                        msg = "Turn right";
                    } else if (angle > 225.f && angle < 270.f) {
                        msg = "Turn left";
                    } else if (angle <= 270.f && angle >= 135.f) {
                        msg = "Turn around";
                    }

                    mTextToSpeech.speak(msg, TextToSpeech.QUEUE_ADD, null);
                }
                time += 15000;
                Log.d("DirectionExample", "Going for... " + time + ": " + mVoiceOutputOn);
                mHandlerSpeakDirection.postDelayed(this, 15000);
            }
        }, 10000);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.game_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void checkDeleteMarker() {

        for (int i = 0; i < mMarkerList.size(); ++i) {
            LatLng latLng = new LatLng(mMarkerList.get(i).getPosition().latitude,
                    mMarkerList.get(i).getPosition().longitude);
            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);
            if (mWalkedLocationsList.get(mWalkedLocationsList.size() - 1).distanceTo(location) < DELETE_MARKER_DISTANCE) {
                Log.i(TAG, "Delete smaller marker" + i);
                deleteSmallerMarker(i);
            }
        }
    }

    private void deleteSmallerMarker(int i) {
        for (int j = i; j >= 0; --j) {
            mMarkerList.get(j).remove();
            mMarkerList.remove(j);
        }
        //Toast.makeText(this, "Remove Markers!", Toast.LENGTH_SHORT).show();
        drawUserDefinedRoute();
        setInfoNextMarker();
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
        mMap.getUiSettings().setMapToolbarEnabled(false); // We don't need a toolbar with navigation and stuff


        checkForPermission();

        if (mMarkerLatitudes != null) {  // We restart the activity and values have been written to the array
            for (int i = 0; i < mMarkerLatitudes.length; ++i) {
                LatLng latLng = new LatLng(mMarkerLatitudes[i], mMarkerLongitudes[i]);
                addPosition(latLng);
            }
        }

        mMap.moveCamera(CameraUpdateFactory.zoomTo(mInitCreateRouteModeZoom));


        //Add listener
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mNavigationModeOn == false) {
                    addPosition(latLng);
                }
            }
        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (mNavigationModeOn == false) {
                    // Don't add marker if it is on the same position as the last marker that was added
                    addPosition(marker.getPosition());
                }
                return true;
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                if (mNavigationModeOn == false) {
                    drawUserDefinedRoute();
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                if (mNavigationModeOn == false) {
                    drawUserDefinedRoute();
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (mNavigationModeOn == false) {
                    drawUserDefinedRoute();
                }
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                // If navigation mode is on, the camera should look into the same direction
                // as the user
                if (mNavigationModeOn) {
                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(latLng)
                                    .tilt(mInitNavigationModeTilt)
                                    .zoom(mInitNavigationModeZoom)
                                    .bearing(mLastLocation.getBearing())
                                    .build()));
                    return true;
                }
                // Do the standard procedure if route planning mode is on
                return false;
            }
        });

        // Acquire a reference to the system Location Manager
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (mNavigationModeOn && location != null) {
                    mWalkedLocationsList.add(location);
                    mLastLocation = location;

                    // Show toasts
                    Toast.makeText(MapsActivity.this, "New Loc! # of walked locations: " + mWalkedLocationsList.size(), Toast.LENGTH_SHORT).show();

                    // Draw walked route
                    drawWalkedRoute();

                    // Delete markers if they were already passed
                    checkDeleteMarker();

                    // Show distance left TextView and calculate distance to first marker
                    TextView distanceLeft = (TextView) findViewById(R.id.distance_left);
                    float distance = calcDistanceToMarker(mMarkerList.get(0));
                    distanceLeft.setText(String.format("%.0f", distance) + " m");

                    // Show value for Walked distance textview
                    TextView walkedDistance = (TextView) findViewById(R.id.distance);
                    distance = calcWalkedDistance();
                    walkedDistance.setText("Walked: " + String.format("%.3f", distance / 1000.) + " km");


                    // Adjust map perspective
                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(latLng)
                                    .tilt(mInitNavigationModeTilt)
                                    .zoom(mInitNavigationModeZoom)
                                    .bearing(mLastLocation.getBearing())
                                    .build()));
                }

                // Adjust arrow
                if (mMarkerList.size() > 0) {
                    Location locationNextMarker = new Location(LocationManager.GPS_PROVIDER);
                    locationNextMarker.setLatitude(mMarkerList.get(0).getPosition().latitude);
                    locationNextMarker.setLongitude(mMarkerList.get(0).getPosition().longitude);
                    ImageView arrow = (ImageView) findViewById(R.id.arrowView);
                    float arrowAngle = mLastLocation.bearingTo(locationNextMarker) - mLastLocation.getBearing();
                    arrow.setRotation(arrowAngle);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            // Register the listener with the Location Manager to receive location updates
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_LOCATION_REFRESH_TIME_MS, MIN_LOCATION_REFRESH_DISTANCE_M, mLocationListener);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == FIREBASE_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getBundleExtra("SAVED_DATA");
                mMarkerLatitudes = bundle.getDoubleArray("LATITUDES");
                mMarkerLongitudes = bundle.getDoubleArray("LONGITUDES");
                if (mMarkerLatitudes != null) {  // We restart the activity and values have been written to the array
                    deleteSmallerMarker(mMarkerList.size() - 1);
                    mMarkerNo = 0;
                    for (int i = 0; i < mMarkerLatitudes.length; ++i) {
                        LatLng latLng = new LatLng(mMarkerLatitudes[i], mMarkerLongitudes[i]);
                        addPosition(latLng);
                    }
                    LatLng latLng = new LatLng(mMarkerLatitudes[0], mMarkerLongitudes[0]);
                    double bearing = mLastLocation != null ? mLastLocation.getBearing() : 0.0;
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(latLng)
                                    .tilt(mInitNavigationModeTilt)
                                    .zoom(mInitNavigationModeZoom)
                                    .bearing((float) bearing)
                                    .build()));
                }
            }
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mMap == null) {
                return;
            }
            if (mLastLocation == null) {
                mLastLocation = new Location(LocationManager.GPS_PROVIDER);
            }
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }

    }


    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult cr) {
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.i(TAG, "Location was granted!");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.i(TAG, "Location was NOT granted!");
                    // Toast.makeText(this, R.string.locationAccessError,
                    //       Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        saveInstanceStateToBundle(savedInstanceState);
        //Log.i(TAG, "Saved activity state!");
    }


    private void saveInstanceStateToBundle(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        float zoom = mMap.getCameraPosition().zoom;
        savedInstanceState.putFloat(ZOOM_LEVEL, zoom);
        double markerLatitudes[] = new double[mMarkerList.size()];
        double markerLongitudes[] = new double[mMarkerList.size()];
        float markerDistances[] = new float[mMarkerList.size()];
        for (int i = 0; i < mMarkerList.size(); ++i) {
            markerLatitudes[i] = mMarkerList.get(i).getPosition().latitude;
            markerLongitudes[i] = mMarkerList.get(i).getPosition().longitude;
            if (i < mMarkerList.size() - 1) {
                markerDistances[i] = calcDistanceBetweenMarkers(mMarkerList.get(i), mMarkerList.get(i + 1));
            } else {
                markerDistances[i] = calcTotalDistance();
            }
        }
        savedInstanceState.putDoubleArray(MARKER_LONGITUDES, markerLongitudes);
        savedInstanceState.putDoubleArray(MARKER_LATITUDES, markerLatitudes);
        savedInstanceState.putFloatArray(MARKER_DISTANCES, markerDistances);
    }


    public void stopLocationUpdates(View view) {
        try {
            Toast.makeText(this, "Stop location updates!", Toast.LENGTH_SHORT).show();
            mLocationManager.removeUpdates(mLocationListener);
        } catch (SecurityException e) {
            Log.e(TAG, "LocationManager security exception");
        }


    }


    public void onButtonTopLeftLongClicked(View view) {
        if (!mNavigationModeOn) {

            while (mMarkerList.size() > 0) {
                mMarkerList.get(mMarkerList.size() - 1).remove();
                mMarkerList.remove(mMarkerList.size() - 1);
            }
            mMarkerNo = 0;
            drawUserDefinedRoute();
        }

    }

    /**
     * This function is called when the button 'Undo' is clicked
     *
     * @param view
     */
    public void onButtonTopLeftClicked(View view) {
        // In EDIT ROUTE mode, delete last marker if possible
        // First reset the marker, then remove it from the list
        if (!mNavigationModeOn) {
            if (mMarkerList.size() > 0) {
                mMarkerList.get(mMarkerList.size() - 1).remove();
                mMarkerList.remove(mMarkerList.size() - 1);
                mMarkerNo--;
                drawUserDefinedRoute();
                if (mMarkerList.size() > 0) {
                    mMarkerList.get(mMarkerList.size() - 1).showInfoWindow();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(mMarkerList.get(mMarkerList.size() - 1).getPosition())
                                    //.tilt(mInitNavigationModeTilt)
                                    .zoom(mInitCreateRouteModeZoom)
                                    //.bearing(mLookingDirectionUser)
                                    .build()));
                }
            } else {
                Toast.makeText(this, "No marker to delete", Toast.LENGTH_SHORT).show();
            }
        } else { // In navigation mode, turn voice output on
            toggleVoiceOutput();
        }

    }

    private void toggleVoiceOutput() {
        Button buttonTopLeft = (Button) findViewById(R.id.buttonTopLeft);
        if (mVoiceOutputOn) {
            buttonTopLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_volume_off_black_24dp, 0, 0, 0);
        } else {
            buttonTopLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_volume_up_black_24dp, 0, 0, 0);
        }
        mVoiceOutputOn = !mVoiceOutputOn;
    }

    public void toggleMapVisibility(View view) {
        if (mNavigationModeOn) {
            ImageButton buttonMapVisibility = (ImageButton) findViewById(R.id.mapVisibility);
            buttonMapVisibility.setVisibility(View.VISIBLE);
            mMapVisibility = !mMapVisibility;
            if (mMapVisibility) {
                buttonMapVisibility.setBackground(getResources().getDrawable(R.drawable.ic_visibility_off_black_24dp));
                mapFragment.getView().setVisibility(View.VISIBLE);
            } else {
                buttonMapVisibility.setBackground(getResources().getDrawable(R.drawable.ic_visibility_black_24dp));
                mapFragment.getView().setVisibility(View.INVISIBLE);
            }
        }
    }

    public void toggleNavigationMode(View view) {
        // If we are in createRoute-mode and haven't set any markers yet, it's not possible to toggle
        if (mMarkerList.size() <= 0 && !mNavigationModeOn) {
            Toast.makeText(this, "Set markers before starting the navigation!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mNavigationModeOn) {
            startNavigation();
        } else {
            stopNavigation();
        }
    }


    private void startNavigation() {

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            // Set audio On/off button
            Button buttonTopLeft = (Button) findViewById(R.id.buttonTopLeft);
            buttonTopLeft.setText(R.string.audioOn);
            if (mVoiceOutputOn) {
                buttonTopLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_volume_up_black_24dp, 0, 0, 0);
            } else {
                buttonTopLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_volume_off_black_24dp, 0, 0, 0);
            }

            // Set text view Walked
            TextView textView = (TextView) findViewById(R.id.distance);
            float distance = calcWalkedDistance();
            textView.setText("Walked: " + String.format("%.3f", distance / 1000.) + " km");

            // Set EditRoute Button)
            Button buttonStartStopNav = (Button) findViewById(R.id.start_stop_navigation);
            buttonStartStopNav.setText(R.string.navigationModeToggleStopNav);
            buttonStartStopNav.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_location_black_24dp, 0);

            // Show arrow
            ImageView arrow = (ImageView) findViewById(R.id.arrowView);
            arrow.setVisibility(View.VISIBLE);

            // Adjust arrow
            if (mMarkerList.size() > 0) {
                Location locationNextMarker = new Location(LocationManager.GPS_PROVIDER);
                locationNextMarker.setLatitude(mMarkerList.get(0).getPosition().latitude);
                locationNextMarker.setLongitude(mMarkerList.get(0).getPosition().longitude);
                float arrowAngle = 0.0f;
                if (mLastLocation != null) {
                    arrowAngle = mLastLocation.bearingTo(locationNextMarker) - mLastLocation.getBearing();
                }
                arrow.setRotation(arrowAngle);
            }

            // Show distance left TextView
            TextView distanceLeft = (TextView) findViewById(R.id.distance_left);
            distanceLeft.setVisibility(View.VISIBLE);
            // Calculate distance to first  marker
            ///
            distance = calcDistanceToMarker(mMarkerList.get(0));
            distanceLeft.setText(String.format("%.0f", distance) + " m");


            // Set button mapVisibility to visible
            ImageButton buttonMapVisibility = (ImageButton) findViewById(R.id.mapVisibility);
            buttonMapVisibility.setVisibility(View.VISIBLE);


            // Set markers as non-draggable
            for (Marker marker : mMarkerList) {
                marker.setDraggable(false);
            }
            setInfoNextMarker();

            // Adjust map perspective
            if (mLastLocation == null) {
                Log.e(TAG, "1mLastLocation is null");
                mLastLocation = new Location(LocationManager.GPS_PROVIDER);
            }
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(latLng)
                            .tilt(mInitNavigationModeTilt)
                            .zoom(mInitNavigationModeZoom)
                            .bearing(mLastLocation.getBearing())
                            .build()));
            mNavigationModeOn = true;
        } else {
            enableGPS();
        }
    }

    private void setInfoNextMarker() {
        if (mMarkerList.size() > 0) {
            mMarkerList.get(0).showInfoWindow();
        }
    }

    private void stopNavigation() {
        // Set DONE button
        Button buttonStartStopNav = (Button) findViewById(R.id.start_stop_navigation);
        buttonStartStopNav.setText(R.string.navigationModeToggleStartNav);
        buttonStartStopNav.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_near_me_black_24dp, 0);

        // Set distance in top line
        TextView textView = (TextView) findViewById(R.id.distance);
        String distance = String.format("%.3f", calcTotalDistance() / 1000.f); // Distance in km
        textView.setText("Total: " + distance + " km");

        // Set button delete last marker
        Button undoMarker = (Button) findViewById(R.id.buttonTopLeft);
        undoMarker.setText(R.string.deleteLastMarker);
        undoMarker.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_undo_black_24dp, 0, 0, 0);

        // Hide navigation arrow
        ImageView arrow = (ImageView) findViewById(R.id.arrowView);
        arrow.setVisibility(View.INVISIBLE);

        // Hide distance left TextView
        TextView distanceLeft = (TextView) findViewById(R.id.distance_left);
        distanceLeft.setVisibility(View.INVISIBLE);

        // Set button mapVisibility to invisible
        ImageButton buttonMapVisibility = (ImageButton) findViewById(R.id.mapVisibility);
        buttonMapVisibility.setVisibility(View.INVISIBLE);

        // Set map to visible
        mapFragment.getView().setVisibility(View.VISIBLE);

        // Set markers as draggable
        for (Marker marker : mMarkerList) {
            marker.setDraggable(true);
        }
        // If possible, get position of last marker in the marker list, otherwise move cam to last
        // known location
        LatLng latLng;
        if (mMarkerList.size() > 0) {
            latLng = new LatLng(mMarkerList.get(mMarkerList.size() - 1).getPosition().latitude,
                    mMarkerList.get(mMarkerList.size() - 1).getPosition().longitude);
        } else {
            if (mLastLocation == null) {
                Log.e(TAG, "3mLastLocation is null");
                mLastLocation = new Location(LocationManager.GPS_PROVIDER);
            }
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(latLng)
                        .tilt(0)
                        .zoom(mInitCreateRouteModeZoom)
                        .bearing(0.f)
                        .build()));
        mNavigationModeOn = false;
    }

    /**
     * This method adds a marker and a position to the mDefinedRoutePolyline
     *
     * @param latLng new position
     */
    private void addPosition(LatLng latLng) {
        try {
            Marker marker = mMap.addMarker(new MarkerOptions().
                    position(latLng).
                    draggable(true).
                    title("Marker No.: " + (++mMarkerNo)).
                    zIndex(mZIndex = mZIndex - 1.f)  // WTF it should be + 1.f but that doesn't work
            );

            marker.showInfoWindow();
            mMarkerList.add(marker);
            setCameraToMarker(marker);
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.toString());
        }
        drawUserDefinedRoute();

    }

    private float calcTotalDistance() {
        float distance = 0.f;
        for (int i = 0; i < mMarkerList.size() - 1; ++i) {
            distance += calcDistanceBetweenMarkers(mMarkerList.get(i), mMarkerList.get(i + 1));
        }
        return distance;
    }

    private float calcWalkedDistance() {
        float distance = 0.f;
        for (int i = 0; i < mWalkedLocationsList.size() - 1; ++i) {
            distance += calcDistanceBetweenLocations(mWalkedLocationsList.get(i), mWalkedLocationsList.get(i + 1));
        }
        return distance;
    }

    private float calcDistanceBetweenLocations(Location location1, Location location2) {

        float[] results = new float[3];
        double latStart = location1.getLatitude();
        double latEnd = location2.getLatitude();
        double lonStart = location1.getLongitude();
        double lonEnd = location2.getLongitude();
        Location.distanceBetween(
                latStart,
                lonStart,
                latEnd,
                lonEnd,
                results);
        return results[0];

    }

    private float calcDistanceBetweenMarkers(Marker marker1, Marker marker2) {

        float[] results = new float[3];
        double latStart = marker1.getPosition().latitude;
        double latEnd = marker2.getPosition().latitude;
        double lonStart = marker1.getPosition().longitude;
        double lonEnd = marker2.getPosition().longitude;
        Location.distanceBetween(
                latStart,
                lonStart,
                latEnd,
                lonEnd,
                results);
        return results[0];

    }

    private float calcDistanceToMarker(Marker marker) {
        float[] results = new float[3];
        if (mLastLocation == null) {
            Log.e(TAG, "4mLastLocation is null");
            mLastLocation = new Location(LocationManager.GPS_PROVIDER); //return 0.f;
        }
        double latStart = mLastLocation.getLatitude();
        double latEnd = marker.getPosition().latitude;
        double lonStart = mLastLocation.getLongitude();
        double lonEnd = marker.getPosition().longitude;
        Location.distanceBetween(
                latStart,
                lonStart,
                latEnd,
                lonEnd,
                results);
        return results[0];
    }

    private void setCameraToMarker(Marker marker) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(
                marker.getPosition()),
                CAMERA_ANIMATION_TIME_MS,
                new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
            }

            @Override
            public void onCancel() {
            }
        });
    }

    private void drawUserDefinedRoute() {
        PolylineOptions routeOptions = null;
        routeOptions = new PolylineOptions().width(6).color(POLYLINE_COLOR_DEFINED_ROUTE).geodesic(true);
        LatLng latLng = null;
        for (int i = 0; i < mMarkerList.size(); i++) {
            latLng = mMarkerList.get(i).getPosition();
            routeOptions.add(latLng);
        }

        if (mDefinedRoutePolyline != null) {
            Log.i(TAG, "Remove mDefinedRoutePolyline!");
            mDefinedRoutePolyline.remove();
        }
        mDefinedRoutePolyline = mMap.addPolyline(routeOptions);

        TextView textView = (TextView) findViewById(R.id.distance);
        String distance = String.format("%.3f", calcTotalDistance() / 1000.f); // Distance in km
        textView.setText("Distance: " + distance + " km");
    }

    private void drawWalkedRoute() {
        PolylineOptions routeOptions = null;
        routeOptions = new PolylineOptions().width(6).color(POLYLINE_COLOR_WALKED_ROUTE).geodesic(true);
        LatLng latLng = null;
        for (int i = 0; i < mWalkedLocationsList.size(); i++) {
            if (mWalkedLocationsList.get(i) == null)
                Log.e(TAG, "mWalkedLocationsList is 0!!");
            latLng = new LatLng(mWalkedLocationsList.get(i).getLatitude(), mWalkedLocationsList.get(i).getLongitude());
            routeOptions.add(latLng);
        }

        if (mWalkedRoutePolyline != null) {
            Log.i(TAG, "Remove mWalkedRoutePolyline!");
            mWalkedRoutePolyline.remove();
        }
        mWalkedRoutePolyline = mMap.addPolyline(routeOptions);
    }

    private void checkForPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            Log.i(TAG, "Got permission for location!");
        } else {
            // Show rationale and request permission.
            Log.i(TAG, "Did not get permission for location! Try to get permission now!");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void startInfoActivity(View view) {
        if (mMarkerList.size() > 0) {
            Bundle bundle = new Bundle();
            saveInstanceStateToBundle(bundle);
            Intent intent = new Intent(this, ExpandableListMain.class);
            intent.putExtra(SAVED_DATA, bundle);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Set markers before having access to more information.", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableGPS() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Your GPS is disabled, enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }
}
