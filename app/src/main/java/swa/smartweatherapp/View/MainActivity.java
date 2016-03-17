package swa.smartweatherapp.View;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.City;
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

    HashMap<String, Double> map = new HashMap<>();


    //Init variables
    private int temperature = 0;
    private String city = "default";
    private String finalAddress;
    private City cityObject;
    private GoogleApiClient.Builder mGoogleApiClient;

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Location location;
    //Init WeatherClient
    // http://survivingwithandroid.github.io/WeatherLib/ check for more information
    WeatherClient weatherclient = null;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //cityObject=(new City.CityBuilder()).name("Mannheim").build();

        //Toast.makeText(this,cityObject.getName(),Toast.LENGTH_LONG).show();

        //Init layouts
        dateView = (TextView) findViewById(R.id.dateView);
        tempView = (TextView) findViewById(R.id.tempView);
        cityBtn = (ImageButton) findViewById(R.id.changeCityBtn);
        locationView = (TextView) findViewById(R.id.locationView);
        spinner=(Spinner)findViewById(R.id.spinner);
        spinner.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_item, Cities.getCityNames()));


        //Configure Weatherlib
        initWeatherClient();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                map.put("Longitude", getCity(selectedItem).getLongitude());
                map.put("Latitude", getCity(selectedItem).getLatitude());
                getWeather();
                locationView.setText(getCity(selectedItem).getName());
                tempView.setText(temperature + "°C ");
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
                /*final Dialog dialog = new Dialog(MainActivity.this, R.style.AlertDialogCustom);
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.popup_changecity);
                dialog.show();

                Button acceptCity = (Button) dialog.findViewById(R.id.popupChangeCityBtn);
                acceptCity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });*/
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

    public void getLocalData(){
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 60 * 1, 10, this);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            map.put("Longitude", location.getLongitude());
            map.put("Latitude", location.getLatitude());
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
            getWeather();
            tempView.setText(temperature + "°C ");
        }
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


    //Provides weather data of a city
    //Mannheim: longitude = 49.4883333, latitude 8.4647222
    private void getWeather() {

            weatherclient.getCurrentCondition(new WeatherRequest(map.get("Longitude"), map.get("Latitude")),
                    new WeatherClient.WeatherEventListener() {
                        @Override
                        public void onWeatherRetrieved(CurrentWeather currentWeather) {
                            // Current Weather

                            temperature = (int) currentWeather.weather.temperature.getTemp();
                            //tempView.setText(temperature + "°C " + "in " + finalAddress);
                            Log.d("MainActivity", tempView.getText().toString() + " is the temperature");
                        }

                        @Override
                        public void onWeatherError(WeatherLibException e) {
                            Log.d("MainActivity", "Error WeatherLibException");
                        }


                        @Override
                        public void onConnectionError(Throwable throwable) {
                            Log.d("MainActivity", "Error Connection");
                        }
                    });

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
    //Get name of selected item in spinner. Put it in and get Object to get lon and lat.
    private Cities getCity(String name){
        switch (name){
            case "Berlin": return Cities.BERLIN;
            case "Ulm": return Cities.ULM;
            case "Biberach": return Cities.BIBERACH;
            default: return Cities.BERLIN;

        }

    }


}

