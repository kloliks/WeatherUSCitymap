package com.klolik.weatheruscitymap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

class CitiesJsonReader {
    private BufferedReader reader;

    CitiesJsonReader(File json) throws IOException {
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(json)));
    }

    CityRow readNext() throws IOException, JSONException {
        String line;
        if ((line = reader.readLine()) == null)
            return null;

        JSONObject mainObj = new JSONObject(line);
        String city = mainObj.getString("name");

        JSONObject latlon = mainObj.getJSONObject("coord");
        String lat = latlon.getString("lat");
        String lon = latlon.getString("lon");

        return new CityRow(city, lat, lon);


    }

    void close() throws IOException {
        reader.close();
    }
}
