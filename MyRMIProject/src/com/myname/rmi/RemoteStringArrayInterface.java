package com.myname.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteStringArrayInterface extends Remote {

    // Returns the capacity of the string array
    int getArrayCapacity() throws RemoteException;

    // Inserts a string at the specified index
    void insertArrayElement(int index, String str) throws RemoteException;

    // Requests a read lock on the specified element for a client
    boolean requestReadLock(int index, int client_id) throws RemoteException;

    // Requests a write lock on the specified element for a client
    boolean requestWriteLock(int index, int client_id) throws RemoteException;

    // Releases any lock on the specified element for a client
    void releaseLock(int index, int client_id) throws RemoteException;

    // Fetches the specified element in read-only mode
    String fetchElementRead(int index, int client_id) throws RemoteException;

    // Fetches the specified element in read/write mode
    String fetchElementWrite(int index, int client_id) throws RemoteException;

    // Writes a string back to the specified index if the client has a write lock
    boolean writeBackElement(String str, int index, int client_id) throws RemoteException;
}
