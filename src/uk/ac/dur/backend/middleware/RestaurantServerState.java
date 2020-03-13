package uk.ac.dur.backend.middleware;

import uk.ac.dur.backend.datamodel.Order;
import uk.ac.dur.backend.datamodel.Restaurant;
import uk.ac.dur.backend.middleware.replica.BasicReplicaServer;
import uk.ac.dur.backend.middleware.replica.ReplicaServer;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * This class is used to store the state of the replica servers.
 */
public final class RestaurantServerState implements Serializable {

    private static final long serialVersionUID = 8165544384244637664L;
    final List<Restaurant> restaurants;
    final List<Order> orders;

    public RestaurantServerState(List<Restaurant> restaurants, List<Order> orders) {
        this.restaurants = restaurants;
        this.orders = orders;
    }

    public List<Restaurant> getRestaurants() {
        return restaurants;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public static void main(String[] args) {
        try {

            Registry registry = LocateRegistry.getRegistry("localhost", 37001);
            InternalFrontEndFunctionality middlewareStub = (InternalFrontEndFunctionality) registry.lookup("frontend");

            double errorRate = 0;
            if (args.length == 2) {
                try {
                    errorRate = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid error rate argument");
                    System.exit(1);
                }
            } else {
                errorRate = 0.0;
            }


            ReplicaServer replica = new BasicReplicaServer(errorRate);
            ReplicaServer replicaStub = (ReplicaServer) UnicastRemoteObject.exportObject(replica, 0);

            System.out.println("Starting new replica server, with error rate:" + errorRate);

            middlewareStub.registerReplica(replicaStub, replica.getID());

            System.out.println("Successfully started replica server");

        } catch (RemoteException | NotBoundException e) {
            System.err.println("Failed to start replica server");
            e.printStackTrace(); // not a user facing error, trace should be shown rather than a message
        }

    }


}
