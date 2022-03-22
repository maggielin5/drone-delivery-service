package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Menus {

    private final List<MenuDetails> menuItems;

    /**
     * Anonymous inner class that matches JSON list exactly
     */
    public static class MenuDetails {
        String name;
        String location;

        List<Item> menu;
        public static class Item{
            String item;
            int pence;
        }
    }

    /**
     * Menus constructor
     * @param parser hostname
     */
    public Menus(Parser parser) {
        this.menuItems = parser.getMenus("http://" + parser.getMachine() + ":" + parser.getPort() + "/menus/menus.json");
    }

    /**
     * Method calculating the cost of delivery in pence
     * @param items that we are calculating the delivery cost of
     * @return int cost in pence of these items including delivery fee of 50p
     * @throws IllegalArgumentException if arguments are wrong type
     * @throws NullPointerException if null
     */
    public int getDeliveryCost(ArrayList<String> items){
        int delivery = 50;
        int total = 0;

        try {
            ArrayList<MenuDetails> menuList = (ArrayList<MenuDetails>) menuItems;

            HashMap<String, Integer> itemsMap = new HashMap<>();
            for (MenuDetails menuDetails : menuList) {
                for (MenuDetails.Item i: menuDetails.menu) {
                    itemsMap.put(i.item, i.pence);
                }
            }

            for (String string: items) {
                if (itemsMap.containsKey(string)) {
                    total += itemsMap.get(string);
                }
            }

        } catch (IllegalArgumentException | NullPointerException e){
            e.printStackTrace();
        }
        return total + delivery;
    }
}

