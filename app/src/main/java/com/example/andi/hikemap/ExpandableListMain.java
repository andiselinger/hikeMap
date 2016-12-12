package com.example.andi.hikemap;

import java.util.ArrayList;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.app.ExpandableListActivity;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import static com.example.andi.hikemap.GlobalDefinitions.MARKER_DISTANCES;
import static com.example.andi.hikemap.GlobalDefinitions.MARKER_LATITUDES;
import static com.example.andi.hikemap.GlobalDefinitions.MARKER_LONGITUDES;
import static com.example.andi.hikemap.GlobalDefinitions.SAVED_DATA;

public class ExpandableListMain extends ExpandableListActivity {
    //Initialize variables
    private static final String STR_CHECKED = " has Checked!";
    private static final String STR_UNCHECKED = " has unChecked!";
    private int ParentClickStatus = -1;
    private int ChildClickStatus = -1;
    private ArrayList<Parent> parents;

    private double[] mMarkerLongitudes; // The actual data
    private double[] mMarkerLatitudes;
    private float[] mMarkerDistances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Resources res = this.getResources();
        //Drawable devider = res.getDrawable(R.drawable.line);

        // Set ExpandableListView values

        getExpandableListView().setGroupIndicator(null);
        registerForContextMenu(getExpandableListView());

        extractMarkerData();

        //Creating static data in arraylist
        final ArrayList<Parent> dummyList = buildDummyData();

        // Adding ArrayList data to ExpandableListView values
        loadHosts(dummyList);

    }

    private void extractMarkerData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(SAVED_DATA);
        mMarkerLatitudes = bundle.getDoubleArray(MARKER_LATITUDES);
        mMarkerLongitudes = bundle.getDoubleArray(MARKER_LONGITUDES);
        mMarkerDistances = bundle.getFloatArray(MARKER_DISTANCES);
    }

    /**
     * here should come your data service implementation
     *
     * @return
     */
    private ArrayList<Parent> buildDummyData() {
        // Creating ArrayList of type parent class to store parent class objects
        final ArrayList<Parent> list = new ArrayList<Parent>();
        final Parent totalDistance = new Parent();
        totalDistance.setName("GeneralInformation");
        totalDistance.setText1("Total Distance: " +
                String.format("%.3f", mMarkerDistances[mMarkerDistances.length - 1] / 1000.) +
                " km");
        list.add(totalDistance);
        for (int i = 0; i < mMarkerLatitudes.length; i++) {
            //Create parent class object
            final Parent parent = new Parent();

            // Set values in parent class object

            parent.setName("" + i);
            parent.setText1("Marker " + i);
            //parent.setText2("Disable App On \nBattery Low");
            parent.setChildren(new ArrayList<Child>());

            // Create Child class object
            final Child child1 = new Child();
            child1.setName("" + i);
            child1.setText1("Longitude: " + String.format("%.5f", mMarkerLongitudes[i]));
            parent.getChildren().add(child1);


            final Child child2 = new Child();
            child2.setName("" + i);
            child2.setText1("Latitude: " + String.format("%.5f", mMarkerLatitudes[i]));
            parent.getChildren().add(child2);


            if (i < mMarkerLatitudes.length - 1) {
                final Child child3 = new Child();
                child3.setName("" + i);

                child3.setText1("Distance to next marker: " + String.format("%.3f", mMarkerDistances[i] / 1000.f) + " km");
                parent.getChildren().add(child3);
            }


            list.add(parent);
        }


        if (mMarkerLatitudes.length == 0) {
            final Parent parent = new Parent();

            // Set values in parent class object

            parent.setName("No Markers");
            parent.setText1("No markers were added!");
            list.add(parent);
        }
        return list;
    }


    private void loadHosts(final ArrayList<Parent> newParents) {
        if (newParents == null)
            return;

        parents = newParents;

        // Check for ExpandableListAdapter object
        if (this.getExpandableListAdapter() == null) {
            //Create ExpandableListAdapter Object
            final MyExpandableListAdapter mAdapter = new MyExpandableListAdapter();

            // Set Adapter to ExpandableList Adapter
            this.setListAdapter(mAdapter);
        } else {
            // Refresh ExpandableListView data
            ((MyExpandableListAdapter) getExpandableListAdapter()).notifyDataSetChanged();
        }
    }

    /**
     * A Custom adapter to create com.example.andi.hikemap.Parent view (Used grouprow.xml) and com.example.andi.hikemap.Child View((Used childrow.xml).
     */
    private class MyExpandableListAdapter extends BaseExpandableListAdapter {


        private LayoutInflater inflater;

        public MyExpandableListAdapter() {
            // Create Layout Inflater
            inflater = LayoutInflater.from(ExpandableListMain.this);
        }


        // This Function used to inflate parent rows view

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parentView) {
            final Parent parent = parents.get(groupPosition);

            // Inflate grouprow.xml file for parent rows
            convertView = inflater.inflate(R.layout.grouprow, parentView, false);

            // Get grouprow.xml file elements and set values
            ((TextView) convertView.findViewById(R.id.text1)).setText(parent.getText1());
            ((TextView) convertView.findViewById(R.id.text)).setText(parent.getText2());
            ImageView image = (ImageView) convertView.findViewById(R.id.image);

            image.setImageResource(
                    getResources().getIdentifier(
                            "com.androidexample.customexpandablelist:drawable/setting" + parent.getName(), null, null));

            ImageView rightcheck = (ImageView) convertView.findViewById(R.id.rightcheck);


            // Change right check image on parent at runtime
            if (parent.isChecked() == true) {
                rightcheck.setImageResource(
                        getResources().getIdentifier(
                                "com.androidexample.customexpandablelist:drawable/rightcheck", null, null));
            } else {
                rightcheck.setImageResource(
                        getResources().getIdentifier(
                                "com.androidexample.customexpandablelist:drawable/button_check", null, null));
            }

            // Get grouprow.xml file checkbox elements
            ImageView checkbox = (ImageView) findViewById(R.id.checkbox);
            //checkbox.setVisibility(View.VISIBLE);


            // Set CheckUpdateListener for CheckBox (see below CheckUpdateListener class)
            //checkbox.setOnCheckedChangeListener(new CheckUpdateListener(parent));

            return convertView;
        }


        // This Function used to inflate child rows view
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parentView) {
            final Parent parent = parents.get(groupPosition);
            final Child child = parent.getChildren().get(childPosition);

            // Inflate childrow.xml file for child rows
            convertView = inflater.inflate(R.layout.childrow, parentView, false);

            // Get childrow.xml file elements and set values
            ((TextView) convertView.findViewById(R.id.text1)).setText(child.getText1());
            //((TextView) convertView.findViewById(R.id.text2)).setText(child.getText2());
            ImageView image = (ImageView) convertView.findViewById(R.id.image);
            image.setImageResource(
                    getResources().getIdentifier(
                            "com.androidexample.customexpandablelist:drawable/setting" + parent.getName(), null, null));

            return convertView;
        }


        @Override
        public Object getChild(int groupPosition, int childPosition) {
            //Log.i("Childs", groupPosition+"=  getChild =="+childPosition);
            return parents.get(groupPosition).getChildren().get(childPosition);
        }

        //Call when child row clicked
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            /****** When com.example.andi.hikemap.Child row clicked then this function call *******/

            //Log.i("Noise", "parent == "+groupPosition+"=  child : =="+childPosition);
            if (ChildClickStatus != childPosition) {
                ChildClickStatus = childPosition;

                //Toast.makeText(getApplicationContext(), "com.example.andi.hikemap.Parent :" + groupPosition + " com.example.andi.hikemap.Child :" + childPosition,
                //        Toast.LENGTH_LONG).show();
            }

            return childPosition;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            int size = 0;
            if (parents.get(groupPosition).getChildren() != null)
                size = parents.get(groupPosition).getChildren().size();
            return size;
        }


        @Override
        public Object getGroup(int groupPosition) {
            return parents.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return parents.size();
        }

        //Call when parent row clicked
        @Override
        public long getGroupId(int groupPosition) {

            if (groupPosition == 2 && ParentClickStatus != groupPosition) {

                //Alert to user
                //Toast.makeText(getApplicationContext(), "com.example.andi.hikemap.Parent :" + groupPosition,
                //        Toast.LENGTH_LONG).show();
            }

            ParentClickStatus = groupPosition;
            if (ParentClickStatus == 0)
                ParentClickStatus = -1;

            return groupPosition;
        }

        @Override
        public void notifyDataSetChanged() {
            // Refresh List rows
            super.notifyDataSetChanged();
        }

        @Override
        public boolean isEmpty() {
            return ((parents == null) || parents.isEmpty());
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }


    }
}