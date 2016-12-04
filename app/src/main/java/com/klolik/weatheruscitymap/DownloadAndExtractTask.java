package com.klolik.weatheruscitymap;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class DownloadAndExtractTask extends AsyncTask<DETaskArgs, Void, Void> {
    private Context mContext;
    private View mView;

    public DownloadAndExtractTask(Context context, View view) {
        mContext = context;
        mView = view;
    }

    @Override
    protected Void doInBackground(DETaskArgs... url_and_paths) {
        try {
            URL url = new URL(url_and_paths[0].sUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e("Error", "code ("+ connection.getResponseCode()
                        +"); message ("+ connection.getResponseMessage() +")");
                return null;
            }

            InputStream stream = new GZIPInputStream(connection.getInputStream());
            InputSource source = new InputSource(stream);
            InputStream input = new BufferedInputStream(source.getByteStream());

            String output_file_name = url_and_paths[0].sPath;
            OutputStream output = new FileOutputStream(output_file_name);

            int count;
            byte data[] = new byte[4096];
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();

            output.close();
            input.close();
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        mView.setClickable(false);

        Animation rotation = AnimationUtils.loadAnimation(mContext, R.anim.rotation);
        rotation.setRepeatCount(Animation.INFINITE);

        mView.startAnimation(rotation);
    }

    @Override
    protected void onPostExecute(Void result) {
        ((MainActivity) mContext).swapDataSet();
        mView.clearAnimation();
        mView.setClickable(true);
    }
}
