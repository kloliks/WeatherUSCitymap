package com.klolik.weatheruscitymap;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView         mCityLat;
        TextView         mCityLon;
        TextView         mCityName;
        RelativeLayout   mCityLayout;

        ViewHolder(View view) {
            super(view);
            mCityLat  = (TextView) view.findViewById(R.id.cityLat);
            mCityLon  = (TextView) view.findViewById(R.id.cityLon);
            mCityName = (TextView) view.findViewById(R.id.cityName);
            mCityLayout = (RelativeLayout) view.findViewById(R.id.cityLayout);
        }
    }

    private View.OnClickListener mOnClickListener;
    private List<CityRow> mDataSet;
    RecyclerViewAdapter(List<CityRow> dataSet, View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
        mDataSet = dataSet;
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_layout, parent, false);
        view.setOnClickListener(mOnClickListener);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CityRow cityRow = mDataSet.get(position);
        holder.mCityLat.setText(cityRow.mLat);
        holder.mCityLon.setText(cityRow.mLon);
        holder.mCityName.setText(cityRow.mName);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
