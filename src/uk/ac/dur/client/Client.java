package uk.ac.dur.client;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import uk.ac.dur.datamodel.MenuItem;
import uk.ac.dur.datamodel.Restaurant;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * The client user interface is modeled as a finite state machine with the following states.
 */
enum ClientStates {
    LANDING_STATE,
    GET_LOCATION,
    RESTAURANT_LIST,
    ADD_ITEM,
    VIEW_CART,
    TRACK_ORDER,
    MANAGE_RESTAURANT,
    EXIT
}

public class Client {


    public static void main(String[] args) {

        ClientStates state = ClientStates.LANDING_STATE;

        String location = null;

        Restaurant chooseRestaurant = null;
        Map<MenuItem, Integer> cart = null;

        Scanner scanner = new Scanner(System.in).useDelimiter("\n");

        while (state != ClientStates.EXIT) {
            if (state == ClientStates.LANDING_STATE) {

                System.out.println("\nWelcome to Just Hungry!");
                System.out.println("1) Order Food");
                System.out.println("2) Track Order");
                System.out.println("3) Manage Restaurant");
                System.out.println("4) Exit");

                int choice;
                try {
                    choice = scanner.nextInt();
                } catch (Exception e) {
                    System.out.println("Invalid choice");
                    continue;
                }

                if (choice == 1) {
                    if (location == null) {
                        state = ClientStates.GET_LOCATION;
                    } else {
                        state = ClientStates.RESTAURANT_LIST;
                    }
                } else if (choice == 2) {
                    state = ClientStates.TRACK_ORDER;
                } else if (choice == 3) {
                    state = ClientStates.MANAGE_RESTAURANT;
                } else if (choice == 4) {
                    state = ClientStates.EXIT;
                } else {
                    System.out.println("Invalid Choice");
                }

            } else if (state == ClientStates.GET_LOCATION) {

                System.out.println("\nEnter your postcode:");
                location = scanner.next();
                state = ClientStates.RESTAURANT_LIST;

            } else if (state == ClientStates.RESTAURANT_LIST) {
                System.out.println("\nChoose a restaurant");

                List<Restaurant> restaurants = getRestaurants();

                for (int i = 0; i < restaurants.size(); i++) {
                    Restaurant current = restaurants.get(i);
                    System.out.printf(
                            "%d)\t%-25s\t%s\t%d min\n",
                            i + 1,
                            current.getName(),
                            "£".repeat(current.getPrice()),
                            current.getDeliveryTime()
                    );

                }

                int choice;
                try {
                    choice = scanner.nextInt() - 1;
                } catch (Exception e) {
                    System.out.println("Invalid choice");
                    continue;
                }

                if (choice < 0 || choice >= restaurants.size()) {
                    System.out.println("Invalid choice");
                    continue;
                }

                chooseRestaurant = restaurants.get(choice);
                cart = new HashMap<>();
                state = ClientStates.ADD_ITEM;

            } else if (state == ClientStates.ADD_ITEM) {

                int itemsInCart = cart.values().stream().reduce(0, Integer::sum);
                System.out.printf("\nAdd to Cart (%d):\n", itemsInCart);
                System.out.println("0)\tView Cart (exit)");
                List<MenuItem> items = chooseRestaurant.getMenuItems();
                for (int i = 0; i < items.size(); i++) {
                    MenuItem current = items.get(i);
                    System.out.printf(
                            "%d)\t%-25s\t£%.2f\t%s\t%s\n",
                            i + 1,
                            current.getName(),
                            current.getPrice() / 100f,
                            current.isAvailable() ? "" : "NOT AVAILABLE",
                            current.isVegetarian() ? "(vegetarian)" : ""
                    );

                }

                int choice;
                try {
                    choice = scanner.nextInt();
                } catch (Exception e) {
                    System.out.println("Invalid choice");
                    continue;
                }

                if (choice < 0 || choice > items.size()) {
                    System.out.println("Invalid choice");
                    continue;
                }

                if (choice == 0) {
                    state = ClientStates.VIEW_CART;
                } else {
                    MenuItem item = items.get(choice - 1);
                    cart.compute(item, (menuItem, total) -> {
                        if (total == null) {
                            return 1;
                        } else {
                            return total + 1;
                        }
                    });
                }

            } else if (state == ClientStates.VIEW_CART) {

                System.out.println("\nYour Cart:");
                cart.forEach((key, value) -> System.out.printf(
                        "%dx £%.2f %s",
                        value,
                        key.getPrice() / 100f,
                        key.getName()
                ));

                if (cart.size() == 0) {
                    System.out.println("Your cart is empty");
                }

                int total = cart.entrySet().stream().map(entry ->
                        entry.getKey().getPrice() * entry.getValue()
                ).reduce(Integer::sum).orElse(0);

                System.out.printf("\nTotal:£%.2f\n", total / 100f);

                System.out.println("1) Add more items");
                System.out.println("2) Place order");

                int choice;
                try {
                    choice = scanner.nextInt();
                } catch (Exception e) {
                    System.out.println("Invalid choice");
                    continue;
                }

                if (choice < 1 || choice > 2) {
                    System.out.println("Invalid choice");
                    continue;
                }

                if (choice == 1) {
                    state = ClientStates.ADD_ITEM;
                } else {
                    // todo make order
                }


            } else if (state == ClientStates.TRACK_ORDER) {

            } else if (state == ClientStates.MANAGE_RESTAURANT) {

            }


        }

    }

    @Contract(pure = true)
    private static @NotNull List<Restaurant> getRestaurants() {

        return Arrays.asList(
                new Restaurant("Pretty Pizzas",
                        "Science Site",
                        100,
                        100,
                        Arrays.asList(
                                new MenuItem("Pepperoni Pizza", true, 1000, 100),
                                new MenuItem("Margarita Pizza", true, 1000, 100),
                                new MenuItem("Veggie Pizza", true, 1000, 100)
                        )
                ),
                new Restaurant("Indian Ideal",
                        "Science Site",
                        100,
                        100,
                        Arrays.asList(
                                new MenuItem("Balti", true, 1000, 100),
                                new MenuItem("Korma", true, 1000, 100),
                                new MenuItem("Nann Bread", true, 1000, 100)
                        )

                ),
                new Restaurant("Chewy Chinese",
                        "Science Site",
                        100,
                        100,
                        Arrays.asList(
                                new MenuItem("Spring Roll", true, 1000, 100),
                                new MenuItem("Chow Mein", true, 1000, 100)
                        )
                ),
                new Restaurant("Big Burgers",
                        "Science Site",
                        100,
                        100,
                        Arrays.asList(
                                new MenuItem("Pepperoni Pizza", true, 1000, 100),
                                new MenuItem("Margarita Pizza", true, 1000, 100))
                ),
                new Restaurant("Mega Mexican",
                        "Science Site",
                        100,
                        100,
                        Arrays.asList(
                                new MenuItem("Pepperoni Pizza", true, 1000, 100),
                                new MenuItem("Margarita Pizza", true, 1000, 100))
                )
        );

    }
}
