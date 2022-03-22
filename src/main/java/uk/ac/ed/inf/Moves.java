package uk.ac.ed.inf;

import java.util.ArrayList;

/**
 * Class to track moves (angles and points)
 */
public class Moves {

    public ArrayList<Integer> angles;
    public ArrayList<LongLat> points;

    public Moves() {
        this.points = new ArrayList<>();
        this.angles = new ArrayList();
    }

    // Getters

    public ArrayList<LongLat> getPoints() {
        return points;
    }

    public ArrayList<Integer> getAngles() {
        return angles;
    }
}

