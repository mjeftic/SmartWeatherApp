package swa.smartweatherapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

public class MainActivity extends Activity {

    private TextView tempView;

    WeatherClient weatherclient=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tempView = (TextView) findViewById(R.id.tempView);
        initWeatherClient();
        getWeather();


    }

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

    private void getWeather() {
        weatherclient.getCurrentCondition(new WeatherRequest("2873891"),
                new WeatherClient.WeatherEventListener() {
                    @Override
                    public void onWeatherRetrieved(CurrentWeather currentWeather) {
                        // We have the current weather now
                        // Update subtitle toolbar

                        tempView.setText(String.format("%.0f", currentWeather.weather.temperature.getTemp()));

                    }

                    @Override
                    public void onWeatherError(WeatherLibException e) {

                    }

                    @Override
                    public void onConnectionError(Throwable throwable) {

                    }
                });
    }
}
