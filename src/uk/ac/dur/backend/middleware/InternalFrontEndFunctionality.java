package uk.ac.dur.backend.middleware;

import uk.ac.dur.backend.middleware.replica.ReplicaServer;

import java.rmi.RemoteException;

/**
 * This is the functionality that the front end provides which is only visible to replica servers
 * @see FrontEndFunctionality
 */
interface InternalFrontEndFunctionality {
    void registerReplica(ReplicaServer replicaStub, String id) throws RemoteException;

}
