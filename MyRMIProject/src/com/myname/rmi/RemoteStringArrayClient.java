package com.myname.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.io.FileInputStream;
import java.util.Scanner;

public class RemoteStringArrayClient {
    private static RemoteStringArrayInterface array;
    private static String[] localArray;
    private static int clientId;


    public static void main(String[] args) {
        try {

            if(args.length != 1) {
                System.err.println("Usage: java RemoteStringArrayClient <path_to_client_config_file>");
                System.exit(1);
            }

            // Load configuration from client side file
            Properties config = new Properties();
            config.load(new FileInputStream(args[0]));

            String bindName = config.getProperty("bindName");
            String serverName = config.getProperty("serverName");
            int serverPort = Integer.parseInt(config.getProperty("serverPort", "1099"));  // default to 1099

            // Connect to the RMI registry on the specified server and port
            Registry registry = LocateRegistry.getRegistry(serverName, serverPort);

            array = (RemoteStringArrayInterface) registry.lookup(bindName);

            localArray = new String[array.getArrayCapacity()];

            String userAndId = array.registerUser();
            System.out.println("You are registered with: " + userAndId);
            clientId = Integer.parseInt(userAndId.split("_")[1]);  // Extracting client ID from the returned string


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
        System.out.println("Select option to execute a task:");
        System.out.println("1. Get Array Capacity");
        System.out.println("2. Fetch Element (Read)");
        System.out.println("3. Fetch Element (Write)");
        System.out.println("4. Insert Element");
        System.out.println("5. Release Lock at Specific Index");
        System.out.println("6. Write Back Elements");
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
                //  Array Capacity
                try {
                    System.out.println("Array Capacity: " + array.getArrayCapacity());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "2":
                //  Fetch Element (Read)
                System.out.print("Enter index to fetch (read): ");
                index = Integer.parseInt(scanner.nextLine());
                try {
                    boolean lockGranted = array.requestReadLock(index, clientId);
                    if (!lockGranted) {
                        System.out.println("Failed to acquire read lock for index " + index);
                        break;
                    }
                    result = array.fetchElementRead(index, clientId);
                    if (result != null) {
                        System.out.println("Success: " + result);
                    } else {
                        System.out.println("Element at index " + index + " is blank (null).");
                    }
                    localArray[index] = result;  // local array
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "3":
                // Fetch Element (Write)
                System.out.print("Enter index to fetch (write): ");
                index = Integer.parseInt(scanner.nextLine());
                try {
                    boolean lockGranted = array.requestWriteLock(index, clientId);
                    if (!lockGranted) {
                        System.out.println("Failed to acquire write lock for index " + index);
                        break;
                    }
                    result = array.fetchElementWrite(index, clientId);
                    if (result != null) {
                        localArray[index] = result;  // Update the local array with the fetched element
                        System.out.println("Success: " + result);
                    } else {
                        System.out.println("Failure");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "4":
                //  Insert Element
                System.out.print("Enter index to insert: ");
                try {
                    index = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Error: Please enter a valid integer for the index.");
                    break; // Exit the case
                }
                System.out.print("Enter string to insert: ");
                input = scanner.nextLine();
                localArray[index] = input;  // Store it in local array
                System.out.println("Element inserted successfully.");
                break;

            case "5":
                //  Release Lock at Specific Index
                System.out.print("Enter index to release lock: ");
                index = Integer.parseInt(scanner.nextLine());
                try {
                    array.releaseLock(index, clientId);
                    System.out.println("Lock released successfully.");
                } catch (IllegalMonitorStateException e) {
                    System.out.println("Error: You either didn't have the lock or the resource was already unlocked.");
                } catch (Exception e) {
                    System.out.println("Failure");
                    //e.printStackTrace();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;

            case "6":
                //  Write Back Element to the server
                System.out.print("Enter index to write back: ");
                index = Integer.parseInt(scanner.nextLine());
                try {
                    boolean success = array.writeBackElement(localArray[index], index, clientId);
                    System.out.println(success ? "Success" : "Failure");
                } catch (Exception e) {
                    System.out.println("Failure");
                    //e.printStackTrace();
                }
                break;

            case "7":
                //  Concatenate to Element
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
                //  Print Element
                System.out.print("Enter index to print: ");
                index = Integer.parseInt(scanner.nextLine());
                try {
                    // Attempt to fetch a read lock
                    String fetchedElement = array.fetchElementRead(index, clientId);
                    if (fetchedElement != null) {
                        System.out.println(fetchedElement);
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