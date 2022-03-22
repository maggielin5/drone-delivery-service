package uk.ac.ed.inf;

/**
 * Class that matches What3Words data exactly
 * Used to retrieve coordinate from What3Words data
 */

public class W3W {
    String country;

    Square square;

    public class Square {
        Southwest southwest;

        public class Southwest {
            double lng;
            double lat;
        }

        Northeast northeast;

        public class Northeast {
            double lng;
            double lat;
        }
    }

    String nearestPlace;

    Coordinates coordinates;

    public class Coordinates {
        double lng;
        double lat;

        // Getters

        public Double Getlng() {
            return lng;
        }

        public Double Getlat(){
            return lat;
        }
    }

    String words;
    String language;
    String map;


}
