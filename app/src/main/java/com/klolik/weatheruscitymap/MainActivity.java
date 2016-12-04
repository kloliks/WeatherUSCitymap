package com.klolik.weatheruscitymap;

import android.os.Environment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mMapView;
    private RecyclerView mRecyclerView;
    private List<CityRow> mDataSet;
    private DrawerLayout mDrawer;
    private GoogleMap mMap;

    private String mJsonCitiesFile;
    private boolean mNeedRefresh;

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();


        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);


        mRecyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mJsonCitiesFile = Environment.getExternalStorageDirectory().getPath()
                +"/"+ mContext.getResources().getString(R.string.city_file_name);

        File json = new File(mJsonCitiesFile);
        mNeedRefresh = ! json.exists();
        if (mNeedRefresh)
            mDataSet = new ArrayList<CityRow>();
        else
            mDataSet = populateCitiesList();
//            long lastTime = json.lastModified();
//            Toast.makeText(this, "last time: "+lastTime, Toast.LENGTH_SHORT).show();


        RecyclerView.Adapter mAdapter = new RecyclerViewAdapter(mDataSet, new myOnClickListener());
        mRecyclerView.setAdapter(mAdapter);
    }

    private class myOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int position = mRecyclerView.getChildLayoutPosition(view);
            CityRow item = mDataSet.get(position);

            double lat = Double.parseDouble(item.mLat);
            double lon = Double.parseDouble(item.mLon);
            LatLng ll = new LatLng(lat, lon);

            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(ll).title(item.mName));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(ll));
            
            mDrawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        if (mNeedRefresh) {
            View refresh_view = menu.findItem(R.id.action_refresh).getActionView();
            onRefresh(refresh_view);
        }
        return true;
    }

    private List<CityRow> populateCitiesList() {
        List<CityRow> citiesList = new ArrayList<CityRow>();

        File json = new File(mJsonCitiesFile);
        if (!json.exists())
            return citiesList;
        
        for( int i = 0; i < 50; ++i ){
            CityRow cityRow = new CityRow("City " + i, ""+ i*10 +".0", ""+ i*20 +".0");
            citiesList.add(cityRow);
        }

        return citiesList;
    }

    public void onRefresh(View view) {
        DownloadAndExtractTask asyncTask = new DownloadAndExtractTask(this, view);
        asyncTask.execute(new DETaskArgs(
                getResources().getString(R.string.url_gzip_city_archive),
                mJsonCitiesFile
                )
        );

    }

    public void swapDataSet() {
        mDataSet.clear();
        mDataSet.addAll(populateCitiesList());
        if (mNeedRefresh) {
            TextView tv = (TextView) findViewById(R.id.no_available_data_drawer_label);
            tv.setVisibility(View.INVISIBLE);
            mNeedRefresh = false;
        }
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
