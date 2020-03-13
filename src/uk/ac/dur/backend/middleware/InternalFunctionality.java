package uk.ac.dur.backend.middleware;

import uk.ac.dur.backend.datamodel.Address;
import uk.ac.dur.backend.datamodel.MenuItem;
import uk.ac.dur.backend.datamodel.Order;
import uk.ac.dur.backend.datamodel.Restaurant;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * This interface is the external functionality which is provided by the replica server
 */
public interface InternalFunctionality {
    /**
     * Returns the restaurants which are the closes (by straight line distance) to the specified address
     *
     * @param address The address to find distance to
     * @param number  The number of restaurants which will be returned
     * @return The list of restaurants
     * @throws RemoteException if there is a failure in this request
     */
    List<Restaurant> getRestaurants(Address address, int number) throws RemoteException;

    /**
     * Finds a specific restaurant by it's id, null if none exist
     *
     * @param id The id of the restaurant to search for
     * @return The restaurant, or null if not found
     * @throws RemoteException if there is a failure in this request
     */
    Restaurant getRestaurantByID(String id) throws RemoteException;

    /**
     * Creates a new restaurant
     *
     * @param name         The name of the new restaurant
     * @param address      The address
     * @param menuItems    The menu
     * @param deliveryCost The delivery cost
     * @param priceRating  The price rating
     * @return The new restaurant that was created
     * @throws RemoteException if there is a failure in this request
     * @see Restaurant
     */
    Restaurant createRestaurant(
            String name,
            Address address,
            List<MenuItem> menuItems,
            int deliveryCost,
            int priceRating
    ) throws RemoteException;

    /**
     * Finds a specific order by it's id, null if none exist
     *
     * @param id The id of the order to search for
     * @return The order, or null if not found
     * @throws RemoteException if there is a failure in this request
     */
    Order getOrderByID(String id) throws RemoteException;

    /**
     * Creates a new order
     *
     * @param items        The map of items in this order
     * @param restaurantID The id of the restaurant for which this order is for
     * @return the new order of null if the request is invalid
     * @throws RemoteException if there is a failure in this request
     * @see Order
     */
    Order createOrder(Map<MenuItem, Integer> items, String restaurantID) throws RemoteException;

    /**
     * Returns the list of orders by the id of the restaurant that they are for
     *
     * @param id the id of the restaurant
     * @return the list of orders
     * @throws RemoteException if there is a failure in this request
     */
    List<Order> getOrdersByRestaurantID(String id) throws RemoteException;

    /**r
     * Sets the status of an order
     * @param orderID the id of the order in question
     * @param status the new status
     * @return The order which was updated
     * @throws RemoteException if there is a failure in this request
     */
    Order updateOrder(String orderID, Order.OrderStatus status) throws RemoteException;

}
