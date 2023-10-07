package com.myname.rmi;

import java.rmi.RemoteException;

import java.rmi.Naming;
import java.util.Properties;
import java.io.FileInputStream;
import java.util.Scanner;



public class RemoteStringArrayClient {
    private static String[] localArray;
    // Add the printLocalArray method here
    private static void printLocalArray() {
        System.out.println("Local Array Contents:");
        for (int i = 0; i < localArray.length; i++) {
            System.out.println("Index " + i + ": " + localArray[i]);
        }
    }
    public static void main(String[] args) {
        try {
            // Load properties from a configuration file
            Properties prop = new Properties();
            prop.load(new FileInputStream("resources/client_config.properties"));
            String bindName = prop.getProperty("bindName", "RemoteStringArrayService");

            // Connect to the RMI registry and look up the remote object
            RemoteStringArrayInterface remoteArray = (RemoteStringArrayInterface) Naming.lookup(bindName);
// Fetch the size of the array from the server and initialize the local array
            int serverArraySize = remoteArray.getArrayCapacity();
            localArray = new String[serverArraySize];

            Scanner scanner = new Scanner(System.in);
            boolean continueRunning = true;

            // Connect to the RMI registry and look up the remote object



            while (continueRunning) {
                System.out.println("Choose an operation:");
                System.out.println("1. Get Array Capacity");
                System.out.println("2. Fetch Element (Read)");
                System.out.println("3. Fetch Element (Write)");
                System.out.println("4. Insert Element");
                System.out.println("5. Release Lock");
                System.out.println("6. Write Back Element");
                System.out.println("7. Concatenate to Element");
                System.out.println("8. Print Element");
                System.out.println("9. Exit");
                System.out.println("10. Check Client holding Read Lock");
                System.out.println("11. Check Client holding Write Lock");
                System.out.println("12. Force Release Lock");
                System.out.println("13. Print Local Array");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character
                switch (choice) {
                    case 1:
                        // Get Array Capacity
                        try {
                            System.out.println("Array capacity: " + remoteArray.getArrayCapacity());
                        } catch (RemoteException e) {
                            System.out.println("Error fetching array capacity: " + e.getMessage());
                        }
                        break;
                    case 2:
                        try {
                            // Fetch Element (Read)
                            System.out.print("Enter index: ");
                            int indexRead = scanner.nextInt();
                            String elementRead = remoteArray.fetchElementRead(indexRead, 1);
                            localArray[indexRead] = elementRead;  // Update the local array
                            System.out.println("Element at index " + indexRead + ": " + elementRead);
                        } catch (RemoteException e) {
                            System.out.println("Error fetching element for reading: " + e.getMessage());
                        }
                        break;

                    case 3:
                        try {
                            // Fetch Element (Write)
                            System.out.print("Enter index: ");
                            int indexWrite = scanner.nextInt();
                            String elementWrite = remoteArray.fetchElementWrite(indexWrite, 1);
                            localArray[indexWrite] = elementWrite;  // Store the fetched element in the local array
                            System.out.println("Element fetched and stored in local array at index " + indexWrite);
                        } catch (RemoteException e) {
                            System.out.println("Error fetching element for writing: " + e.getMessage());
                        }
                        break;

                    case 4:
                        // Insert Element into the local array
                        System.out.print("Enter index: ");
                        int indexInsert = scanner.nextInt();
                        System.out.print("Enter string to insert: ");
                        String strInsert = scanner.next();
                        localArray[indexInsert] = strInsert;  // Insert the string into the local array
                        System.out.println("Element inserted into local array at index " + indexInsert + ".");
                        break;

                    case 5:
                        try {
                            // Release Lock
                            System.out.print("Enter index: ");
                            int indexRelease = scanner.nextInt();
                            remoteArray.releaseLock(indexRelease, 1);
                            System.out.println("Lock released successfully.");
                        } catch (RemoteException e) {
                            System.out.println("Error releasing lock: " + e.getMessage());
                        }
                        break;

                    case 6:
                        System.out.print("Enter index: ");
                        int indexWriteBack = scanner.nextInt();
                        try {
                            String strWriteBack = localArray[indexWriteBack];
                            remoteArray.writeBackElement(strWriteBack, indexWriteBack, 1);
                            System.out.println("Element from local array written back to server array at index " + indexWriteBack + ".");
                        } catch (RemoteException e) {
                            System.out.println("Error: You don't have the lock for index " + indexWriteBack);
                        }
                        break;

                    case 7:
                        System.out.print("Enter index: ");
                        int indexConcat = scanner.nextInt();
                        if (localArray[indexConcat] != null) {
                            System.out.print("Enter string to concatenate: ");
                            String strConcat = scanner.next();
                            localArray[indexConcat] += strConcat;
                            System.out.println("String concatenated to local array element at index " + indexConcat + ".");
                        } else {
                            System.out.println("Error: Element at index " + indexConcat + " has not been fetched.");
                        }
                        break;

                    case 8:
                        // Print Element
                        System.out.print("Enter index: ");
                        int indexPrint = scanner.nextInt();

                        try {
                            String elementPrint = remoteArray.fetchElementRead(indexPrint, 1);
                            System.out.println("Element at index " + indexPrint + ": " + elementPrint);
                        } catch (RemoteException e) {
                            if (e.getMessage().contains("Write lock is already held")) {
                                System.out.println("Error: The element at index " + indexPrint + " is currently locked by another client. Please try again later or release the lock if you hold it.");
                            } else {
                                System.out.println("An error occurred: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        break;
                    case 9:
                        continueRunning = false;
                        break;


                    case 10:
                        System.out.print("Enter index: ");
                        int indexReadLock = scanner.nextInt();
                        int clientHoldingReadLock = remoteArray.getClientHoldingReadLock(indexReadLock);
                        if (clientHoldingReadLock != -1) {
                            System.out.println("Client " + clientHoldingReadLock + " holds the read lock on element at index " + indexReadLock);
                        } else {
                            System.out.println("No client holds the read lock on element at index " + indexReadLock);
                        }
                        break;

                    case 11:
                        System.out.print("Enter index: ");
                        int indexWriteLock = scanner.nextInt();
                        int clientHoldingWriteLock = remoteArray.getClientHoldingWriteLock(indexWriteLock);
                        if (clientHoldingWriteLock != -1) {
                            System.out.println("Client " + clientHoldingWriteLock + " holds the write lock on element at index " + indexWriteLock);
                        } else {
                            System.out.println("No client holds the write lock on element at index " + indexWriteLock);
                        }
                        break;

                    case 12:
                        System.out.print("Enter index: ");
                        int indexForceRelease = scanner.nextInt();
                        System.out.print("Enter client ID to force release: ");
                        int clientIdForceRelease = scanner.nextInt();
                        try {
                            remoteArray.forceReleaseLock(indexForceRelease, clientIdForceRelease);
                            System.out.println("Lock released successfully for client " + clientIdForceRelease + " on element at index " + indexForceRelease);
                        } catch (RemoteException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                        break;

                    case 13:
                        printLocalArray();
                        break;

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine(); // Consume the invalid input
            }
        }

        scanner.close();

    } catch (Exception e) {
        System.out.println("An error occurred: " + e.getMessage());
        e.printStackTrace();
    }
}
}