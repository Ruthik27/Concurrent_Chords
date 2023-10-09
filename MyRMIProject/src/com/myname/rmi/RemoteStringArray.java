package com.myname.rmi;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RemoteStringArray extends UnicastRemoteObject implements RemoteStringArrayInterface {
    private String[] array;
    private ReentrantReadWriteLock[] locks;

    public RemoteStringArray(int n) throws RemoteException {
        array = new String[n];
        locks = new ReentrantReadWriteLock[n];
        for (int i = 0; i < n; i++) {
            locks[i] = new ReentrantReadWriteLock();
        }
    }

    @Override
    public int getArrayCapacity() throws RemoteException {
        return array.length;
    }

    @Override
    public void insertArrayElement(int index, String str) throws RemoteException {
        if (index < array.length) {
            array[index] = str;
        }
    }

    @Override
    public boolean requestReadLock(int index, int client_id) throws RemoteException {
        return locks[index].readLock().tryLock();
    }

    @Override
    public boolean requestWriteLock(int index, int client_id) throws RemoteException {
        return locks[index].writeLock().tryLock();
    }

    @Override
    public void releaseLock(int index, int client_id) throws RemoteException {
        if (locks[index].isWriteLockedByCurrentThread()) {
            locks[index].writeLock().unlock();
        } else {
            locks[index].readLock().unlock();
        }
    }

    @Override
    public String fetchElementRead(int index, int client_id) throws RemoteException {
        if (locks[index].readLock().tryLock()) {
            return array[index];
        }
        return null; // or throw an exception based on your design decision
    }

    @Override
    public String fetchElementWrite(int index, int client_id) throws RemoteException {
        if (locks[index].writeLock().tryLock()) {
            return array[index];
        }
        return null; // or throw an exception based on your design decision
    }

    @Override
    public boolean writeBackElement(String str, int index, int client_id) throws RemoteException {
        if (locks[index].isWriteLockedByCurrentThread()) {
            array[index] = str;
            return true;
        }
        return false;
    }
}
