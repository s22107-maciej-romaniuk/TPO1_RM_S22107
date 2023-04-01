package zad2;

import java.util.Objects;

public class City {
    public String cityName;
    public String countryCode;
    public String state;
    public Double lat;
    public Double lon;

    public City(String city, String countryCode, String state, Double latitude, Double longitude) {
        this.cityName = city;
        this.countryCode = countryCode;
        this.state = state;
        this.lat = latitude;
        this.lon = longitude;
    }

    @Override
    public boolean equals(Object city){
        if(city == null) return false;
        return this.hashCode() == city.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityName, countryCode, state);
    }

    @Override
    public String toString() {
        return
               "City Name='" + cityName + '\'' +
               ", Country Code='" + countryCode + '\'' +
               ", State Name='" + state + '\'' +
               ", Latitude=" + lat +
               ", Longitude=" + lon;
    }
}
