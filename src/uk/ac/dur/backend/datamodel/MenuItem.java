package uk.ac.dur.backend.datamodel;

import java.io.Serializable;

/**
 * This class is used to store restaurant menu items. All of it's properties except availability are immutable
 *
 * @see Restaurant
 */
public final class MenuItem implements Serializable {

    private static final long serialVersionUID = -2891060857054948287L;
    private final String name;
    private final int price;

    private boolean isAvailable;

    private final boolean isVegetarian;

    /**
     * Creates a new menu item
     *
     * @param name         The name
     * @param isAvailable  If the item is available
     * @param price        The price in pennies
     * @param isVegetarian If the item is vegetarian
     */
    public MenuItem(String name, boolean isAvailable, int price, boolean isVegetarian) {
        this.name = name;
        this.isAvailable = isAvailable;
        this.price = price;
        this.isVegetarian = isVegetarian;
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public int getPrice() {
        return price;
    }

    public boolean isVegetarian() {
        return isVegetarian;
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "name='" + name + '\'' +
                ", isAvailable=" + isAvailable +
                ", price=" + price +
                ", isVegetarian=" + isVegetarian +
                '}';
    }

}
