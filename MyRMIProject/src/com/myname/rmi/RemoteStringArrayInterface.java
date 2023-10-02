package com.myname.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteStringArrayInterface extends Remote {

    void initializeArray(int n) throws RemoteException;
    int getArrayCapacity() throws RemoteException;
    void insertArrayElement(int l, String str) throws RemoteException;
    boolean requestReadLock(int l, int client_id) throws RemoteException;
    boolean requestWriteLock(int l, int client_id) throws RemoteException;
    void releaseLock(int l, int client_id) throws RemoteException;
    String fetchElementRead(int l, int client_id) throws RemoteException;
    String fetchElementWrite(int l, int client_id) throws RemoteException;
    boolean writeBackElement(String str, int l, int client_id) throws RemoteException;
    int getClientHoldingReadLock(int l) throws RemoteException;
    int getClientHoldingWriteLock(int l) throws RemoteException;
    void forceReleaseLock(int l, int client_id) throws RemoteException;

}
