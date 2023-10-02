package com.myname.rmi;

import java.util.Map;
import java.util.HashMap;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RemoteStringArray extends UnicastRemoteObject implements RemoteStringArrayInterface {

    private String[] array;
    private ReentrantReadWriteLock[] locks;
    private Map<Integer, Integer> readLockOwners = new HashMap<>();
    private Map<Integer, Integer> writeLockOwners = new HashMap<>();

    public RemoteStringArray() throws RemoteException {
        super();
    }

    @Override
    public void initializeArray(int n) throws RemoteException {
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
    public void insertArrayElement(int l, String str) throws RemoteException {
        if (l >= 0 && l < array.length) {
            array[l] = str;
        } else {
            throw new RemoteException("Index out of bounds");
        }
    }

    @Override
    public boolean requestReadLock(int l, int client_id) throws RemoteException {
        if (l >= 0 && l < array.length) {
            if (writeLockOwners.containsKey(l) && writeLockOwners.get(l) != client_id) {
                return false; // Another client holds the write lock.
            }
            if (locks[l].readLock().tryLock()) {
                readLockOwners.put(l, client_id);
                return true;
            }
        }
        throw new RemoteException("Index out of bounds");
    }

    @Override
    public boolean requestWriteLock(int l, int client_id) throws RemoteException {
        if (l >= 0 && l < array.length) {
            if ((readLockOwners.containsKey(l) && readLockOwners.get(l) != client_id) ||
                    (writeLockOwners.containsKey(l) && writeLockOwners.get(l) != client_id)) {
                return false; // Another client holds a lock.
            }
            if (locks[l].writeLock().tryLock()) {
                writeLockOwners.put(l, client_id);
                return true;
            }
        }
        throw new RemoteException("Index out of bounds");
    }


    @Override
    public void releaseLock(int l, int client_id) throws RemoteException {
        if (l >= 0 && l < array.length) {
            if (locks[l].isWriteLockedByCurrentThread() && writeLockOwners.get(l) == client_id) {
                locks[l].writeLock().unlock();
                writeLockOwners.remove(l);
            } else if (locks[l].readLock().tryLock() && readLockOwners.get(l) == client_id) {
                locks[l].readLock().unlock();
                readLockOwners.remove(l);
            } else {
                throw new RemoteException("Client " + client_id + " does not hold the lock for index " + l);
            }
        } else {
            throw new RemoteException("Index out of bounds");
        }
    }


    @Override
    public String fetchElementRead(int l, int client_id) throws RemoteException {
        if (l >= 0 && l < array.length) {
            if (array[l] == null) {
                throw new RemoteException("Element at index " + l + " doesn't exist.");
            }
            if (requestReadLock(l, client_id)) {
                return array[l];
            } else {
                return null; // or throw an exception indicating that the lock couldn't be acquired
            }
        }
        throw new RemoteException("Index out of bounds");
    }

    @Override
    public String fetchElementWrite(int l, int client_id) throws RemoteException {
        if (l >= 0 && l < array.length) {
            if (requestWriteLock(l, client_id)) {
                return array[l];
            } else {
                return null; // or throw an exception
            }
        }
        throw new RemoteException("Index out of bounds");
    }

    @Override
    public boolean writeBackElement(String str, int l, int client_id) throws RemoteException {
        if (l >= 0 && l < array.length) {
            if (locks[l].isWriteLockedByCurrentThread()) {
                array[l] = str;
                return true;
            } else {
                return false;
            }
        }
        throw new RemoteException("Index out of bounds");
    }

    @Override
    public int getClientHoldingReadLock(int l) throws RemoteException {
        if (l >= 0 && l < array.length) {
            return readLockOwners.getOrDefault(l, -1); // Returns -1 if no client holds the lock
        }
        throw new RemoteException("Index out of bounds");
    }

    @Override
    public int getClientHoldingWriteLock(int l) throws RemoteException {
        if (l >= 0 && l < array.length) {
            return writeLockOwners.getOrDefault(l, -1); // Returns -1 if no client holds the lock
        }
        throw new RemoteException("Index out of bounds");
    }

    @Override
    public void forceReleaseLock(int l, int client_id) throws RemoteException {
        if (l >= 0 && l < array.length) {
            if (writeLockOwners.get(l) == client_id) {
                locks[l].writeLock().unlock();
                writeLockOwners.remove(l);
            } else if (readLockOwners.get(l) == client_id) {
                locks[l].readLock().unlock();
                readLockOwners.remove(l);
            } else {
                throw new RemoteException("Client " + client_id + " does not hold a lock on element at index " + l);
            }
        } else {
            throw new RemoteException("Index out of bounds");
        }
    }

}
