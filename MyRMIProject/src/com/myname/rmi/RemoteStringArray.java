package com.myname.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RemoteStringArray extends UnicastRemoteObject implements RemoteStringArrayInterface {

    private String[] array;
    private ReentrantReadWriteLock[] locks;
    private Map<Integer, Integer> readLockOwners = new HashMap<>();
    private Map<Integer, Integer> writeLockOwners = new HashMap<>();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private Map<Integer, ScheduledFuture<?>> lockTimeoutTasks = new HashMap<>();

    public RemoteStringArray(int n) throws RemoteException {
        super();
        if (n <= 0) {
            throw new RemoteException("Invalid array size provided: " + n);
        }
        initializeArray(n);
    }

    public void initializeArray(int n) throws RemoteException {
        array = new String[n];
        locks = new ReentrantReadWriteLock[n];
        for (int i = 0; i < n; i++) {
            locks[i] = new ReentrantReadWriteLock();
        }
    }

    @Override
    public int getArrayCapacity() throws RemoteException {
        if (array == null) {
            throw new RemoteException("Array has not been initialized.");
        }
        return array.length;
    }

    @Override
    public void insertArrayElement(int l, String str) throws RemoteException {
        if (l >= 0 && l < array.length) {
            if (array[l] != null) {
                throw new RemoteException("Element at index " + l + " already exists. Overwriting is not allowed.");
            }
            array[l] = str;
        } else {
            throw new RemoteException("Index out of bounds");
        }
    }

    @Override
    public void releaseLock(int l, int client_id) throws RemoteException {
        if (l < 0 || l >= array.length) {
            throw new RemoteException("Index out of bounds");
        }

        // Check if the client ID matches the one in the writeLockOwners map
        if (writeLockOwners.containsKey(l) && writeLockOwners.get(l).equals(client_id)) {
            locks[l].writeLock().unlock();
            writeLockOwners.remove(l);

            // Cancel the timeout task if it exists
            ScheduledFuture<?> timeoutTask = lockTimeoutTasks.get(l);
            if (timeoutTask != null) {
                timeoutTask.cancel(false);
                lockTimeoutTasks.remove(l);
            }
        }
        // Check if the client ID matches the one in the readLockOwners map
        else if (readLockOwners.containsKey(l) && readLockOwners.get(l).equals(client_id)) {
            locks[l].readLock().unlock();
            readLockOwners.remove(l);

            // Cancel the timeout task if it exists
            ScheduledFuture<?> timeoutTask = lockTimeoutTasks.get(l);
            if (timeoutTask != null) {
                timeoutTask.cancel(false);
                lockTimeoutTasks.remove(l);
            }
        } else {
            throw new RemoteException("Client " + client_id + " does not hold the lock for index " + l);
        }
    }

    @Override
    public boolean requestReadLock(int l, int client_id) throws RemoteException {
        if (l >= 0 && l < array.length) {
            if (writeLockOwners.containsKey(l)) {
                throw new RemoteException("Write lock is already held by another client for index " + l);
            }
            try {
                if (locks[l].readLock().tryLock(5, TimeUnit.SECONDS)) { // wait up to 5 seconds
                    readLockOwners.put(l, client_id);
                    return true;
                } else {
                    throw new RemoteException("Failed to acquire read lock for index " + l + " within timeout.");
                }
            } catch (InterruptedException e) {
                throw new RemoteException("Lock acquisition interrupted", e);
            }
        } else {
            throw new RemoteException("Index out of bounds");
        }
    }

    @Override
    public boolean requestWriteLock(int l, int client_id) throws RemoteException {
        if (l >= 0 && l < array.length) {
            if (readLockOwners.containsKey(l)) {
                throw new RemoteException("Read lock is already held by another client for index " + l);
            }
            if (writeLockOwners.containsKey(l)) {
                throw new RemoteException("Write lock is already held by another client for index " + l);
            }
            if (locks[l].writeLock().tryLock()) {
                writeLockOwners.put(l, client_id);
                final int lockTimeout = 1;  // 1 minute

                // Schedule a task to release the lock after the timeout
                ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
                    try {
                        releaseLock(l, client_id);
                        System.out.println("Lock at index " + l + " released due to timeout.");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }, lockTimeout, TimeUnit.MINUTES);

                // Store the timeout task so it can be canceled if needed
                lockTimeoutTasks.put(l, timeoutTask);

                System.out.println("Write lock acquired by client " + client_id + " for index " + l);
                return true;
            } else {
                throw new RemoteException("Failed to acquire write lock for index " + l);
            }
        }
        throw new RemoteException("Index out of bounds");
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
                throw new RemoteException("Failed to acquire read lock for index " + l);
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
                throw new RemoteException("Failed to acquire write lock for index " + l);
            }
        }
        throw new RemoteException("Index out of bounds");
    }

    @Override
    public boolean writeBackElement(String str, int l, int client_id) throws RemoteException {
        if (l >= 0 && l < array.length) {
            if (locks[l].isWriteLockedByCurrentThread()) {
                array[l] = str;

                // Release the lock and cancel the timeout task
                releaseLock(l, client_id);
                ScheduledFuture<?> timeoutTask = lockTimeoutTasks.get(l);
                if (timeoutTask != null) {
                    timeoutTask.cancel(false);
                    lockTimeoutTasks.remove(l);
                }

                return true;
            } else {
                throw new RemoteException("Client " + client_id + " does not hold the write lock for index " + l);
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

