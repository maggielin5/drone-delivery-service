package uk.ac.ed.inf;

public class LongLat {

    public double longitude;
    public double latitude;

    // Define length of move
    private final static double length = 0.00015;

    // Confinement zone boundaries
    public final double xMin = -3.192473;
    public final double xMax = -3.184319;
    public final double yMin = 55.942617;
    public final double yMax = 55.946233;


    /**
     * LongLat constructor
     * @param longitude x value
     * @param latitude y value
     */
    public LongLat(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // Getters
    public double getLongitude() {
        return this.longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    /**
     * No-parameter method which says if the point is in bounds or not
     * @return true if the point is within the drone confinement area
     */
    public boolean isConfined() {
        boolean valid_x = false;
        boolean valid_y = false;

        if ((longitude > xMin) && (longitude < xMax)) {
            valid_x = true;
        }
        if ((latitude > yMin) && (latitude < yMax)) {
            valid_y = true;
        }
        return (valid_x && valid_y);
    }

    /**
     * Method to calculate distance between two points
     * @param point LongLat object
     * @return the Pythagorean distance between two points
     */
    public double distanceTo(LongLat point) {
        double x_difference = point.longitude - longitude;
        double y_difference = point.latitude - latitude;

        return Math.sqrt((x_difference * x_difference) + (y_difference * y_difference));
    }

    /**
     * Method that checks if points are close to each other
     * @param point LongLat object to be checked
     * @return true if distance is strictly less than the distance tolerance (length)
     */
    public boolean closeTo(LongLat point) {
        if (distanceTo(point) < length) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method that returns the new position of the drone if it makes a move in the direction of the angle
     * @param angle direction given
     * @return LongLat object that represents new position
     * @throws IllegalArgumentException if angle is invalid
     */
    public LongLat nextPosition(int angle) {
        // drone is flying
        if ((angle >= 0) && (angle <= 350) && (angle % 10 == 0)) {

            double x_move = length * Math.cos(Math.toRadians(angle));
            double y_move = length * Math.sin(Math.toRadians(angle));

            return new LongLat(longitude + x_move, latitude + y_move); }
        // drone is hovering
        else if (angle == -999) {
            return new LongLat(longitude,latitude); }
        else { throw new IllegalArgumentException(); }

    }

    /**
     * Method that returns angle between current position to new position
     * Used to move towards new position
     * @param goalPosition position to calculate angle to
     * @return int angle
     */
    public int calculateAngleTo(LongLat goalPosition) {
        double angle;
        angle = Math.toDegrees(Math.atan2(goalPosition.latitude - latitude, goalPosition.longitude - longitude));
        angle = angle + Math.ceil( -angle / 350) * 350;
        int roundedAngle = (int) Math.round(angle/10.0) * 10;

        return roundedAngle;
    }
}
