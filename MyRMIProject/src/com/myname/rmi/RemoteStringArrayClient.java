package com.myname.rmi;

import java.rmi.RemoteException;

import java.rmi.Naming;
import java.util.Properties;
import java.io.FileInputStream;
import java.util.Scanner;

public class RemoteStringArrayClient {

    public static void main(String[] args) {
        try {
            // Load properties from a configuration file
            Properties prop = new Properties();
            prop.load(new FileInputStream("resources/client_config.properties"));
            String bindName = prop.getProperty("bindName", "RemoteStringArrayService");

            // Connect to the RMI registry and look up the remote object
            RemoteStringArrayInterface remoteArray = (RemoteStringArrayInterface) Naming.lookup(bindName);

            Scanner scanner = new Scanner(System.in);
            boolean continueRunning = true;

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
                            System.out.println("Element at index " + indexWrite + ": " + elementWrite);
                        } catch (RemoteException e) {
                            System.out.println("Error fetching element for writing: " + e.getMessage());
                        }
                        break;

                    case 4:
                        // Insert Element
                        System.out.print("Enter index: ");
                        int indexInsert = scanner.nextInt();
                        System.out.print("Enter string to insert: ");
                        String strInsert = scanner.next();
                        try {
                            remoteArray.insertArrayElement(indexInsert, strInsert);
                            System.out.println("Element inserted successfully at index " + indexInsert + ".");
                        } catch (RemoteException e) {
                            System.out.println("Error inserting element: " + e.getMessage());
                        }
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
                        try {
                            // Write Back Element
                            System.out.print("Enter index: ");
                            int indexWriteBack = scanner.nextInt();
                            System.out.print("Enter string to write back: ");
                            String strWriteBack = scanner.next();
                            remoteArray.writeBackElement(strWriteBack, indexWriteBack, 1);
                            System.out.println("Element written back successfully.");
                        } catch (RemoteException e) {
                            System.out.println("Error writing back element: " + e.getMessage());
                        }
                        break;
                    // ... [Keep the existing cases 2 to 6 as they are]
                    case 7:
                        // Concatenate to Element
                        System.out.print("Enter index: ");
                        int indexConcat = scanner.nextInt();
                        System.out.print("Enter string to concatenate: ");
                        String strConcat = scanner.next();
                        String updatedStr = remoteArray.fetchElementWrite(indexConcat, 1) + strConcat;
                        remoteArray.writeBackElement(updatedStr, indexConcat, 1);
                        System.out.println("Element updated successfully.");
                        break;
                    case 8:
                        // Print Element
                        System.out.print("Enter index: ");
                        int indexPrint = scanner.nextInt();
                        String elementPrint = remoteArray.fetchElementRead(indexPrint, 1);
                        System.out.println("Element at index " + indexPrint + ": " + elementPrint);
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

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }

            scanner.close();

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
