package com.orbitusrobotics.intlspacestation;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ISSMapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    public Double latitude;
    public Double longitude;

    public Double old_latitude;
    public Double old_longitude;

    private Timer myTimer;
    private boolean firstDataPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issmaps);
        setUpMapIfNeeded();

        firstDataPoint = true;
        latitude = 0.0;
        longitude = 0.0;

        TimerMethod();
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }

        }, 0, 5000);


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
    private void TimerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.
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

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(Timer_Tick);
    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {

            //This method runs in the same thread as the UI.
            if (!firstDataPoint)
            {
                /*
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title("ISS")
                        .snippet(String.format("lat: %d\nlong: %d", latitude, longitude))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.spacestation_small)));
                */

                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(latitude, longitude), new LatLng(old_latitude, old_longitude))
                        .width(20)
                        .color(Color.BLUE));

                //mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitD)
                //MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow));
                //mMap.addMarker(markerOptions);

                SimpleDateFormat s = new SimpleDateFormat("hh:mm:ss dd/MM/yy");
                String format = s.format(new Date());

                mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("ISS-" + format));
            }

            //Do something to the UI thread here
            Log.d("maps", "UIThread - TimeTick");


        }
    };

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


        Log.d("maps", "getting iss_position" + rootobj);
        String latitude_s = lat_elem.getAsString();
        String longitude_s = long_elem.getAsString();

        old_latitude = latitude;
        old_longitude = longitude;

        latitude = lat_elem.getAsDouble();
        longitude = long_elem.getAsDouble();

        DecimalFormat df = new DecimalFormat("#.000000000000000");
        latitude_s =  df.format(latitude);
        longitude_s = df.format(longitude);

        ((TextView) findViewById(R.id.coordinates)).setText(String.format("lat: %s\nlong: %s", latitude_s, longitude_s));

        Log.d("maps", "latitude " + latitude_s);
        Log.d("maps", "longitude " + longitude_s);
        // *** add on the main UI thread
        // mMap.addMarker(new MarkerOptions().position(new LatLng(lat_elem.getAsDouble(), long_elem.getAsDouble())).title("Marker"));

        if (firstDataPoint)
        {
            TimerMethod();
        }
        firstDataPoint = false;

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


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


    private void setUpMap() {

    }
}
