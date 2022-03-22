package uk.ac.ed.inf;

import java.util.ArrayList;

/**
 * Class to track orders
 */
public class Order {

    private final String orderNo;
    private final String deliveryDate;
    private final String customer;
    private final String deliverTo;
    private final ArrayList<String> foodList;


    /**
     * Order constructor
     * @param orderNo order number
     * @param deliveryDate delivery date
     * @param customer student number
     * @param deliverTo
     * @param foodList
     */
    public Order(String orderNo, String deliveryDate, String customer, String deliverTo, ArrayList<String> foodList) {
        this.orderNo = orderNo;
        this.deliveryDate = deliveryDate;
        this.customer = customer;
        this.deliverTo = deliverTo;
        this.foodList = foodList;
    }

    // Getters

    public ArrayList<String> getFoodList() {
        return foodList;
    }

    public String getDeliverTo() {
        return deliverTo;
    }

    public String getOrderNo() {
        return orderNo;
    }

}

