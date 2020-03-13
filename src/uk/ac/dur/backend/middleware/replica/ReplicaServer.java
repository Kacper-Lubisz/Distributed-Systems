package uk.ac.dur.backend.middleware.replica;

import uk.ac.dur.backend.middleware.InternalFunctionality;
import uk.ac.dur.backend.middleware.RestaurantServerState;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * This is the interface which describes what methods can be called on a replica server
 */
public interface ReplicaServer extends Remote, InternalFunctionality {

    String getID() throws RemoteException;

    /**
     * This method makes this replica a master
     *
     * @param slaves The slaves which will receive state updates from this replica
     */
    void promoteToMaster(List<ReplicaServer> slaves) throws RemoteException;

    /**
     * Adds a slave to the master
     *
     * @param slave The new slave
     * @throws RemoteException thrown if the slave can't accept the new state
     */
    void addSlaveReplica(ReplicaServer slave) throws RemoteException;

    /**
     * This sets the state of this replica
     *
     * @param state The new state
     */
    void updateState(RestaurantServerState state) throws RemoteException;

    /**
     * This method does nothing and is only used to see if communication with a remote object still holds
     *
     * @throws RemoteException if the remote object is disconnected
     */
    void ping() throws RemoteException;

}
