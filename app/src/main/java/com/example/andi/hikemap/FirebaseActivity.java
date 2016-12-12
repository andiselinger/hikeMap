package com.example.andi.hikemap;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.andi.hikemap.GlobalDefinitions.MARKER_LATITUDES;
import static com.example.andi.hikemap.GlobalDefinitions.MARKER_LONGITUDES;
import static com.example.andi.hikemap.GlobalDefinitions.SAVED_DATA;

public class FirebaseActivity extends Activity {

    private FirebaseDatabase mDatabaseRoute;
    private DatabaseReference mDatabase;
    private DatabaseReference mRef;
    private double[] mMarkerLongitudesArrayOriginal; // The actual data
    private double[] mMarkerLatitudesArrayOriginal; // The actual data

    private List<Double> mMarkerLatitudesOriginal;
    private List<Double> mMarkerLongitudesOriginal;
    private final static String TAG = "FirebaseActivity";

    private String ANDROID_ID;

    private List<Route> mPublicRoutes = new ArrayList<>();
    private List<Route> mPrivateRoutes = new ArrayList<>();

    private long mNumRoutes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        ANDROID_ID = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mDatabaseRoute = FirebaseDatabase.getInstance();
        mRef = mDatabaseRoute.getReference("route");
        mDatabase = FirebaseDatabase.getInstance().getReference();


        extractMarkerData();


        ValueEventListener privateRouteListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                mNumRoutes = dataSnapshot.getChildrenCount();
                Iterable<DataSnapshot> routes = dataSnapshot.getChildren();
                int i = 0;
                for (DataSnapshot snapshot : routes) {
                    Log.i(TAG, "i = " + i++);
                    Route route = snapshot.getValue(Route.class);
                    if (route != null) {
                        mPrivateRoutes.add(route);
                        Log.w(TAG, "PRIVATE!! : name: " + route.routeName);
                    }
                }
                createPrivateButtons();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabase.child("privateRoutes" + ANDROID_ID).addListenerForSingleValueEvent(privateRouteListener);


        Button buttonPrivateRoute = (Button) findViewById(R.id.saveRoutePrivate);
        buttonPrivateRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMarkerLongitudesArrayOriginal != null) {
                    if (mMarkerLongitudesArrayOriginal.length > 0) {

                        mMarkerLatitudesOriginal = new ArrayList<>();
                        mMarkerLongitudesOriginal = new ArrayList<>();
                        if (mMarkerLongitudesArrayOriginal != null) {
                            for (int i = 0; i < mMarkerLatitudesArrayOriginal.length; ++i) {
                                mMarkerLatitudesOriginal.add(mMarkerLatitudesArrayOriginal[i]);
                                mMarkerLongitudesOriginal.add(mMarkerLongitudesArrayOriginal[i]);
                            }
                        }
                        EditText editText = (EditText) findViewById(R.id.editText);
                        String routeName = editText.getText().toString();
                        if (routeName.length() > 0) {
                            writePrivateRoute(routeName, routeName);
                            Toast.makeText(FirebaseActivity.this, "Route was successfully uploaded!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(FirebaseActivity.this, "Type a name first!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(FirebaseActivity.this, "Define a route first!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FirebaseActivity.this, "Define a route first!", Toast.LENGTH_SHORT).show();
                }
            }

        });


        ValueEventListener publicRouteListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                mNumRoutes = dataSnapshot.getChildrenCount();
                Iterable<DataSnapshot> routes = dataSnapshot.getChildren();
                int i = 0;
                for (DataSnapshot snapshot : routes) {
                    Log.i(TAG, "i = " + i++);
                    Route route = snapshot.getValue(Route.class);
                    if (route != null) {
                        mPublicRoutes.add(route);
                        Log.w(TAG, "name: " + route.routeName);
                    }
                }
                createPublicButtons();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabase.child("publicRoutes").addListenerForSingleValueEvent(publicRouteListener);


        Button buttonPublishRoute = (Button) findViewById(R.id.publishRoute);
        buttonPublishRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMarkerLongitudesArrayOriginal != null) {
                    if (mMarkerLongitudesArrayOriginal.length > 0) {

                        mMarkerLatitudesOriginal = new ArrayList<>();
                        mMarkerLongitudesOriginal = new ArrayList<>();
                        if (mMarkerLongitudesArrayOriginal != null) {
                            for (int i = 0; i < mMarkerLatitudesArrayOriginal.length; ++i) {
                                mMarkerLatitudesOriginal.add(mMarkerLatitudesArrayOriginal[i]);
                                mMarkerLongitudesOriginal.add(mMarkerLongitudesArrayOriginal[i]);
                            }
                        }
                        EditText editText = (EditText) findViewById(R.id.editText);
                        String routeName = editText.getText().toString();
                        if (routeName.length() > 0) {
                            writePublicRoute(routeName, routeName);
                            Toast.makeText(FirebaseActivity.this, "Route was successfully uploaded!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(FirebaseActivity.this, "Type a name first!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(FirebaseActivity.this, "Define a route first!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FirebaseActivity.this, "Define a route first!", Toast.LENGTH_SHORT).show();
                }
            }

        });


    }

    private void writePublicRoute(String routeId, String name) {
        Route route = new Route(name, mMarkerLongitudesOriginal, mMarkerLatitudesOriginal);
        Log.d(TAG, "created new route!");
        mDatabase.child("publicRoutes").child(routeId).setValue(route);
    }

    private void writePrivateRoute(String routeId, String name) {
        Route route = new Route(name, mMarkerLongitudesOriginal, mMarkerLatitudesOriginal);
        Log.d(TAG, "created new route!");
        mDatabase.child("privateRoutes" + ANDROID_ID).child(routeId).setValue(route);
    }

    private void extractMarkerData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(SAVED_DATA);
        mMarkerLatitudesArrayOriginal = bundle.getDoubleArray(MARKER_LATITUDES);
        mMarkerLongitudesArrayOriginal = bundle.getDoubleArray(MARKER_LONGITUDES);
    }


    @IgnoreExtraProperties
    public static class Route {

        public String routeName;
        public List<Double> markerLatitudes;
        public List<Double> markerLongitudes;

        public Route() {
            // Default constructor required for calls to DataSnapshot.getValue(Route.class)
        }

        public Route(String routeName, List<Double> longitudes, List<Double> latitudes) {
            this.routeName = routeName;
            this.markerLatitudes = latitudes;
            this.markerLongitudes = longitudes;
        }
    }

    public double[] getLatitudesArray(Route route) {
        return listToArray(route.markerLatitudes);
    }

    public double[] getLongitudesArray(Route route) {
        return listToArray(route.markerLongitudes);
    }

    private double[] listToArray(List<Double> doublesList) {
        double[] target = new double[doublesList.size()];
        for (int i = 0; i < target.length; i++) {
            target[i] = doublesList.get(i);                // java 1.5+ style (outboxing)
        }
        return target;
    }

    private void createPublicButtons() {
        for (final Route route : mPublicRoutes) {
            // Add new button
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutPublicRoutes);
            Button button = new Button(this);
            button.setText(route.routeName);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putDoubleArray("LATITUDES", getLatitudesArray(route));
                    bundle.putDoubleArray("LONGITUDES", getLongitudesArray(route));

                    intent.putExtra("SAVED_DATA", bundle);
                    setResult(-1, intent);
                    finish();//finishing activity

                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(5, 5, 5, 5); // left, top, right, bottom

            button.setLayoutParams(layoutParams);
            button.setPadding(10, 10, 10, 10);
            linearLayout.addView(button);
        }
    }

    private void createPrivateButtons() {
        for (final Route route : mPrivateRoutes) {
            // Add new button
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutPrivateRoutes);
            Button button = new Button(this);
            button.setText(route.routeName);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putDoubleArray("LATITUDES", getLatitudesArray(route));
                    bundle.putDoubleArray("LONGITUDES", getLongitudesArray(route));

                    intent.putExtra("SAVED_DATA", bundle);
                    setResult(-1, intent);
                    finish();//finishing activity

                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(5, 5, 5, 5); // left, top, right, bottom

            button.setLayoutParams(layoutParams);
            button.setPadding(10, 10, 10, 10);
            linearLayout.addView(button);
        }
    }
}
