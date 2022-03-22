package uk.ac.ed.inf;

import com.mapbox.geojson.*;

import java.util.ArrayList;

public class Buildings {
    private ArrayList<Point> landmarks;
    private ArrayList<Polygon> noFlyZones;

    /**
     * Buildings constructor
     * @param parser instantiated in App class
     */
    public Buildings(Parser parser) {
        this.landmarks = new ArrayList<Point>();
        this.noFlyZones = new ArrayList<Polygon>();

        // Convert features to points for landmarks
        var landmarkList = parser.getLandmarks();
        for (Feature i : landmarkList) {
            Point landmark = (Point) i.geometry();
            this.landmarks.add(landmark);
        }

        // Convert features to polygons for no fly zone
        var noFlyList = parser.getNoFlyZones();
        for (Feature i : noFlyList) {
            Polygon polygon = (Polygon) i.geometry();
            this.noFlyZones.add(polygon);
        }
    }

    // Code reworked from https://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/

    // Given three collinear points p, q, r, the function checks if
    // point q lies on line segment 'pr'
    static boolean onSegment(Point p, Point q, Point r)
    {
        if (q.longitude() <= Math.max(p.longitude(), r.longitude()) && q.longitude() >= Math.min(p.longitude(), r.longitude()) &&
                q.latitude() <= Math.max(p.latitude(), r.latitude()) && q.latitude() >= Math.min(p.latitude(), r.latitude()))
            return true;

        return false;
    }

    // To find orientation of ordered triplet (p, q, r).
    // The function returns following values
    // 0 --> p, q and r are collinear
    // 1 --> Clockwise
    // 2 --> Counterclockwise
    static int orientation(Point p, Point q, Point r)
    {
        double val = (q.latitude() - p.latitude()) * (r.longitude() - q.longitude()) -
                (q.latitude() - p.latitude()) * (r.latitude() - q.latitude());

        if (val == 0) return 0; // collinear

        return (val > 0)? 1: 2; // clock or counterclock wise
    }

    // The main function that returns true if line segment 'p1q1'
    // and 'p2q2' intersect.
    static boolean doIntersect(Point p1, Point q1, Point p2, Point q2) {
        // Find the four orientations needed for general and
        // special cases
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        // General case
        if (o1 != o2 && o3 != o4)
            return true;

        // Special Cases
        // p1, q1 and p2 are collinear and p2 lies on segment p1q1
        if (o1 == 0 && onSegment(p1, p2, q1)) return true;

        // p1, q1 and q2 are collinear and q2 lies on segment p1q1
        if (o2 == 0 && onSegment(p1, q2, q1)) return true;

        // p2, q2 and p1 are collinear and p1 lies on segment p2q2
        if (o3 == 0 && onSegment(p2, p1, q2)) return true;

        // p2, q2 and q1 are collinear and q1 lies on segment p2q2
        if (o4 == 0 && onSegment(p2, q1, q2)) return true;

        return false;
    }

    /**
     * Method to see if move crosses no-fly-zone
     * @param p currentPosition
     * @param q goalPosition
     * @return true if it does
     */
    public Boolean isInNoFlyZone(Point p, Point q) {
        for (Polygon poly : noFlyZones) {
            LineString polygonPerimeter = poly.outer();
            var pointsOnPerimeter = polygonPerimeter.coordinates();

            for (int i=0; i<pointsOnPerimeter.size(); i++){
                if (i < pointsOnPerimeter.size()-1){
                    if (doIntersect(p,q,pointsOnPerimeter.get(i),pointsOnPerimeter.get(i+1))){
                        return true;
                    }
                }
            }
        }
        return false;

    }

    // Getters

    public ArrayList<Point> getLandmarks() {
        return landmarks;
    }
}
