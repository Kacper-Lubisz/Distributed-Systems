package uk.ac.dur.client;

import uk.ac.dur.backend.datamodel.Address;
import uk.ac.dur.backend.datamodel.MenuItem;
import uk.ac.dur.backend.datamodel.Order;
import uk.ac.dur.backend.datamodel.Restaurant;
import uk.ac.dur.backend.middleware.RemoteFunctionality;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

final class ConnectedClient extends Client {

    private final RemoteFunctionality remoteStub;

    ConnectedClient(RemoteFunctionality remoteStub) {
        this.remoteStub = remoteStub;
    }

    @Override
    public List<Restaurant> getRestaurants(Address address, int number) throws RemoteException {
        return remoteStub.getRestaurants(address, number);
    }

    @Override
    public Restaurant getRestaurantByID(String id) throws RemoteException {
        return remoteStub.getRestaurantByID(id);
    }

    @Override
    public Restaurant createRestaurant(String name, Address address, List<MenuItem> menuItems, int deliveryCost, int priceRating) throws RemoteException {
        return remoteStub.createRestaurant(name, address, menuItems, deliveryCost, priceRating);
    }

    @Override
    public Order getOrderByID(String id) throws RemoteException {
        return remoteStub.getOrderByID(id);
    }

    @Override
    public Order createOrder(Map<MenuItem, Integer> items, String restaurantID) throws RemoteException {
        return remoteStub.createOrder(items, restaurantID);
    }

    @Override
    public List<Order> getOrdersByRestaurantID(String id) throws RemoteException {
        return remoteStub.getOrdersByRestaurantID(id);
    }

    @Override
    public Order updateOrder(String orderID, Order.OrderStatus status) throws RemoteException {
        return remoteStub.updateOrder(orderID, status);

    }

    @Override
    public Address getAddressFromPostcode(String doorNumber, String postcode) throws RemoteException {
        return remoteStub.getAddressFromPostcode(doorNumber, postcode);
    }
}
