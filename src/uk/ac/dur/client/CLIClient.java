package uk.ac.dur.client;

import uk.ac.dur.backend.datamodel.Address;
import uk.ac.dur.backend.datamodel.MenuItem;
import uk.ac.dur.backend.datamodel.Restaurant;

import java.util.Map;
import java.util.Scanner;

public class CLIClient {

    protected Client connection;
    protected CLIClientStates state;
    protected Scanner stdin_scanner;

    protected Address address;

    protected Restaurant currentRestaurant;
    protected Map<MenuItem, Integer> cart;

    public CLIClient(Client client) {
        this.connection = client;
        this.state = CLIClientStates.MAIN_MENU;

        stdin_scanner = new Scanner(System.in).useDelimiter("\n");
    }

    /**
     * Runs the state transitions of the user interface until they become null
     */
    void start() {
        while (state != null) {
            state = state.updateState(this, this.stdin_scanner);
        }
    }
}
