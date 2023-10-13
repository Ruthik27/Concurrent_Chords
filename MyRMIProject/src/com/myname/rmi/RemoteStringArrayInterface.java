package com.myname.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteStringArrayInterface extends Remote {
    String registerUser() throws RemoteException;

    // Used for returning the capacity of the string array
    int getArrayCapacity() throws RemoteException;

    // Used for inserting a string at the specified index
    void insertArrayElement(int index, String str) throws RemoteException;

    // Used for requesting a read lock on the specified element for a client
    boolean requestReadLock(int l, int client_id) throws RemoteException;

    // Used for requesting a write lock on the specified element for a client
    boolean requestWriteLock(int l, int client_id) throws RemoteException;

    // Used for releasing any lock on the specified element for a client
    void releaseLock(int l, int client_id) throws RemoteException;
    boolean isLockAvailable(int index, int clientId) throws RemoteException;


    // Used for fetching the specified element in read-only mode
    String fetchElementRead(int l, int client_id) throws RemoteException;

    // Used for fetching the specified element in read/write mode
    String fetchElementWrite(int l, int client_id) throws RemoteException;

    // Used for writing a string back to the specified index if the client has a write lock
    boolean writeBackElement(String str, int l, int client_id) throws RemoteException;
}
