package com.orbitusrobotics.intlspacestation;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ISSMapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    public Double latitude;
    public Double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issmaps);
        setUpMapIfNeeded();

        Log.d("maps", "Start");
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    //Your code goes here
                    doThis();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

    public void doThis() throws IOException {

        Log.d("maps", "Do This Start");
        // Connect to the URL using java's native library
        URL url = null;
        try {
            url = new URL("http://api.open-notify.org/iss-now.json");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
        JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.

        JsonObject iss_position_object = rootobj.getAsJsonObject("iss_position");
        JsonElement lat_elem = iss_position_object.get("latitude");
        JsonElement long_elem = iss_position_object.get("longitude");


        /*
        try {
            JSONObject json= (JSONObject) new JSONTokener(result).nextValue();
            JSONObject json2 = json.getJSONObject("results");
            test = (String) json2.get("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }*/


        Log.d("maps", "getting iss_position" + rootobj);
        String latitude_s = lat_elem.getAsString();
        String longitude_s = long_elem.getAsString();

        Log.d("maps", "latitude " + latitude_s);
        Log.d("maps", "longitude " + longitude_s);


        //Log.d("maps", rootobj.get("iss_position").getAsString()); //just grab the zipcode

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }
}