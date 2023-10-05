package com.myname.rmi;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;
import java.io.FileInputStream;

public class RemoteStringArrayServer {

    public static void main(String[] args) {
        try {
            // Check if a command line argument is provided
            if (args.length < 1) {
                System.out.println("Please provide the path to the configuration file as a command line argument.");
                return;
            }

            String configFilePath = args[0]; // Use the provided argument as the configuration file path

            // Load properties from the provided configuration file
            Properties prop = new Properties();
            prop.load(new FileInputStream(configFilePath));
            String bindName = prop.getProperty("bindName", "RemoteStringArrayService");

            // Start the RMI registry on the default port (1099)
            // LocateRegistry.createRegistry(1099);

            // Create an instance of the RemoteStringArray
            int arraySize = Integer.parseInt(prop.getProperty("arraySize", "10")); // Default size is 10
            RemoteStringArrayInterface remoteArray = new RemoteStringArray(arraySize);

            // Initialize the array with the strings from the configuration file
            String[] initialStrings = prop.getProperty("initialStrings", "").split(",");
            for (int i = 0; i < Math.min(arraySize, initialStrings.length); i++) {
                remoteArray.insertArrayElement(i, initialStrings[i]);
            }

            // Bind the remote object to a name in the RMI registry
            Naming.rebind(bindName, remoteArray);

            System.out.println("RemoteStringArrayServer is running and waiting for client requests...");

            // Add a shutdown hook to unbind the remote object
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Naming.unbind(bindName);
                    System.out.println("RemoteStringArrayServer shut down successfully.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

        } catch (RemoteException e) {
            System.out.println("RemoteException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
