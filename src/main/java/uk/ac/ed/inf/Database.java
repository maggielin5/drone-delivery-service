package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    /**
     * Database class constructor
     * @param jdbcString
     */
    public Database(String jdbcString) {
        this.jdbcString = jdbcString;
    }

    public String jdbcString;

    /**
     * Method to create flightpath table
     * @return true if successful
     */
    public boolean createFlightpathTable() {
        try {
            Connection conn = DriverManager.getConnection(jdbcString);

            // Create a statement object that we can use for running various
            // SQL statement commands against the database.
            Statement statement = conn.createStatement();

            DatabaseMetaData databaseMetadata = conn.getMetaData();
            ResultSet resultSet = databaseMetadata.getTables(null, null, "FLIGHTPATH", null);

            // If the resultSet is not empty then the table exists, so we can drop it
            if (resultSet.next()) {
                statement.execute("drop table FLIGHTPATH");
            }

            statement.execute("create table flightpath(" + "orderNo varchar(8), " + "fromLongitude double, " + "fromLatitude double, " + "angle int, " + "toLongitude double, " + "toLatitude double)");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Method to create deliveries table
     * @return true if successful
     */
    public boolean createDeliveriesTable() {
        try {
            Connection conn = DriverManager.getConnection(jdbcString);

            // Create a statement object that we can use for running various
            // SQL statement commands against the database.
            Statement statement = conn.createStatement();

            DatabaseMetaData databaseMetadata = conn.getMetaData();

            ResultSet resultSet = databaseMetadata.getTables(null, null, "DELIVERIES", null);

            // If the resultSet is not empty then the table exists, so we can drop it
            if (resultSet.next()) {
                statement.execute("drop table DELIVERIES");
            }

            statement.execute("create table deliveries(" + "orderNo varchar(8), " + "deliveredTo varchar(19), " + "costInPence int)");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;

    }

    /**
     * Method to find orders and order details (items) on specificied date
     * @param date date specified on command line
     * @return Order object
     * @throws SQLException
     */
    public ArrayList<Order> findOrders(String date) throws SQLException {
        Connection conn = DriverManager.getConnection(jdbcString);

        final String coursesQuery = "select * from orders where deliveryDate=(?)";
        PreparedStatement psCourseQuery = conn.prepareStatement(coursesQuery);
        psCourseQuery.setString(1, date);

        // Search for the student’s courses and add them to a list
        ArrayList<Order> orderList = new ArrayList<>();
        ResultSet rs = psCourseQuery.executeQuery();
        while (rs.next()) {
            String orderNumber = rs.getString("orderNo");
            String deliveryDate = rs.getString("deliveryDate");
            String customer = rs.getString("customer");
            String deliverTo = rs.getString("deliverTo");

            final String detailsQuery = "select * from orderDetails where orderNo=(?)";
            PreparedStatement psDetailsQuery = conn.prepareStatement(detailsQuery);
            psDetailsQuery.setString(1, orderNumber);

            // Search for the student’s courses and add them to a list
            ArrayList<String> foodList = new ArrayList<>();
            ResultSet fs = psDetailsQuery.executeQuery();
            while (fs.next()) {
                String item = fs.getString("item");
                foodList.add(item);
            }

            orderList.add(new Order(orderNumber, deliveryDate, customer, deliverTo, foodList));
        }
        return orderList;
    }

    /**
     * Method to insert information to deliveries table
     * @param orderNo order number
     * @param deliverTo student number
     * @param costInPence total cost of items (including delivery)
     * @return true if successful
     * @throws SQLException
     */
    public boolean insertToDeliveriesTable(String orderNo, String deliverTo, int costInPence) throws SQLException {
        Connection conn = DriverManager.getConnection(jdbcString);
        PreparedStatement delivery = null;
        try {
            delivery = conn.prepareStatement("insert into deliveries values (?, ?, ?)");
            delivery.setString(1, orderNo);
            delivery.setString(2, deliverTo);
            delivery.setInt(3, costInPence);
            delivery.execute();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Method to insert information to flightpath table
     * @param orderNo order number
     * @param fromLongitude currentPosition longitude
     * @param fromLatitude currentPosition latitude
     * @param angle int angle between current and next position
     * @param toLongitude nextPosition longitude
     * @param toLatitude nextPosition latitude
     * @return true if successful
     * @throws SQLException
     */
    public boolean insertToFlightpathTable(String orderNo, Double fromLongitude, Double fromLatitude, int angle, Double toLongitude, Double toLatitude) throws SQLException {
        Connection conn = DriverManager.getConnection(jdbcString);
        PreparedStatement flightpath = null;
        try {
            flightpath = conn.prepareStatement("insert into flightpath values (?, ?, ?, ?, ?, ?)");
            flightpath.setString(1, orderNo);
            flightpath.setDouble(2, fromLongitude);
            flightpath.setDouble(3, fromLatitude);
            flightpath.setInt(4, angle);
            flightpath.setDouble(5, toLongitude);
            flightpath.setDouble(6, toLatitude);
            flightpath.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}


