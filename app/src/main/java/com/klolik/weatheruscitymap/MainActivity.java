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
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.google.maps.android.clustering.view.ClusterRenderer;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mMapView;
    private RecyclerView mRecyclerView;
    private List<CityRow> mDataSet;
    private DrawerLayout mDrawer;
    private GoogleMap mMap;
    private ClusterManager<MapMarker> mClusterManager;

    private String mJsonCitiesFile;
    private boolean mNeedRefresh;

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mClusterManager = null;

        initDrawerAndToolbar();
        initMapView(savedInstanceState);
        initRecycleView();
    }


    private void initDrawerAndToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
    }


    private void initMapView(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
    }


    private class myOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int position = mRecyclerView.getChildLayoutPosition(view);
            CityRow item = mDataSet.get(position);

            double lat = Double.parseDouble(item.mLat);
            double lon = Double.parseDouble(item.mLon);
            LatLng ll = new LatLng(lat, lon);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 20));
            mDrawer.closeDrawer(GravityCompat.START);
        }
    }

    private void initRecycleView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mJsonCitiesFile = Environment.getExternalStorageDirectory().getPath()
                +"/"+ getResources().getString(R.string.city_file_name);

        mDataSet = populateCitiesList();
        mNeedRefresh = mDataSet.size() == 0;

        RecyclerView.Adapter mAdapter = new RecyclerViewAdapter(mDataSet, new myOnClickListener());
        mRecyclerView.setAdapter(mAdapter);
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

        try {
            CitiesJsonReader reader = new CitiesJsonReader(json);
            CityRow row;
            while ((row = reader.readNext()) != null) {
                citiesList.add(row);
            }
            reader.close();
        } catch (IOException e) {
            Log.e("IOException", e.getMessage());
        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage());
        }

        if (mClusterManager != null) {
            mClusterManager.clearItems();
            fillMap(citiesList);
        }
        return citiesList;
    }

    private void fillMap(List<CityRow> list) {
        for (int i = 0; i < list.size(); ++i) {
            double lat = Double.parseDouble(list.get(i).mLat);
            double lon = Double.parseDouble(list.get(i).mLon);
            LatLng ll = new LatLng(lat, lon);

            MapMarker marker = new MapMarker(lat, lon, list.get(i).mName);
            mClusterManager.addItem(marker);
        }
        mClusterManager.cluster();
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
        mClusterManager = new ClusterManager<MapMarker>(this.getApplicationContext(), mMap);
        mClusterManager.setRenderer(new MapCustomClusterRenderer(this.getApplicationContext(), mMap, mClusterManager));
        GridBasedAlgorithm<MapMarker> gridAlgorithm = new GridBasedAlgorithm<MapMarker>();
        mClusterManager.setAlgorithm(new PreCachingAlgorithmDecorator<MapMarker>(gridAlgorithm));
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        if (!mDataSet.isEmpty()) {
            mClusterManager.clearItems();
            fillMap(mDataSet);
        }
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
