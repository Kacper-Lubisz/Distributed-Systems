package uk.ac.dur.datamodel;

import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    private long id;

    private String name;

    private String address;
    private double longitude;
    private double latitude;

    private int price;

    private List<MenuItem> menuItems;

    private double deliveryCost;

    public Restaurant(String name, String address, double longitude, double latitude, List<MenuItem> menuItems) {
        this.name = name;
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.menuItems = menuItems;

        deliveryCost = 150;

        price = 2;

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    public int getDeliveryTime() {
        return 10;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "name='" + name + '\'' +
                '}';
    }
}
