package uk.ac.dur.client;

import uk.ac.dur.backend.middleware.RemoteFunctionality;

import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * This class implements the methods necessary to communicate with the back end server
 */
public abstract class Client implements RemoteFunctionality {

    public static void main(String[] args) {

        try {

            Registry registry = LocateRegistry.getRegistry("localhost", 37001);
            // ideally this would use DNS to provide extra transparency.  If it didn't make the submission awkward I
            // would have done this.
            RemoteFunctionality stub = (RemoteFunctionality) registry.lookup("frontend");
            Client connection = new ConnectedClient(stub);
            CLIClient cliInterface = new CLIClient(connection);
            cliInterface.start();

        } catch (ConnectException e) {
            System.out.println("Couldn't connect to JustHungry, try again later");
        } catch (Exception e) {
            System.err.println("Client failed unexpectedly, try reopening JustHungry");
        }

    }

}
