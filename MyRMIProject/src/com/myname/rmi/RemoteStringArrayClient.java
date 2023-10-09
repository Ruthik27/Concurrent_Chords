package com.myname.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.io.FileInputStream;
import java.util.Scanner;

public class RemoteStringArrayClient {
    private static RemoteStringArrayInterface array;
    private static String[] localArray;

    public static void main(String[] args) {
        try {
            // Load configuration from client_config.properties
            Properties config = new Properties();
            config.load(new FileInputStream("resources/client_config.properties"));

            String bindName = config.getProperty("bindName");

            // Connect to the registry
            Registry registry = LocateRegistry.getRegistry("localhost"); // Example host

            // Lookup the remote object
            array = (RemoteStringArrayInterface) registry.lookup(bindName);

            // Initialize local array with the same capacity as the server array
            localArray = new String[array.getArrayCapacity()];

            // Command line interface for user
            Scanner scanner = new Scanner(System.in);
            while (true) {
                displayMenu();
                String choice = scanner.nextLine();
                handleChoice(choice);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void displayMenu() {
        System.out.println("Choose an operation:");
        System.out.println("1. Get Array Capacity");
        System.out.println("2. Fetch Element (Read)");
        System.out.println("3. Fetch Element (Write)");
        System.out.println("4. Insert Element");
        System.out.println("5. Release Lock at Specific Index");
        System.out.println("6. Write Back Element");
        System.out.println("7. Concatenate to Element");
        System.out.println("8. Print Element");
        System.out.println("9. Exit");
        System.out.println("10. Print Local Array");
    }

    private static void handleChoice(String choice) {
        Scanner scanner = new Scanner(System.in);
        int index;
        String input;
        String result;

        switch (choice) {
            case "1":
                // Handle Get Array Capacity
                try {
                    System.out.println("Array Capacity: " + array.getArrayCapacity());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "2":
                // Handle Fetch Element (Read)
                System.out.print("Enter index to fetch (read): ");
                index = Integer.parseInt(scanner.nextLine());
                try {
                    result = array.fetchElementRead(index, 1);
                    localArray[index] = result;  // Store in local array
                    System.out.println(result != null ? "Success: " + result : "Failure");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "3":
                // Handle Fetch Element (Write)
                System.out.print("Enter index to fetch (write): ");
                index = Integer.parseInt(scanner.nextLine());
                try {
                    result = array.fetchElementWrite(index, 1);
                    localArray[index] = result;  // Store in local array
                    System.out.println(result != null ? "Success: " + result : "Failure");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "4":
                // Handle Insert Element
                System.out.print("Enter index to insert: ");
                index = Integer.parseInt(scanner.nextLine());
                System.out.print("Enter string to insert: ");
                input = scanner.nextLine();
                localArray[index] = input;  // Store in local array
                System.out.println("Element inserted successfully.");
                break;

            case "5":
                // Handle Release Lock at Specific Index
                System.out.print("Enter index to release lock: ");
                index = Integer.parseInt(scanner.nextLine());
                try {
                    array.releaseLock(index, 1); // Assuming client_id = 1
                    System.out.println("Lock released successfully.");
                } catch (IllegalMonitorStateException e) {
                    System.out.println("Error: You either didn't have the lock or the resource was already unlocked.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "6":
                // Handle Write Back Element
                System.out.print("Enter index to write back: ");
                index = Integer.parseInt(scanner.nextLine());
                try {
                    boolean success = array.writeBackElement(localArray[index], index, 1);
                    System.out.println(success ? "Success" : "Failure");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "7":
                // Handle Concatenate to Element
                System.out.print("Enter index to concatenate: ");
                index = Integer.parseInt(scanner.nextLine());
                System.out.print("Enter string to concatenate: ");
                input = scanner.nextLine();
                try {
                    String current = localArray[index];
                    if (current == null) {
                        localArray[index] = input;
                    } else {
                        localArray[index] = current + input;
                    }
                    System.out.println("Concatenation successful.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "8":
                // Handle Print Element
                System.out.print("Enter index to print: ");
                index = Integer.parseInt(scanner.nextLine());
                try {
                    // Attempt to fetch a read lock
                    String fetchedElement = array.fetchElementRead(index, 1); // Assuming client_id = 1
                    if (fetchedElement != null) {
                        System.out.println(fetchedElement);
                        array.releaseLock(index, 1); // Release the read lock after printing
                    } else {
                        System.out.println("Failed to acquire read lock for index " + index);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "9":
                System.exit(0);
                break;

            case "10":
                // Handle Print Local Array
                for (int i = 0; i < localArray.length; i++) {
                    System.out.println("Index " + i + ": " + localArray[i]);
                }
                break;

            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
}