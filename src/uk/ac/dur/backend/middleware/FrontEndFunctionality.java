package uk.ac.dur.backend.middleware;

import java.rmi.Remote;

/**
 * This interface is all functionality that the front end (middleware) provides.
 *
 */
interface FrontEndFunctionality extends Remote, RemoteFunctionality, InternalFrontEndFunctionality {
}

