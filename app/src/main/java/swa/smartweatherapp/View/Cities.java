package swa.smartweatherapp.View;

/**
 * Created by Marko on 17.03.16.
 */
public enum Cities {
    BERLIN("Berlin",52.520007,13.404954),
    BARCELONA ("Barcelona", 41.385064,2.173403),
    ULM ("Ulm", 48.401082, 9.987608),
    BIBERACH ("Biberach",48.095147,9.790152);


    private String name;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    private double latitude;
    private double longitude;

    Cities(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

public static String[] getCityNames(){
    String[] array = new String[values().length];
        for(int i=0; i<values().length;i++){
            array[i] = values()[i].getName();
        }
    return array;
    }
}
