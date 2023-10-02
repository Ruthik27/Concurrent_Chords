package com.myname.rmi;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;
import java.io.FileInputStream;

public class RemoteStringArrayServer {

    public static void main(String[] args) {
        try {
            // Load properties from a configuration file
            Properties prop = new Properties();
            prop.load(new FileInputStream("resources/server_config.properties"));
            String bindName = prop.getProperty("bindName", "RemoteStringArrayService");

            // Start the RMI registry on the default port (1099)
            LocateRegistry.createRegistry(1099);

            // Create an instance of the RemoteStringArray
            RemoteStringArrayInterface remoteArray = new RemoteStringArray();

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
