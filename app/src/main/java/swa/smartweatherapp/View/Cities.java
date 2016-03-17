package swa.smartweatherapp.View;

/**
 * Created by Marko on 17.03.16.
 */
public enum Cities {
    BERLIN ("Berlin", 23,23),
    ULM ("Ulm", 23, 22),
    BIBERACH ("Biberach",11,11);


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
