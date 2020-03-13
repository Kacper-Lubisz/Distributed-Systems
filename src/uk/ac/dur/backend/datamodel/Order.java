package uk.ac.dur.backend.datamodel;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.ac.dur.Utils.generateID;

/**
 * This class is used to store an instance of an order, all but status is immutable
 */
public final class Order implements Serializable {

    private static final long serialVersionUID = 3792345103263993647L;

    /**
     * The states of an order
     */
    public enum OrderStatus {
        PENDING,
        REJECTED,
        COOKING,
        ON_THE_WAY,
        DELIVERED
    }

    private final String id;

    private final Map<MenuItem, Integer> items;
    private final String restaurantID;
    private OrderStatus status;

    /**
     * Creates a new order
     * @param items The map of item to amount for this order, if items are missing this means zero of that item
     * @param status The status to initialise with
     * @param restaurantID The id of the restaurant that this order is tied to
     */
    public Order(
            @NotNull Map<@NotNull MenuItem, @NotNull Integer> items,
            @NotNull OrderStatus status,
            @NotNull String restaurantID
    ) {
        id = generateID();

        this.items = items;
        this.status = status;
        this.restaurantID = restaurantID;
    }

    public String getID() {
        return id;
    }

    public Map<MenuItem, Integer> getItems() {
        return items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getRestaurantID() {
        return restaurantID;
    }

    /**
     * This method generates the test order set
     * @return The test set
     */
    @NotNull
    public static List<Order> getOrderTestSet() {
        return new ArrayList<>();
    }

}
