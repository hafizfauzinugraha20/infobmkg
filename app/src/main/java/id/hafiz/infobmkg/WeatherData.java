package id.hafiz.infobmkg;

import com.google.gson.annotations.SerializedName;

public class WeatherData {
    @SerializedName("current_weather")
    public CurrentWeather currentWeather;

    public static class CurrentWeather {
        @SerializedName("temperature")
        public double temperature;

        @SerializedName("windspeed")
        public double windspeed;

        @SerializedName("weathercode")
        public int weathercode; // Kode cuaca WMO (0=Cerah, 1-3=Berawan, dst)

        @SerializedName("time")
        public String time;
    }
}