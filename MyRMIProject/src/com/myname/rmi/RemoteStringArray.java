package com.myname.rmi;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.logging.Logger;



public class RemoteStringArray extends UnicastRemoteObject implements RemoteStringArrayInterface {
    private static final Logger LOGGER = Logger.getLogger(RemoteStringArray.class.getName());
    private HashMap<String, Integer> userToClientId;  // Maps username to client_id
    private AtomicInteger nextClientId;  // Auto-incremented client_id
    private Map<Integer, Integer> lockOwnersByIndex;
    private String[] array;
    private ReentrantReadWriteLock[] locks;  // Use ReentrantReadWriteLock for locking
    private HashMap<Integer, Integer> lockOwners;  // Maps array index to client_id

    public RemoteStringArray(int n) throws RemoteException {
        array = new String[n];
        locks = new ReentrantReadWriteLock[n];  // Initialize the locks array
        lockOwners = new HashMap<>();
        userToClientId = new HashMap<>();
        nextClientId = new AtomicInteger(1);
        lockOwnersByIndex = new HashMap<>();

        for (int i = 0; i < n; i++) {
            locks[i] = new ReentrantReadWriteLock();  // Initialize with ReentrantReadWriteLock
        }
    }

    @Override
    public boolean isLockAvailable(int index, int clientId) {
        ReentrantReadWriteLock lock = locks[index];
        synchronized (lock) {
            boolean isAvailable = !lockOwners.containsKey(index) || lockOwners.get(index) == clientId;
            LOGGER.info("Lock at index " + index + " is " + (isAvailable ? "available" : "owned by Client " + lockOwners.get(index)));
            return isAvailable;
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
    public synchronized String registerUser() throws RemoteException {
        String generatedUsername = "User_" + (userToClientId.size() + 1);
        int clientId = nextClientId.getAndIncrement();
        userToClientId.put(generatedUsername, clientId);
        return generatedUsername + "_" + clientId;
    }
    @Override
    public boolean requestReadLock(int index, int client_id) throws RemoteException {
        ReentrantReadWriteLock lock = locks[index];

        synchronized (lock) {
            if (!lock.isWriteLocked() || lockOwners.get(index) == client_id) {
                lock.readLock().lock();  // Acquire a read lock
                lockOwners.put(index, client_id);  // Grant the read lock
                lockOwnersByIndex.put(index, client_id);  // Consistently track the lock owner for this index
                System.out.println("Read lock acquired by Client " + client_id + " at index " + index);
                LOGGER.info("Read lock granted to Client " + client_id + " at index " + index);
                return true;
            } else {
                LOGGER.warning("Failed to grant read lock to Client " + client_id + " at index " + index + " as it's owned by Client " + lockOwners.get(index));
                System.out.println("Lock at index " + index + " is owned by another client.");
                System.out.println("Lock at index " + index + " is owned by Client " + lockOwners.get(index));
                return false;  // Lock is owned by another client.
            }
        }
    }



    @Override
    public boolean releaseLock(int index, int clientId) throws RemoteException {
        ReentrantReadWriteLock lock = locks[index];

        synchronized (lock) {
            Integer currentLockOwner = lockOwners.get(index);
            if (currentLockOwner != null && currentLockOwner == clientId) {
                lockOwners.remove(index);  // Remove the lock ownership tracking for this index
                lockOwnersByIndex.remove(index);
                if (lock.isWriteLockedByCurrentThread()) {
                    lock.writeLock().unlock();  // Release the write lock
                    System.out.println("Write lock released by Client " + clientId + " at index " + index);
                } else if (lock.getReadHoldCount() > 0) {
                    lock.readLock().unlock();  // Release the read lock
                    System.out.println("Read lock released by Client " + clientId + " at index " + index);
                } else {
                    System.out.println("Error: Lock at index " + index + " wasn't held by Client " + clientId);
                    return false; // Lock wasn't held by the client
                }
                return true;  // Successful release
            } else {
                System.out.println("Error: You either didn't have the lock or the resource was already unlocked.");
                return false; // Lock wasn't held by the client or was already unlocked
            }
        }
    }



    @Override
    public String fetchElementWrite(int index, int clientId) throws RemoteException {
        ReentrantReadWriteLock lock = locks[index];

        synchronized (lock) {
            Integer currentLockOwner = lockOwners.get(index);
            if (currentLockOwner != null && currentLockOwner != clientId) {
                System.out.println("Lock at index " + index + " is owned by another client.");
                System.out.println("Lock at index " + index + " is owned by Client " + lockOwners.get(index));
                return null;  // Someone else has the lock.
            }

            lock.writeLock().lock();  // Acquire the write lock

            try {
                System.out.println("Active read locks at index " + index + ": " + lock.getReadLockCount());

                // Return the value even if it's null
                return array[index] != null ? array[index] : "null";
            } finally {
                lock.writeLock().unlock();  // Release the write lock
            }
        }
    }

    @Override
    public String fetchElementRead(int index, int clientId) throws RemoteException {
        ReentrantReadWriteLock lock = locks[index];
        lock.readLock().lock();  // Acquire a read lock

        try {
            if (!lock.isWriteLocked() || lockOwners.get(index) == clientId) {
                String element = array[index];
                return element != null ? element : "";
            } else {
                System.out.println("Lock at index " + index + " is owned by another client.");
                System.out.println("Lock at index " + index + " is owned by Client " + lockOwners.get(index));
                return null;  // Someone else has the lock.
            }
        } finally {
            lock.readLock().unlock();  // Release the read lock

        }
    }

    @Override
    public boolean requestWriteLock(int index, int client_id) throws RemoteException {
        synchronized (lockOwners) {
            if (!lockOwners.containsKey(index) && isNoLocksOrCurrentClient(index, client_id)) {
                ReentrantReadWriteLock lock = locks[index];
                synchronized (lock) {
                    if (lock.getReadLockCount() == 0) {
                        // Grant the write lock
                        lock.writeLock().lock();
                        lockOwners.put(index, client_id);
                        lockOwnersByIndex.put(index, client_id);  // Consistently track the lock owner for this index
                        return true;
                    }
                }
            }
            System.out.println("Write lock at index " + index + " is owned by Client " + lockOwners.get(index));
            return false;  // Lock is owned by another client or there are active read locks.
        }
    }


    private boolean isNoLocksOrCurrentClient(int index, int client_id) {
        return lockOwnersByIndex.get(index) == null || lockOwnersByIndex.get(index) == client_id;
    }

    @Override
    public boolean writeBackElement(String str, int index, int client_id) throws RemoteException {
        ReentrantReadWriteLock lock = locks[index];
        lock.writeLock().lock();  // Acquire a write lock

        try {
            int currentLockOwner = lockOwners.get(index);
            if (currentLockOwner == client_id) {
                // Check if the element at the specified index is null and replace it if needed
                if (array[index] == null || !array[index].equals(str)) {
                    array[index] = str;
                    return true;
                }
            }
            return false;
        } finally {
            lock.writeLock().unlock();  // Release the write lock
        }
    }


}
