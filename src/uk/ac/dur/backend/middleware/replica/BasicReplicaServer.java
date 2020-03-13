package uk.ac.dur.backend.middleware.replica;

import org.jetbrains.annotations.NotNull;
import uk.ac.dur.backend.datamodel.Address;
import uk.ac.dur.backend.datamodel.MenuItem;
import uk.ac.dur.backend.datamodel.Order;
import uk.ac.dur.backend.datamodel.Restaurant;
import uk.ac.dur.backend.middleware.RestaurantServerState;

import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static uk.ac.dur.Utils.generateID;

/**
 * This a basic implementation of ReplicaServer.
 *
 * @see ReplicaServer
 */
public class BasicReplicaServer implements ReplicaServer {
    // Ideally, this class would be implemented with a generic state type, but this adds a lot of necessary complexity
    // for the sake of the assignment

    private final String id;


    private ReplicaServer master;
    private List<ReplicaServer> slaves;

    private RestaurantServerState state;


    private final double errorRate; // exists for the sake of the assignment
    private final Random random;

    /**
     * Creates a BasicReplicaServer which has the probability of throwing an error on any request based on the
     * probability
     *
     * @param errorRate The probability of error
     */
    public BasicReplicaServer(double errorRate) {
        id = generateID();
        this.errorRate = errorRate;
        this.random = new Random();
    }

    public String getID() {
        return id;
    }

    private void attemptError() throws RemoteException {
        if (random.nextDouble() < this.errorRate) {
            RemoteException testingException = new RemoteException("Randomly thrown error thrown");
            System.out.println("Throwing random exception");
            throw testingException;
        }
    }


    @Override
    public void promoteToMaster(List<ReplicaServer> slaves) throws RemoteException {
        System.out.println("Server is now master");
        this.master = null;
        this.slaves = slaves;
    }

    @Override
    public void addSlaveReplica(ReplicaServer slave) throws RemoteException {

        // without much more effort a hierarchical model of state propagation could be implemented if this check
        // were removed
        if (master != null) {
            throw new RemoteException("Can't add a slave to a replica which isn't master");
        }
        slaves.add(slave);
        slave.updateState(state);
    }

    @Override
    public void updateState(RestaurantServerState state) throws RemoteException {
        System.out.println("Received state update");
        this.state = state;
    }

    /**
     * Class updateState on all slaves, if not possible the slave is removed
     */
    private void propagateState() throws RemoteException {
        try {
            attemptError();
            this.slaves = this.slaves.parallelStream().filter(slave -> {
                try {
                    slave.updateState(this.state);
                    return true;
                } catch (RemoteException e) {
                    return false; // the slave is removed
                }
            }).collect(Collectors.toList());
        } catch (RemoteException e) {
            errorRoutine(e);
        }
    }

    @Override
    public List<Restaurant> getRestaurants(Address address, int number) throws RemoteException {
        try {
            System.out.println("Handling getRestaurants");
            attemptError();

            return state.getRestaurants().stream().sorted(Comparator.comparingDouble(restaurant ->
                    restaurant.getAddress().getStraightLineDistance(address)
            )).limit(number).collect(Collectors.toList());
        } catch (RemoteException e) {
            errorRoutine(e);
            return null;
        }
    }

    @Override
    public Restaurant getRestaurantByID(String id) throws RemoteException {
        try {
            System.out.println("Handling getRestaurantByID");
            attemptError();

            return state.getRestaurants().stream().filter(restaurant -> restaurant.getID().equals(id)).findAny().orElse(null);
        } catch (RemoteException e) {
            errorRoutine(e);
            return null;
        }
    }

    @Override
    public Restaurant createRestaurant(
            String name,
            Address address,
            List<MenuItem> menuItems,
            int deliveryCost,
            int priceRating
    ) throws RemoteException {
        try {
            System.out.println("Handling createRestaurant");
            attemptError();

            Restaurant restaurant = new Restaurant(name, address, menuItems, deliveryCost, priceRating);
            state.getRestaurants().add(restaurant);
            propagateState();
            return restaurant;
        } catch (RemoteException e) {
            errorRoutine(e);
            return null;
        }
    }

    @Override
    public Order getOrderByID(String id) throws RemoteException {
        try {
            System.out.println("Handling getOrderByID");
            attemptError();

            return state.getOrders().stream().filter(order -> order.getID().equals(id)).findAny().orElse(null);
        } catch (RemoteException e) {
            errorRoutine(e);
            return null;
        }
    }

    @Override
    public Order createOrder(Map<MenuItem, Integer> items, String restaurantID) throws RemoteException {
        try {
            System.out.println("Handling createOrder");
            attemptError();

            Order order = new Order(items, Order.OrderStatus.PENDING, restaurantID);
            state.getOrders().add(order);
            propagateState();

            return order;
        } catch (RemoteException e) {
            errorRoutine(e);
            return null;
        }
    }

    @Override
    public List<Order> getOrdersByRestaurantID(String id) throws RemoteException {
        try {
            System.out.println("Handling getOrdersByRestaurantID");
            attemptError();

            return state.getOrders().stream().filter(order -> order.getRestaurantID().equals(id)).collect(Collectors.toList());
        } catch (RemoteException e) {
            errorRoutine(e);
            return null;
        }
    }

    @Override
    public Order updateOrder(String orderID, Order.OrderStatus status) throws RemoteException {
        try {
            System.out.println("Handling updateOrder");
            attemptError();

            Order order = getOrderByID(orderID);
            if (order == null) {
                return null;
            } else {
                order.setStatus(status);
                propagateState();
                return order;
            }
        } catch (RemoteException e) {
            errorRoutine(e);
            return null;
        }
    }

    @Override
    public void ping() throws RemoteException {

        try {
            attemptError();
        } catch (RemoteException e) {
            errorRoutine(e);
        }
    }

    /**
     * This method handles the termination of the replica when an exception is thrown occurs
     *
     * @param e The exception
     */
    public void errorRoutine(@NotNull RemoteException e) {
        System.err.println("System encountered an error, terminating replica");
        System.err.println(e.getMessage());
        System.exit(1);
    }

}