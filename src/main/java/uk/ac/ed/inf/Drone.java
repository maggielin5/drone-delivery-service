package uk.ac.ed.inf;

import com.mapbox.geojson.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Drone {

    // Define maximum moves
    public static final int maxMoves = 1500;

    private final ArrayList<Order> orders;
    private final LongLat ATPosition;
    private LongLat currentPosition;
    private ArrayList<LongLat> landmarks;
    private int totalMoves;
    private final ArrayList<Menus.MenuDetails> menuList;

    private Database database;
    private Parser parser;
    private Menus menus;
    private Buildings buildings;

    // Local variables to track flightpath points and angles that ARE travelled
    public List<Point> flightpathPoints = new ArrayList<>();
    private List<Integer> flightpathAngles = new ArrayList<>();

    // To track which orders are visited/not visited
    private ArrayList<Order> unvisited;
    private ArrayList<Order> visited;

    /**
     * Drone constructor
     * @param database instantiated in App class
     * @param parser instantiated in App class
     * @param menus instantiated in App class
     * @param buildings instantiated in App class
     * @param orders orders for the day
     * @param menuList list of items in menu
     */
    public Drone(Database database, Parser parser, Menus menus, Buildings buildings, ArrayList<Order> orders, ArrayList<Menus.MenuDetails> menuList) {
        this.database = database;
        this.parser = parser;
        this.menus = menus;
        this.buildings = buildings;
        this.orders = orders;
        this.menuList = menuList;
        
        this.ATPosition = new LongLat(-3.186874,55.944494);
        this.currentPosition = new LongLat(-3.186874,55.944494);
        this.landmarks = findLandmarksLongLats();
        this.totalMoves = 0;
    }

    /**
     * Method to start flightpath
     * Uses helper methods below to find flightpath for the day
     * @throws SQLException in case of database access error
     */
    public void startPath() throws SQLException {

        unvisited = orders;
        visited = new ArrayList<Order>();
        Order nextOrder = null;
        var index = 0;

        // Add AT to start of flightpath
        flightpathPoints.add(convertToPoint(ATPosition));

        while (!unvisited.isEmpty()) {
            nextOrder = findBestChoice();

            // Instantiate class to track moves
            Moves moves = new Moves();

            // In case we have to go back
            LongLat origin = currentPosition;

            // Pick up items
            ArrayList<LongLat> pickUpPoints = findDeliverFromLL(nextOrder);

            // Sort pickup points
            ArrayList<LongLat> sortedPickUpPoints = sortPickup(pickUpPoints);

            // Pickup order
            for (LongLat pickup : sortedPickUpPoints) {
                moves = moveTo(moves, pickup);
            }

            // Deliver order
            LongLat deliveryPoint = findDeliverToLL(nextOrder);
            moves = moveTo(moves, deliveryPoint);

            // If not enough moves to AT, go back to original starting position
            if (totalMoves + moves.angles.size() + movesToAT(currentPosition) > maxMoves) {
                currentPosition = origin;
            }
            else {
                for (LongLat p : moves.getPoints()) {
                    flightpathPoints.add(convertToPoint(p));
                }
                flightpathAngles.addAll(moves.getAngles());
                unvisited.remove(nextOrder);
                visited.add(nextOrder);
                totalMoves += moves.angles.size();

                for (int i = index; i < flightpathAngles.size(); i++) {
                    database.insertToFlightpathTable(nextOrder.getOrderNo(), flightpathPoints.get(i).longitude(), flightpathPoints.get(i).latitude(), flightpathAngles.get(i), flightpathPoints.get(i + 1).longitude(), flightpathPoints.get(i + 1).latitude());
                }

                // Update index
                index = flightpathPoints.size();
            }
        }

        // Return to AT
        Moves returnMoves = new Moves();

        // Instantiate class to track moves
        returnMoves = moveTo(returnMoves, ATPosition);
        // Take away hover from end
        returnMoves.points.remove(returnMoves.angles.size()-1);
        returnMoves.angles.remove(returnMoves.angles.size()-1);

        for (LongLat p: returnMoves.getPoints()) {
            flightpathPoints.add(convertToPoint(p));
        }
        flightpathAngles.addAll(returnMoves.getAngles());

        for (int i = index; i < flightpathAngles.size(); i++) {
            database.insertToFlightpathTable(nextOrder.getOrderNo(), flightpathPoints.get(i).longitude(), flightpathPoints.get(i).latitude(), flightpathAngles.get(i), flightpathPoints.get(i + 1).longitude(), flightpathPoints.get(i + 1).latitude());
        }
    }

    /**
     * Method to calculate number of moves to Appleton Tower
     * @param currentPosition position to calculate from
     * @return int number of moves
     */
    private int movesToAT(LongLat currentPosition) {
        var moves = currentPosition.distanceTo(ATPosition);
        var length = 0.00015;
        return (int) (moves/length);
    }

    /**
     * Method to sort pickup points from closest to farthest
     * Maximum two pickup poits per order
     * @param pickUpPoints to be sorted
     * @return ArrayList<LongLat> sorted pickUpPoints
     */
    private ArrayList<LongLat> sortPickup(ArrayList<LongLat> pickUpPoints) {
        if (pickUpPoints.size() >1) {

            int firstInArrayDist = (int) currentPosition.distanceTo(pickUpPoints.get(0));
            int secondInArrayDist = (int) currentPosition.distanceTo(pickUpPoints.get(1));

            if (firstInArrayDist > secondInArrayDist) {
                Collections.reverse(pickUpPoints);
            }
        }
        return pickUpPoints;
    }

    /**
     * Method to move the drone towards the goal position
     * Hovers after every pickup or delivery
     * @param moves object which tracks moves made
     * @param goalPosition position to move to
     * @return moves object
     */
    private Moves moveTo(Moves moves, LongLat goalPosition) {

        if (buildings.isInNoFlyZone(convertToPoint(currentPosition), convertToPoint(goalPosition))) {
            var landmarkToFlyTo = findBestLandmark(goalPosition);
            while (!currentPosition.closeTo(landmarkToFlyTo) && (currentPosition.isConfined())) {
                var angleBetween = currentPosition.calculateAngleTo(landmarkToFlyTo);
                moves.angles.add(angleBetween);
                LongLat nextPos = currentPosition.nextPosition(angleBetween);
                moves.points.add(nextPos);

                currentPosition = nextPos;
            }

            while (!currentPosition.closeTo(goalPosition) && (currentPosition.isConfined())) {
                var angleBetween = currentPosition.calculateAngleTo(goalPosition);
                moves.angles.add(angleBetween);
                LongLat nextPos = currentPosition.nextPosition(angleBetween);
                moves.points.add(nextPos);

                currentPosition = nextPos;
            }
        } else {
                while (!currentPosition.closeTo(goalPosition) && (currentPosition.isConfined())) {
                    var angleBetween = currentPosition.calculateAngleTo(goalPosition);
                    moves.angles.add(angleBetween);
                    LongLat nextPos = currentPosition.nextPosition(angleBetween);
                    moves.points.add(nextPos);

                    currentPosition = nextPos;
                }
            }

        // Hover after pickup/delivery
        moves.points.add(currentPosition);
        moves.angles.add(-999);
        return moves;
    }

    /**
     * Method which finds ideal landmark to fly to
     * Sorts landmarks from closest to furthest
     * Needs fixing
     * @return best landmark to fly to
     */
    private LongLat findBestLandmark(LongLat goalPosition) {
        var sortedLandmarks = landmarks;
        ArrayList <Double> distancesTo = new ArrayList<>();
        LongLat landmarkToUse;
        for (LongLat landmark: sortedLandmarks) {
            distancesTo.add(currentPosition.distanceTo(landmark));
        }

        for ( int i = 0; i < sortedLandmarks.size(); i++) {
            for (int j = i + 1; j < sortedLandmarks.size(); j++) {
                double dtmp= 0;
                LongLat stmp= null;
                if (distancesTo.get(i) > distancesTo.get(j)) {
                    dtmp = distancesTo.get(i);
                    distancesTo.set(i, distancesTo.get(j));
                    distancesTo.set(j, dtmp);
                    stmp = sortedLandmarks.get(i);
                    sortedLandmarks.set(i, sortedLandmarks.get(j));
                    sortedLandmarks.set(j, stmp);
                }
            }
        }

        // If line from current position to landmark goes through no-fly-zone
        // Use other landmark
        int score = 0;

        if (buildings.isInNoFlyZone(convertToPoint(currentPosition), convertToPoint(sortedLandmarks.get(0)))) {
            score = 0;
        }

        if (buildings.isInNoFlyZone(convertToPoint(currentPosition), convertToPoint(sortedLandmarks.get(1)))) {
            score = 1;
        }

        landmarkToUse = sortedLandmarks.get(score);
        return landmarkToUse;
    }

    /**
     * Method to convert LongLat object to geojson point
     * @param position LongLat to be converted
     * @return Point
     */
    private Point convertToPoint(LongLat position) {
        Point point = Point.fromLngLat(position.longitude, position.latitude);
        return point;
    }

    /**
     * Method to find best order to complete next
     * Goes through unvisited orders, and calculates price / distance score
     * @return Order with max score
     */
    public Order findBestChoice() {
        ArrayList<Double> scores = new ArrayList<Double>();
        ArrayList<Double> distances = new ArrayList<Double>();
        ArrayList<Double> prices = new ArrayList<>();

        for (Order order: unvisited) {
            var deliveryLoc = findDeliverToLL(order);
            var shops = findDeliverFromLL(order);
            double distance = 0;
            double price = 0;
            double score = 0;

            price = menus.getDeliveryCost(order.getFoodList());
            prices.add(price);

            if (shops.size() > 1) {
                // Order made up from max two shops
                var firstInArrayDist = 0;
                var secondInArrayDist = 0;

                firstInArrayDist = (int) currentPosition.distanceTo(shops.get(0));
                secondInArrayDist = (int) currentPosition.distanceTo(shops.get(1));

                if (firstInArrayDist > secondInArrayDist) {
                    distance += secondInArrayDist + shops.get(0).distanceTo(shops.get(1)) + shops.get(0).distanceTo(deliveryLoc);
                }
                else {
                    distance += firstInArrayDist + shops.get(1).distanceTo(shops.get(0)) + shops.get(1).distanceTo(deliveryLoc);
                }
            }
            else {
                distance = shops.get(0).distanceTo(deliveryLoc);
            }
            distances.add(distance);

            // Want max money per unit of distance
            score = price / distance;
            scores.add(score);

        }

        Double bestVal = Collections.max(scores);
        int bestIdx = scores.indexOf(bestVal);

        return orders.get(bestIdx);
    }

    /**
     * Method to retrieve landmarks from buildings object
     * @return ArrayList<LongLat> landmarks
     */
    public ArrayList<LongLat> findLandmarksLongLats() {
        var landmarksPoints = buildings.getLandmarks();
        var landmarksLL = new ArrayList<LongLat>();
        for (Point p: landmarksPoints) {
            landmarksLL.add(new LongLat(p.longitude(), p.latitude()));
        }
        return landmarksLL;
    }

    /**
     * Method to find delivery location of an order from What3Words data
     * @param order order to be delivered
     * @return LongLat delivery location
     */
    public LongLat findDeliverToLL(Order order) {
        String location = order.getDeliverTo();
        Point locPoint = parser.getWordsLoc(location);
        return new LongLat(locPoint.longitude(), locPoint.latitude());
    }

    /**
     * Method to find shop locations of an order from What3Words data
     * Using menuList to find which shops the items in the order are from
     * @param order order to be picked up
     * @return ArrayList<LongLat> shops
     */
    public ArrayList<LongLat> findDeliverFromLL(Order order) {
        ArrayList<String> items = order.getFoodList();
        ArrayList<String> locs = new ArrayList<String>();
        ArrayList<LongLat> pickupLLs = new ArrayList<LongLat>();

        for (String item : items) {
            // Iterate through all items
            for (Menus.MenuDetails shops : menuList) {
                for (Menus.MenuDetails.Item i : shops.menu) {

                    // if string matches menu item, add shop location
                    if (i.item.equals(item)) {
                        if (!locs.contains(shops.location)) {
                            locs.add(shops.location);
                        }
                    }
                }
            }
        }

        for (String loc: locs) {
            Point parsedPoint = parser.getWordsLoc(loc);
            pickupLLs.add(new LongLat(parsedPoint.longitude(), parsedPoint.latitude()));
        }
        return pickupLLs;
    }

    /**
     * Method to create FeatureCollection of single linestring of flightpath points
     * Written to output geojson file
     * @param flightpathPoints points used in flightpath
     * @return FeatureCollection containing one linestring to represent the path
     */
    public FeatureCollection createFeatureCollection(List<Point> flightpathPoints) {
        List<Feature> features = new ArrayList<Feature>();
        features.add(Feature.fromGeometry(LineString.fromLngLats(flightpathPoints)));
        return FeatureCollection.fromFeatures(features);
    }

    }

