package com.myname.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.FileInputStream;
import java.util.Properties;

public class RemoteStringArrayServer {

    public static void main(String[] args) {
        try {

            if(args.length != 1) {
                System.err.println("Usage: java RemoteStringArrayServer <path_to_config_file>");
                System.exit(1);
            }

            // Load configuration from server side file
            Properties config = new Properties();
            config.load(new FileInputStream(args[0]));




            String bindName = config.getProperty("bindName");
            int arrayCapacity = Integer.parseInt(config.getProperty("arrayCapacity"));
            String[] initialStrings = config.getProperty("initialStrings").split(",");
            int registryPort = Integer.parseInt(config.getProperty("registryPort", "1099"));  // default to 1099

//            String bindName = config.getProperty("bindName");
//            int arrayCapacity = Integer.parseInt(config.getProperty("arraySize"));
//            String[] initialStrings = config.getProperty("initialStrings").split(",");

            // Used for creating the remote object
            RemoteStringArray array = new RemoteStringArray(arrayCapacity);

            // Used for populating the array with initial strings
            for (int i = 0; i < initialStrings.length && i < arrayCapacity; i++) {
                array.insertArrayElement(i, initialStrings[i]);
            }

            // Used for binding the remote object to the registry
            Registry registry = LocateRegistry.createRegistry(registryPort);
//            Registry registry = LocateRegistry.createRegistry(1099); // Default RMI registry port
            registry.rebind(bindName, array);

            System.out.println("Server is ready and bound to: " + bindName);

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
