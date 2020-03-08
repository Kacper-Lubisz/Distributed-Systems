package uk.ac.dur.datamodel;

import java.util.Objects;

public class MenuItem {
    private String name;
    private boolean isAvailable;
    private int calories;
    private int price;
    private boolean isVegetarian;

    public MenuItem(String name, boolean isAvailable, int calories, int price) {
        this.name = name;
        this.isAvailable = isAvailable;
        this.calories = calories;
        this.price = price;
        this.isVegetarian = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "name='" + name + '\'' +
                ", isAvailable=" + isAvailable +
                ", calories=" + calories +
                ", price=" + price +
                ", isVegetarian=" + isVegetarian +
                '}';
    }

    public boolean isVegetarian() {
        return isVegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        isVegetarian = vegetarian;
    }
}
