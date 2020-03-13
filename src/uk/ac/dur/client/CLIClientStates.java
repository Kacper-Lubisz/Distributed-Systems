package uk.ac.dur.client;

import org.jetbrains.annotations.NotNull;
import uk.ac.dur.backend.datamodel.MenuItem;
import uk.ac.dur.backend.datamodel.Order;
import uk.ac.dur.backend.datamodel.Restaurant;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The client user interface is modeled as a finite state machine with the following states.
 */
enum CLIClientStates {


    MAIN_MENU {
        @Override
        public CLIClientStates updateState(CLIClient client, Scanner stdin) {

            System.out.println("\nWelcome to Just Hungry!");
            System.out.println("1) Order Food");
            System.out.println("2) Track Order");
            System.out.println("3) Manage Restaurant");
            System.out.println("4) Exit");

            String choice = stdin.next();

            switch (choice) {
                case "1":
                    return CLIClientStates.GET_LOCATION;
                case "2":
                    return CLIClientStates.TRACK_ORDER;
                case "3":
                    return CLIClientStates.MANAGE_RESTAURANT_MENU;
                case "4":
                    return null; //exit

                default:
                    System.out.println("Invalid Choice");
                    return this;
            }
        }
    },
    GET_LOCATION {
        @Override
        public CLIClientStates updateState(CLIClient client, Scanner stdin) {

            getAddress(stdin, client);
            return RESTAURANT_LIST;
        }
    },
    RESTAURANT_LIST {
        @Override
        public CLIClientStates updateState(CLIClient client, Scanner stdin) {

            try {
                // stress test code
//                IntStream.range(0, 10000).parallel().forEach(value ->
//                        {
//                            try {
//                                client.createOrder(new HashMap<>(), "");
//                            } catch (RemoteException e) {
//                                e.printStackTrace();
//
//                            }
//                        }
//                );

                List<Restaurant> restaurants = client.connection.getRestaurants(client.address, 10);
                System.out.println("\nChoose one of these 10 closest restaurants:");

                for (int i = 0; i < restaurants.size(); i++) {
                    Restaurant current = restaurants.get(i);
                    double distance = current.getAddress().getStraightLineDistance(client.address) / 1600;
                    System.out.printf(
                            "%d)\t%-25s\t%s\t%.1f miles\n",
                            i + 1,
                            current.getName(),
                            "£".repeat(current.getPriceRating()),
                            distance
                    );

                }

                String choice = stdin.next();

                int chosen = Integer.parseInt(choice);

                if (chosen < 0 || chosen > restaurants.size()) {
                    System.out.println("Invalid choice");
                    return this;
                }

                if (chosen == 0) {
                    return MAIN_MENU;

                } else {
                    client.currentRestaurant = restaurants.get(chosen - 1);
                    client.cart = new HashMap<>();
                    return CLIClientStates.ADD_ITEM;
                }
            } catch (RemoteException e) {
                System.out.println("Failed to load restaurants");
                System.out.println("Please try again!");
                return MAIN_MENU;

            }

        }
    },
    ADD_ITEM {
        @Override
        public CLIClientStates updateState(CLIClient client, Scanner stdin) {

            int itemsInCart = client.cart.values().stream().reduce(0, Integer::sum);

            System.out.printf("\nAdd to Cart (%d):\n", itemsInCart);
            System.out.println("0)\tView Cart/Checkout/Exit");

            List<MenuItem> items = client.currentRestaurant.getMenuItems();
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

            String choice;
            try {
                choice = stdin.next();
            } catch (Exception e) {
                System.out.println("Invalid choice");
                return this;
            }

            int chosen = Integer.parseInt(choice);

            if (chosen < 0 || chosen > items.size()) {
                System.out.println("Invalid choice");
                return this;
            }

            if (chosen == 0) {
                return CLIClientStates.VIEW_CART;
            } else {
                MenuItem item = items.get(chosen - 1);
                client.cart.compute(item, (menuItem, total) -> {
                    if (total == null) {
                        return 1;
                    } else {
                        return total + 1;
                    }
                });
            }
            return this;
        }
    },
    VIEW_CART {
        @Override
        public CLIClientStates updateState(CLIClient client, Scanner stdin) {

            System.out.println("\nYour Cart:");
            client.cart.forEach((key, value) -> System.out.printf(
                    "%dx £%.2f %s\n",
                    value,
                    key.getPrice() / 100f,
                    key.getName()
            ));

            System.out.printf("Delivery Charge: %.2f\n", client.currentRestaurant.getDeliveryCost() / 100f);

            if (client.cart.size() == 0) {
                System.out.println("Your cart is empty");
            }

            int total = client.cart.entrySet().stream().map(entry ->
                    entry.getKey().getPrice() * entry.getValue()
            ).reduce(Integer::sum).orElse(0) + client.currentRestaurant.getDeliveryCost();

            System.out.printf("\nTotal:£%.2f\n", total / 100f);

            System.out.println("1) Add more items");
            System.out.println("2) Place order");
            System.out.println("3) Discard Order");

            String choice;
            try {
                choice = stdin.next();
            } catch (Exception e) {
                System.out.println("Invalid choice");
                return this;
            }

            switch (choice) {
                case "1":
                    return CLIClientStates.ADD_ITEM;
                case "2": // make an order

                    if (client.cart.size() == 0) {
                        System.out.println("Your cart is empty! Couldn't place order");
                        return CLIClientStates.VIEW_CART;
                    } else {

                        System.out.println("Placing order");

                        try {

                            Order order = client.connection.createOrder(client.cart, client.currentRestaurant.getID());


                            System.out.println("Order has been placed!");
                            System.out.println("Your order ID is: " + order.getID());

                            return CLIClientStates.MAIN_MENU;

                        } catch (RemoteException e) {
                            e.printStackTrace();
                            System.out.println("Failed to place order");
                            System.out.println("Please try again!");

                            return CLIClientStates.VIEW_CART;

                        }

                    }
                case "3":  // discard
                    client.cart = null;
                    return CLIClientStates.MAIN_MENU;
                default:
                    System.out.println("Invalid choice");
                    return this;
            }


        }
    },
    TRACK_ORDER {
        @Override
        public CLIClientStates updateState(CLIClient client, Scanner stdin) {

            System.out.println("Enter your order ID:");
            String orderID = stdin.next();

            try {

                Order order = client.connection.getOrderByID(orderID);

                if (order == null) {
                    System.out.println("No such order exists, ensure that your order ID is correct");
                } else {

                    printOrder(order, client.connection.getRestaurantByID(order.getRestaurantID()));

                }

            } catch (RemoteException e) {
                System.out.println("Error fetching your order");
                System.out.println("Please try again!");
            }

            return CLIClientStates.MAIN_MENU;

        }
    },
    MANAGE_RESTAURANT_MENU {
        @Override
        public CLIClientStates updateState(CLIClient client, Scanner stdin) {
            System.out.println("Restaurant Options");
            System.out.println("1) Make new restaurant");
            System.out.println("2) Manage existing restaurant");
            System.out.println("3) Back");

            String choice = stdin.next();

            switch (choice) {
                case "1":
                    return CLIClientStates.NEW_RESTAURANT;
                case "2":
                    return CLIClientStates.MANAGE_RESTAURANT_IN;
                case "3":
                    return CLIClientStates.MAIN_MENU;
                default:
                    System.out.println("Invalid Choice");
                    return this;
            }
        }
    },
    MANAGE_RESTAURANT_IN {
        @Override
        public CLIClientStates updateState(CLIClient client, Scanner stdin) {

            System.out.println("Enter your Restaurant ID:");
            String restaurantID = stdin.next();

            try {

                Restaurant restaurant = client.connection.getRestaurantByID(restaurantID);

                if (restaurant == null) {
                    System.out.println("No such restaurant exists, ensure that your order ID is correct");
                    return MAIN_MENU;
                } else {
                    client.currentRestaurant = restaurant;
                    return MANAGE_RESTAURANT;
                }
            } catch (Exception e) {
                return null;
            }
        }
    },
    MANAGE_RESTAURANT {
        @Override
        public CLIClientStates updateState(CLIClient client, Scanner stdin) {
            if (client.currentRestaurant == null) {
                return null;
            }

            System.out.println("Manage: " + client.currentRestaurant.getName());
            System.out.println("1) Process Orders");
            System.out.println("2) Manage Availability");
            System.out.println("3) Back");

            String choice = stdin.next();

            switch (choice) {
                case "1":
                    return CLIClientStates.MANAGE_RESTAURANT_PROCESS_ORDERS;
                case "2":
                    return CLIClientStates.MANAGE_RESTAURANT_AVAILABILITY;
                case "3":
                    return CLIClientStates.MAIN_MENU;
                default:
                    System.out.println("Invalid Choice");
                    return this;
            }
        }
    },
    MANAGE_RESTAURANT_PROCESS_ORDERS {
        @Override
        public CLIClientStates updateState(CLIClient client, Scanner stdin) {
            try {

                List<Order> allOrders = client.connection.getOrdersByRestaurantID(client.currentRestaurant.getID());
                Map<Order.OrderStatus, List<Order>> groupedOrders = allOrders.stream().collect(
                        Collectors.groupingBy(Order::getStatus)
                );

                System.out.println("Orders preview");
                groupedOrders.forEach((status, orders) -> System.out.println(status + ": " + orders.size()));

                groupedOrders.computeIfPresent(Order.OrderStatus.PENDING, (status, orders) -> {
                    System.out.println("Accept Order");
                    orders.forEach(order -> {

                        printOrder(order, client.currentRestaurant);

                        System.out.println("Options:");
                        System.out.println("1) Accept");
                        System.out.println("2) Reject");


                        choice:
                        while (true) {
                            String choice = stdin.next();

                            switch (choice) {
                                case "1":
                                    try {
                                        client.connection.updateOrder(order.getID(), Order.OrderStatus.COOKING);
                                        break choice;
                                    } catch (RemoteException e) {
                                        System.out.println("Failed to change status, try again");
                                    }
                                case "2":
                                    try {
                                        client.connection.updateOrder(order.getID(), Order.OrderStatus.REJECTED);
                                        break choice;
                                    } catch (RemoteException e) {
                                        System.out.println("Failed to change status, try again");
                                    }
                                default:
                                    System.out.println("Invalid Choice");
                                    break;
                            }
                        }

                    });
                    return orders;
                });

                groupedOrders.computeIfPresent(Order.OrderStatus.COOKING, (status, orders) -> {
                    System.out.println("Mark dispatched orders");
                    orders.forEach(order -> {

                        printOrder(order, client.currentRestaurant);

                        System.out.println("Has this order dispatched (y/n)?");


                        while (true) {
                            String choice = stdin.next();

                            if (choice.toLowerCase().equals("y")) {
                                try {
                                    client.connection.updateOrder(order.getID(), Order.OrderStatus.ON_THE_WAY);
                                    System.out.println("Order marked as on the way");
                                    break;
                                } catch (RemoteException e) {
                                    System.out.println("Failed to change status, try again later");
                                }
                            } else {
                                System.out.println("Order status not changed");
                                break;
                            }
                        }

                    });
                    return orders;
                });

                groupedOrders.computeIfPresent(Order.OrderStatus.COOKING, (status, orders) -> {
                    System.out.println("Mark delivered orders");
                    orders.forEach(order -> {

                        printOrder(order, client.currentRestaurant);

                        System.out.println("Has this order been delivered (y/n)?");

                        while (true) {
                            String choice = stdin.next();

                            if (choice.toLowerCase().equals("y")) {
                                try {
                                    client.connection.updateOrder(order.getID(), Order.OrderStatus.DELIVERED);
                                    System.out.println("Order marked as on the way");
                                    break;
                                } catch (RemoteException e) {
                                    System.out.println("Failed to change status, try again later");
                                }
                            } else {
                                System.out.println("Order status not changed");
                                break;
                            }
                        }

                    });
                    return orders;
                });

                System.out.println("Done, no more orders to process");

            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return MANAGE_RESTAURANT;
        }
    },
    MANAGE_RESTAURANT_AVAILABILITY {
        @Override
        public CLIClientStates updateState(CLIClient client, Scanner stdin) {

            while (true) {


                List<MenuItem> items = client.currentRestaurant.getMenuItems();
                System.out.println("Type the number of a menu item to toggle it's availability");
                System.out.println("0) Exit");
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

                String choice;
                try {
                    choice = stdin.next();
                } catch (Exception e) {
                    System.out.println("Invalid choice");
                    return this;
                }

                int chosen = Integer.parseInt(choice);

                if (chosen < 0 || chosen > items.size()) {
                    System.out.println("Invalid choice");
                    return this;
                }

                if (chosen == 0) {
                    return CLIClientStates.MANAGE_RESTAURANT;
                } else {
                    MenuItem item = items.get(chosen - 1);
                    item.setAvailable(!item.isAvailable());
                }
            }
        }
    },
    NEW_RESTAURANT {
        @Override
        public CLIClientStates updateState(CLIClient client, Scanner stdin) {
            System.out.println("Setting up a new Restaurant");

            System.out.println("Name:");
            System.out.flush();
            String name = stdin.next();

            int deliveryPrice;
            int priceRating;
            List<MenuItem> menuItems;
            try {
                if (name.length() < 4) {
                    System.out.println("The name must be at least 3 characters");
                    return this;
                }

                getAddress(stdin, client);

                System.out.println("Delivery Cost (in pennies):");
                deliveryPrice = stdin.nextInt();

                if (deliveryPrice < 0) {
                    System.out.println("Delivery price can't be negative");
                    return this;
                }

                System.out.println("Expensiveness rating (1-5):");
                priceRating = stdin.nextInt();

                if (priceRating < 0 | priceRating > 5) {
                    System.out.println("Expensiveness must be between 1-5");
                    return this;
                }

                menuItems = new ArrayList<>();

                while (true) {
                    System.out.println("Enter a new dish (ENTER to end):");
                    String itemName = stdin.next();
                    if (itemName.length() == 0) {
                        break;
                    }

                    System.out.println("Price (in pennies):");
                    int price = stdin.nextInt();

                    System.out.println("Is vegetarian (y/n):");
                    boolean isVeggie = stdin.next().equals("y");

                    menuItems.add(new MenuItem(itemName, true, price, isVeggie));
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input");
                return this;
            }

            try {
                Restaurant restaurant = client.connection.createRestaurant(
                        name,
                        client.address,
                        menuItems,
                        deliveryPrice,
                        priceRating
                );

                System.out.println("A new restaurant has successfully been created");
                System.out.println("The ID of your new restaurant is: " + restaurant.getID());
                client.currentRestaurant = restaurant;

                return MANAGE_RESTAURANT;
            } catch (RemoteException e) {
                e.printStackTrace();
                return MAIN_MENU;
            }

        }
    };

    public static void printOrder(Order order, Restaurant restaurant) {

        System.out.println("\nOrder id: " + order.getID());
        System.out.println("Items:");
        order.getItems().forEach((key, value) -> System.out.printf(
                "%dx £%.2f %s\n",
                value,
                key.getPrice() / 100f,
                key.getName()
        ));

        int total = order.getItems().entrySet().stream().map(entry ->
                entry.getKey().getPrice() * entry.getValue()
        ).reduce(Integer::sum).orElse(0);

        System.out.printf("Delivery Charge: £%.2f\n", restaurant.getDeliveryCost() / 100f);

        System.out.println("Status: " + order.getStatus().toString());
        System.out.printf("Total: £%.2f\n", total / 100f);

    }

    public static final String OFFICIAL_POSTCODE_REGEX = "^([Gg][Ii][Rr] 0[Aa]{2})|((([A-Za-z][0-9]{1,2})|(([A-Za-z][A-Ha-hJ-Yj-y][0-9]{1,2})|(([A-Za-z][0-9][A-Za-z])|([A-Za-z][A-Ha-hJ-Yj-y][0-9][A-Za-z]?))))\\s?[0-9][A-Za-z]{2})$";

    /**
     * Gets an address from standard in and writes it into the client
     *
     * @param stdin  standard in
     * @param client The client
     */
    public static void getAddress(@NotNull Scanner stdin, @NotNull CLIClient client) {

        String address;
        String doorNumber;
        boolean matches = false;

        while (client.address == null || !matches) {

            System.out.println("Enter your postcode: ");
            System.out.flush();

            address = stdin.next();

            matches = address.matches(OFFICIAL_POSTCODE_REGEX);

            if (!matches) {
                continue;
            }

            System.out.println("Enter your door number: ");
            System.out.flush();

            doorNumber = stdin.next();

            try {
                client.address = client.connection.getAddressFromPostcode(doorNumber, address);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

    }



    /**
     * This method is the transition between the states of the CLI interface
     *
     * @param client The current state of the client
     * @param stdin  Standard in scanner
     * @return The next state
     */
    protected abstract CLIClientStates updateState(CLIClient client, Scanner stdin);

}
