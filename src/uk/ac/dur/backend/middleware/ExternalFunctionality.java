package uk.ac.dur.backend.middleware;

import uk.ac.dur.backend.datamodel.Address;

import java.rmi.RemoteException;

/**
 * This interface is the functionality that the front end provides which isn't provided by the replica servers
 *
 * @see FrontEndFunctionality
 */
public interface ExternalFunctionality {
    Address getAddressFromPostcode(String doorNumber, String postcode) throws RemoteException;
}
