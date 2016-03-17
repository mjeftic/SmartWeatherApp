package swa.smartweatherapp.View;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import swa.smartweatherapp.R;

public class MainActivity extends Activity implements LocationListener{

    //Init layouts
    private TextView tempView;
    private TextView dateView;
    private ImageButton cityBtn;
    private Spinner spinner;
    private TextView locationView;

    //Stores Geodata: longitude, latitude
    HashMap<String, Double> map = new HashMap<>();


    //Init variables
    private int temperature = 0;//default
    private static int tmp; //temporary
    private String finalAddress; //location
    private boolean firstSelect=false; //deactivate onItemSelectedListener from beginning

    protected LocationManager locationManager;
    protected LocationListener locationListener; //not needed for now
    protected Location location;

    //Init WeatherClient
    // http://survivingwithandroid.github.io/WeatherLib/ check for more information
    WeatherClient weatherclient;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init layouts
        dateView = (TextView) findViewById(R.id.dateView);
        tempView = (TextView) findViewById(R.id.tempView);
        cityBtn = (ImageButton) findViewById(R.id.changeCityBtn);
        locationView = (TextView) findViewById(R.id.locationView);
        spinner=(Spinner)findViewById(R.id.spinner);
        spinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item, Cities.getCityNames()));


        //Configure Weatherlib
        initWeatherClient();

        //Get weather of predefined enum citys
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(firstSelect==true){
                String selectedItem = parent.getItemAtPosition(position).toString();
                map.put("Longitude", getCity(selectedItem).getLongitude());
                map.put("Latitude", getCity(selectedItem).getLatitude());
                new WeatherTask().execute();
                //getWeather();
                locationView.setText(getCity(selectedItem).getName());
                tempView.setText(temperature + "°C ");
                tempView.setVisibility(View.VISIBLE);
                locationView.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), getCity(selectedItem).getName() + " wurde ausgewählt. Die Koordinaten sind Long: " + getCity(selectedItem).getLongitude() + " Lat: " + getCity(selectedItem).getLatitude(), Toast.LENGTH_LONG).show();}
                firstSelect=true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
                                          });

        //Change current city through a button. Opens a dialog.
        cityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocalData();
                    }
                });

        //Changes the date in the layout
        getDate();


    }


    //Provides the Date
    private void getDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd.MMM.yyyy");
        String formattedDate = df.format(c.getTime());
        dateView.setText(formattedDate);

    }


    //Uses the locationmanager to get local GPS Data and transforms them into an address with the geocoder.
    public void getLocalData(){
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 60 * 1, 10, this);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            map.put("Longitude", location.getLongitude());
            map.put("Latitude", location.getLatitude());
            new WeatherTask().execute();
            Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
            StringBuilder builder = new StringBuilder();
            try {
                List<Address> address = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                int maxLines = address.get(0).getMaxAddressLineIndex();
                for (int i=0; i<maxLines; i++) {
                    String addressStr = address.get(0).getAddressLine(i);
                    builder.append(addressStr);
                    builder.append(" ");
                }

                finalAddress = builder.toString(); //This is the complete address.

            } catch (IOException e) {}
            catch (NullPointerException e) {
            }
            locationView.setText(finalAddress);
            tempView.setText(temperature + "°C ");
            tempView.setVisibility(View.VISIBLE);
            locationView.setVisibility(View.VISIBLE);
        }
        cityBtn.setEnabled(true);
    }


    //Configure the Weather Library
    private void initWeatherClient() {
        WeatherClient.ClientBuilder builder = new WeatherClient.ClientBuilder();
        WeatherConfig config = new WeatherConfig();
        config.unitSystem = WeatherConfig.UNIT_SYSTEM.M;
        config.lang = "en";  //Language
        config.maxResult = 5; // How much cities
        config.numDays = 6; // Maximum forcast of days
        config.ApiKey = "2b8a5418e3e4cb1436cabf4f7e27feaf";

        try {
            weatherclient = builder.attach(this)
                    .provider(new OpenweathermapProviderType())
                    .httpClient(com.survivingwithandroid.weather.lib.client.volley.WeatherClientDefault.class)
                    .config(config)
                    .build();
        }
        catch(Throwable t) {

        }


    }


    //Provides weather data of a city. Needs longitude and latitude to excecute task.
    //Example: Mannheim: longitude = 49.4883333, latitude 8.4647222
    private class WeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... str) {
            String response = "";
            weatherclient.getCurrentCondition(new WeatherRequest(map.get("Longitude"), map.get("Latitude")),
                    new WeatherClient.WeatherEventListener() {
                        @Override
                        public void onWeatherRetrieved(CurrentWeather currentWeather) {
                            // Current Weather
                            tmp = (int) currentWeather.weather.temperature.getTemp();
                            Log.d("MainActivity", tempView.getText().toString() + " is the temperature");
                            cityBtn.setEnabled(true);
                            tempView.setVisibility(View.VISIBLE);
                            locationView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onWeatherError(WeatherLibException e) {
                            Log.d("MainActivity", "Error WeatherLibException");
                            cityBtn.setEnabled(true);
                        }


                        @Override
                        public void onConnectionError(Throwable throwable) {
                            Log.d("MainActivity", "Error Connection");
                            cityBtn.setEnabled(true);
                        }
                    });
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            cityBtn.setEnabled(true);
            temperature = tmp;
            //temperature = (int) currentWeather.weather.temperature.getTemp();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            cityBtn.setEnabled(false);

        }
    }


        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    //Provides Cities of Enum List with Geo Data.
    private Cities getCity(String name){
        switch (name){
            case "Berlin": return Cities.BERLIN;
            case "Ulm": return Cities.ULM;
            case "Barcelona": return Cities.BARCELONA;
            case "Biberach": return Cities.BIBERACH;
            default: return Cities.BERLIN;

        }

    }


}

