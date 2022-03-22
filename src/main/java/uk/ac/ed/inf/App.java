package uk.ac.ed.inf;

import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpClient;
import java.sql.SQLException;
import java.util.ArrayList;

public class App {
    private static final String machine = "localhost";
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Method to get client which is used so there is ONE client for the whole implementation
     * @return
     */
    public static HttpClient getClient() {
        return client;
    }

    /**
     * Main method to handle the arguments from the command line. Using these arguments we instantiate the objects to be used
     * by the drone class to create flightpath. We also write deliveries for the day to the
     * @param args command line arguments
     */
    public static void main(String[] args) throws SQLException, IOException {

        // Arguments from command line
        String dd = args[0];
        String mm = args[1];
        String yyyy = args[2];
        String port = args[3];
        String db = args[4];

        // Connect to database and create tables
        Database database = new Database("jdbc:derby://localhost:" + db + "/derbyDB");
        database.createDeliveriesTable();
        database.createFlightpathTable();

        // Initialise other objects
        Parser parser = new Parser(machine, port);
        Menus menus = new Menus(parser);
        Buildings buildings = new Buildings(parser);

        // Get orders and menus info from database to use in drone
        ArrayList<Order> orders = database.findOrders(yyyy + "-" + mm + "-" + dd);
        ArrayList<Menus.MenuDetails> menuList = (ArrayList<Menus.MenuDetails>) parser.getMenus("http://" + machine + ":" + port + "/menus/menus.json");

        // Start drone flightpath
        Drone drone = new Drone(database, parser, menus, buildings, orders, menuList);
        drone.startPath();

        // Write deliveries to delivery table
        for (int i = 0; i < orders.size(); i++) {
            database.insertToDeliveriesTable(orders.get(i).getOrderNo(), orders.get(i).getDeliverTo(), menus.getDeliveryCost(orders.get(i).getFoodList()));
        }

        // Create geojson map from points in flightpath, output to new file
        try (FileWriter output = new FileWriter(
                "drone-" + args[0] + "-" + args[1] + "-" + args[2] + ".geojson")){
            output.write(drone.createFeatureCollection(drone.flightpathPoints).toJson());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
