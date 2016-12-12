package com.example.andi.hikemap;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import static com.example.andi.hikemap.GlobalDefinitions.MARKER_DISTANCES;
import static com.example.andi.hikemap.GlobalDefinitions.MARKER_LATITUDES;
import static com.example.andi.hikemap.GlobalDefinitions.MARKER_LONGITUDES;
import static com.example.andi.hikemap.GlobalDefinitions.SAVED_DATA;

public class Info2Activity extends Activity {

    private double[] mMarkerLongitudes; // The actual data
    private double[] mMarkerLatitudes;
    private float[] mMarkerDistances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info2);
        View view = findViewById(R.id.activity_info2);
        if (view == null) {
            Log.e("...", "asdfadsafasdfasfnull");
        } else {
            view.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
        extractMarkerData();
        createButtons();
    }


    private void extractMarkerData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(SAVED_DATA);
        mMarkerLatitudes = bundle.getDoubleArray(MARKER_LATITUDES);
        mMarkerLongitudes = bundle.getDoubleArray(MARKER_LONGITUDES);
        mMarkerDistances = bundle.getFloatArray(MARKER_DISTANCES);
    }

    private void createButtons() {
        for (int i = 0; i < mMarkerLatitudes.length; ++i) {
            // Add new button
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutInfoActivity);
            TextView textView = new TextView(this);
            textView.setText("Marker");
            textView.setTextColor(getResources().getColor(R.color.colorBlack));
            textView.setBackgroundColor(getResources().getColor(R.color.colorWhite));

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(300, 70);
            layoutParams.setMargins(5, 3, 0, 0); // left, top, right, bottom
            textView.setLayoutParams(layoutParams);
            linearLayout.addView(textView);
        }
    }


}
