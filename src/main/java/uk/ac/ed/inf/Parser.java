package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.FeatureCollection;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;


public class Parser {

    public String machine;
    public String port;
    public String url;
    public final List<Feature> noFlyZones;
    public final List<Feature> landmarks;
    public final List<Menus.MenuDetails> menus;

    /**
     * Parser constructor to connect to web server and get data for buildings, menus and W3W words
     * @param machine localhost
     * @param port web server specified from command line
     */
    public Parser(String machine, String port) {
        this.machine = machine;
        this.port = port;
        this.noFlyZones = getBuildings("http://" + machine + ":" + port + "/buildings/no-fly-zones.geojson");
        this.landmarks = getBuildings("http://" + machine + ":" + port + "/buildings/landmarks.geojson");
        this.menus = getMenus("http://" + machine + ":" + port + "/menus/menus.json");
    }

    // Getters
    public List<Feature> getNoFlyZones() {
        return noFlyZones;
    }

    public List<Feature> getLandmarks() {
        return landmarks;
    }

    public String getMachine() {
        return machine;
    }

    public String getPort() {
        return port;
    }

    /**
     * Method to connect to web server and get buildings data
     * @param url string to connect to
     * @return list of features
     */
    public List<Feature> getBuildings(String url) {
        // HttpRequest assumes that it is a GET request by default.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        // We call the send method on the client which we created.
        try {
            HttpResponse<String> response = App.getClient().send(request, HttpResponse.BodyHandlers.ofString());
            List<Feature> buildings = FeatureCollection.fromJson(String.valueOf(response.body())).features();
            return buildings;
        }
        catch (IOException | InterruptedException e) {
                e.printStackTrace();
        }
        return null;
    }


    /**
     * Method to connect to web server and retrieve menu details
     * @param url string to connect to
     * @return list of menuDetails inside class object
     */
    public List<Menus.MenuDetails> getMenus(String url) {
        // HttpRequest assumes that it is a GET request by default.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        // We call the send method on the client which we created.
        try {
            HttpResponse<String> response = App.getClient().send(request, HttpResponse.BodyHandlers.ofString());
            Type listType = new TypeToken<ArrayList<Menus.MenuDetails>>(){}.getType();
            ArrayList<Menus.MenuDetails> menus = new Gson().fromJson(response.body(), listType);
            return menus;
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to get point from What3Words location string
     * @param location string representing What3Words
     * @return Point of location
     */
    public Point getWordsLoc(String location) {

        String[] loc = location.split("\\."); //split location into 3 parts
        W3W word = new W3W();

        // HttpRequest assumes that it is a GET request by default.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/words/" + loc[0] + "/" + loc[1] + "/" + loc[2] +"/details.json"))
                .build();
        // We call the send method on the client which we created.
        try {
            HttpResponse<String> response = App.getClient().send(request, HttpResponse.BodyHandlers.ofString());
            word = new Gson().fromJson(response.body(), W3W.class);
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return Point.fromLngLat(word.coordinates.Getlng(), word.coordinates.Getlat());
    }


}