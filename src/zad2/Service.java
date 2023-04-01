/**
 *
 *  @author Romaniuk Maciej S22107
 *
 */

package zad2;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Service {

    public String city;
    public Double latitude = 0.0;
    public Double longitude = 0.0;
    public String countryCode;
    public String countryCurrencyCode;
    public Double currencyRate;
    public String currencyCode;
    public Double nbpRate;

    public String weather;

    private final String apiKey = "58dac5991d7d460cc17bf8ea91d2bb22";
    public String state;


    public Service(String country) {
        this.getCountryCode(country);
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("latitude: ").append(latitude).append("\n");
        builder.append("longitude: ").append(longitude).append("\n");
        builder.append("countryCode: ").append(countryCode).append("\n");
        builder.append("countryCurrencyCode: ").append(countryCurrencyCode).append("\n");
        builder.append("currencyRate: ").append(currencyRate).append("\n");
        builder.append("currencyCode: ").append(currencyCode).append("\n");
        builder.append("nbpRate: ").append(nbpRate).append("\n");
        builder.append("weather: ").append(weather).append("\n");

        return builder.toString();
    }

    public String getCountryCode(String country) throws NullPointerException{
        Map<String, String> countries = new HashMap<>();
        for (String iso : Locale.getISOCountries()) {
            Locale l = new Locale("", iso);
            countries.put(l.getDisplayCountry(), iso);
        }
        this.countryCode = countries.get(country);
        this.countryCurrencyCode = this.getCurrency();
        return this.countryCode;
    }

    public String getWeather(String city) {
        try {
            this.getGeolocation(city);
            String link = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=metric",
                                        this.latitude, this.longitude, this.apiKey);
            System.out.println(link);
            URL url = new URL(link);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder builder = new StringBuilder(in.readLine());
            String response = builder.toString();
            in.close();
            JSONObject obj = new JSONObject(response);
            this.weather = obj.toString();
        }
        catch(Exception ex){
            this.weather = ex.getMessage();
        }
        return this.weather;
    }

    public String getWeatherWithCurrentCountryAndState() {
        try {
            String link = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=metric",
                                        this.latitude, this.longitude, this.apiKey);
            System.out.println(link);
            URL url = new URL(link);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder builder = new StringBuilder(in.readLine());
            String response = builder.toString();
            in.close();
            JSONObject obj = new JSONObject(response);
            this.weather = obj.toString();
        }
        catch(Exception ex){
            this.weather = "An error occured while retrieving data";
        }
        return this.weather;
    }

    public Double getRateFor(String currencyCode) {
        try {
            this.currencyCode = currencyCode;
            String link = String.format("https://api.exchangerate.host/latest?base=%s&symbols=%s",
                                        this.countryCurrencyCode, currencyCode);
            URL url = new URL(link);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder builder = new StringBuilder(in.readLine());
            String response = builder.toString();
            in.close();
            JSONObject obj = new JSONObject(response);
            this.currencyRate = obj.getJSONObject("rates").getDouble(currencyCode);
            return this.currencyRate;
        }
        catch(Exception ex){
            this.currencyRate = -1.0;
        }
        return this.currencyRate;
    }

    public Double getNBPRate() {
        if(this.countryCurrencyCode.equals("PLN")) this.nbpRate = 1.0;
        else {
            try {
                if (!searchNBPtable('A')) searchNBPtable('B');
            } catch (Exception ex) {
                this.nbpRate = -1.0;
            }
        }
        return this.nbpRate;
    }


    public Set<City> getGeolocation(String cityName) throws Exception {
        this.city = cityName;
        this.state = null;
        Set<City> cities = new HashSet<>();
        String link = String.format("http://api.openweathermap.org/geo/1.0/direct?q=%s,%s&limit=100&appid=%s",
                                    cityName, this.countryCode,  this.apiKey);
        System.out.println(link);
        URL url = new URL(link);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder builder = new StringBuilder(in.readLine());
        String response = builder.toString();
        in.close();
        JSONArray possibleCities = new JSONArray(response);
        if(possibleCities.length() == 0) throw new Exception("Did not find city");
        for(Object possibleCity : possibleCities){
            JSONObject city = (JSONObject) possibleCity;
            System.out.println(city.getString("country"));
            if(city.getString("country").equals(this.countryCode)){
                this.latitude = city.getDouble("lat");
                this.longitude = city.getDouble("lon");
                if(possibleCities.length() > 1) {
                    this.state = city.getString("state");
                    City newCity = new City(this.city, this.countryCode, this.state, this.latitude, this.longitude);
                    System.out.println(newCity.hashCode());
                    cities.add(newCity);

                }
                else{
                    this.state = null;
                    break;
                }
            }
        }
        System.out.println(cities);
        if(cities.size() <= 1) this.state = null;
        return cities;
    }

    public String getCurrency() throws NullPointerException{
        this.countryCurrencyCode = Currency.getInstance(new Locale("", this.countryCode)).getCurrencyCode();
        return this.countryCurrencyCode;
    }


    public boolean searchNBPtable(Character table) throws IOException {
        String link = String.format("http://api.nbp.pl/api/exchangerates/tables/%s/", table);
        System.out.println(link);
        URL url = new URL(link);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder builder = new StringBuilder(in.readLine());
        builder.deleteCharAt(0);
        builder.deleteCharAt(builder.length()-1);
        String response = builder.toString();
        in.close();
        JSONObject obj = new JSONObject(response);
        JSONArray rates = obj.getJSONArray("rates");
        for(Object rateObject : rates){
            JSONObject rate = (JSONObject) rateObject;
            if(rate.getString("code").equals(this.countryCurrencyCode)){
                this.nbpRate = rate.getDouble("mid");
                return true;
            }
        }
        return false;
    }
}  
