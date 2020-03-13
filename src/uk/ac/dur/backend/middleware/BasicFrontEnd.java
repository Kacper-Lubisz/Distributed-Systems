package uk.ac.dur.backend.middleware;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import uk.ac.dur.backend.datamodel.Address;
import uk.ac.dur.backend.datamodel.MenuItem;
import uk.ac.dur.backend.datamodel.Order;
import uk.ac.dur.backend.datamodel.Restaurant;
import uk.ac.dur.backend.middleware.replica.ReplicaServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is a basic implementation of the FrontEndFunctionality.
 *
 * @see FrontEndFunctionality
 */
public class BasicFrontEnd implements FrontEndFunctionality, Serializable {

    private static final long serialVersionUID = 4895177709554536838L;

    public static final String POSTCODE_API_URL = "http://api.postcodes.io/postcodes/";
    public static final String USER_AGENT = "JustHungry/1.0.0";

    private final List<ReplicaServer> replicas;
    private ReplicaServer master;

    BasicFrontEnd() {
        replicas = new ArrayList<>();

    }

    public static void main(String[] args) {
        System.out.println("Starting Front End Server");
        BasicFrontEnd server = new BasicFrontEnd();

        try {
            Registry registry = LocateRegistry.createRegistry(37001);

            Remote stub = UnicastRemoteObject.exportObject(server, 0);

            registry.bind("frontend", stub);

            System.out.println("Front End Server Running");

        } catch (RemoteException | AlreadyBoundException e) {
            System.err.println("Critical failure occurred");
            e.printStackTrace();
        }

    }

    @Override
    public void registerReplica(ReplicaServer newReplica, String id) throws RemoteException {
        synchronized (this) {
            replicas.add(newReplica);
            System.out.println("New replica connected, id:" + id);

            if (replicas.size() == 1) {
                promoteToMaster(newReplica);
                newReplica.updateState(getDefaultState());
            } else {
                try {
                    master.addSlaveReplica(newReplica);
                } catch (RemoteException | NullPointerException e) {
                    findNewMaster();
                }
            }
        }

    }

    @Contract(" -> new")
    private static @NotNull RestaurantServerState getDefaultState() {
        return new RestaurantServerState(
                Restaurant.getRestaurantTestSet(),
                Order.getOrderTestSet()
        );
    }

    /**
     * This method attempts to promote a replica to master.
     *
     * @param master the replica which will become master
     * @throws RemoteException if this replica can't become a master
     */
    private void promoteToMaster(@NotNull ReplicaServer master) throws RemoteException {
        this.master = master;

        List<ReplicaServer> newSlaves = this.replicas.stream().filter(replica ->
                replica != this.master
        ).collect(Collectors.toList());

        master.promoteToMaster(newSlaves);
    }

    /**
     * This method searches the connected replicas to find a new master.
     * It starts by pinging all replicas to remove all ones which are not responding, it then chooses the first one to
     * become the master.
     *
     * @return The new master
     * @throws RemoteException if a master can't be found
     */
    private ReplicaServer findNewMaster() throws RemoteException {
        synchronized (this) {
            System.out.println("Looking for a new master");

            master = null;

            List<ReplicaServer> newReplicas = replicas.stream().filter(replica -> {
                try {
                    // this ensures that all replicas are still alive. this costs one 'network delay' but prevents chaining
                    // of delays when several replicas go down between
                    replica.ping();
                    return true;
                } catch (RemoteException e) {
                    return false;
                }
            }).dropWhile((possibleMaster) -> {
                try {
                    this.promoteToMaster(possibleMaster);
                    return false;
                } catch (RemoteException e) {
                    // if a replica can't be promoted then it must have a critical failure and be removed
                    return true;
                }
            }).collect(Collectors.toList());

            // here the middleware should spin up more replicas, if this were deployed in a containerised fashion, then this
            // would be more feasible right now
            // As a work around I could do something like, but that wouldn't be a nice solution
            // Runtime.getRuntime().exec("./spinUpReplica.sh")

            if (newReplicas.size() == 0) {
                throw new RemoteException("Request Failed, no replicas available");
            } else {
                return master;
            }
        }
    }

    @FunctionalInterface
    interface ThrowingFunction<A, B, E extends Throwable> {
        B apply(A argument) throws E;
    }

    /**
     * This method is used run a function on an unknown master.  If a mater doesn't exist or fails, then a new
     * master is found and used
     *
     * @param function The method which is called on the new master
     * @param <T>      The Return type of the method function
     * @return The result of the function
     * @throws RemoteException if a new master can't be found
     */
    private <T> T performOnMaster(ThrowingFunction<ReplicaServer, T, RemoteException> function) throws RemoteException {
        synchronized (this) { // to ensure there are no issues with concurrency

            while (master == null) {
                master = findNewMaster();
                // if this is not possible then a remote error is thrown
            }

            while (replicas.size() != 0) {
                try {
                    return function.apply(master);
                } catch (RemoteException e) {
                    System.out.println("Master failed");

                    // if the current master can't fulfill the request then a search for a new master continues
                    master = null;
                    master = findNewMaster();
                }
            }

            throw new RemoteException("System Error");
            // todo start new replicas

        }
    }

    @Override
    public List<Restaurant> getRestaurants(Address address, int number) throws RemoteException {
        return performOnMaster(master -> master.getRestaurants(address, number));
    }


    @Override
    public Restaurant getRestaurantByID(String id) throws RemoteException {
        // these read only methods could be performed on slaves,
        // this method could be replaced with a, 'perform on any' method
        return performOnMaster(master -> master.getRestaurantByID(id));
    }

    @Override
    public Order getOrderByID(String id) throws RemoteException {
        return performOnMaster(master -> master.getOrderByID(id));
    }

    @Override
    public Order createOrder(Map<MenuItem, Integer> items, String restaurantID) throws RemoteException {
        return performOnMaster(master -> master.createOrder(items, restaurantID));
    }

    @Override
    public List<Order> getOrdersByRestaurantID(String id) throws RemoteException {
        return performOnMaster(master -> master.getOrdersByRestaurantID(id));
    }

    @Override
    public Restaurant createRestaurant(String name, Address address, List<MenuItem> menuItems, int deliveryCost, int priceRating) throws RemoteException {
        return performOnMaster(master -> master.createRestaurant(name, address, menuItems, deliveryCost, priceRating));
    }

    @Override
    public Order updateOrder(String orderID, Order.OrderStatus status) throws RemoteException {
        return performOnMaster(master -> master.updateOrder(orderID, status));
    }

    @Override
    public Address getAddressFromPostcode(String doorNumber, String postcode) throws RemoteException {
        try {

            URL url = new URL(POSTCODE_API_URL + postcode);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", USER_AGENT);

            InputStream inputStream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);

            JSONObject responseBody = (JSONObject) JSONValue.parseWithException(reader);

            JSONObject results = (JSONObject) responseBody.get("result");

            if ((long) responseBody.get("status") != 200) {
                return null;
            } else {

                double longitude = (double) results.get("longitude");
                double latitude = (double) results.get("latitude");

                return new Address(doorNumber, postcode, longitude, latitude);

            }
        } catch (IOException | ParseException | NullPointerException e) {
            throw new RemoteException("Failed to get post code information");
        }

    }

}
