package uk.ac.dur.backend.datamodel;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static uk.ac.dur.Utils.generateID;

/**
 * This data class stores a restaurant, all of its fields are immutable.
 */
public final class Restaurant implements Serializable {

    private static final long serialVersionUID = -6890366190329848992L;

    private final String id;

    private final String name;
    private final Address address;

    private final int priceRating;

    private final List<MenuItem> menuItems;

    private final int deliveryCost;

    /**
     * Creates a new resturant
     * @param name The name of the restaurant
     * @param address The address
     * @param menuItems The list of menu items
     * @param deliveryCost The cost of delivery, in pennies
     * @param priceRating The price rating on a scale from 1 to 5
     */
    public Restaurant(String name, Address address, List<MenuItem> menuItems, int deliveryCost, int priceRating) {
        this.name = name;
        this.address = address;
        this.menuItems = menuItems;
        this.deliveryCost = deliveryCost;
        this.priceRating = priceRating;

        id = generateID();

    }


    public int getPriceRating() {
        return priceRating;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public int getDeliveryCost() {
        return deliveryCost;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "name='" + name + '\'' +
                '}';
    }

    public String getID() {
        return id;
    }

    /**
     * Generates the test set of restaurants
     * @return The test set
     */
    @NotNull
    public static List<Restaurant> getRestaurantTestSet() {
        return new LinkedList<>(Arrays.asList(
                new Restaurant("Pretty Pizzas",
                        new Address("1", "Science Site", -1.575756, 54.778267),
                        Arrays.asList(
                                new MenuItem("Pepperoni Pizza", true, 799, false),
                                new MenuItem("Margarita Pizza", true, 599, false),
                                new MenuItem("Veggie Pizza", true, 499, false)
                        ),
                        250,
                        2
                ),
                new Restaurant("Indian Ideal",
                        new Address("1", "Science Site", -1.574998, 54.776178),
                        Arrays.asList(
                                new MenuItem("Chicken Balti", true, 699, false),
                                new MenuItem("Chicken Korma", true, 699, false),
                                new MenuItem("Rice", true, 299, false),
                                new MenuItem("Nann Bread", true, 299, true)
                        ),
                        199,
                        4
                ),
                new Restaurant("Chewy Chinese",
                        new Address("1", "Science Site", -1.567847, 54.772949),
                        Arrays.asList(
                                new MenuItem("Spring Rolls", true, 599, false),
                                new MenuItem("Chow Mein", true, 640, false)
                        ),
                        299,
                        3
                ),
                new Restaurant("Big Burgers",
                        new Address("1", "Science Site", -1.570463, 54.767403),
                        Arrays.asList(
                                new MenuItem("Big Beef Burger", true, 599, false),
                                new MenuItem("Big Chicken Burger", true, 599, false),
                                new MenuItem("Big Chips", true, 149, true)
                        ),
                        150,
                        3
                ),
                new Restaurant("Mega Mexican",
                        new Address("1", "Science Site", -1.573229, 54.768106),
                        Arrays.asList(
                                new MenuItem("Taco", true, 799, false),
                                new MenuItem("Burrito", true, 1499, false),
                                new MenuItem("Nachos", true, 899, true)
                        ),
                        299,
                        3
                )
        ));
    }

}
