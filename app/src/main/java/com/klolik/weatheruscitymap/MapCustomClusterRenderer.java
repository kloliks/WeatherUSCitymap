package com.klolik.weatheruscitymap;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

class MapCustomClusterRenderer extends DefaultClusterRenderer<MapMarker> {
    MapCustomClusterRenderer(
            Context context, GoogleMap map, ClusterManager<MapMarker> clusterManager)
    {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(MapMarker item, MarkerOptions markerOptions) {
        markerOptions.title(item.toString());
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        return cluster.getSize() > 1;
    }
}
