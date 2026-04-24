package main;  // This matches folder structure src/main/

import system.ServiceProviderSystem;

public class MainApp {
    public static void main(String[] args) {
        ServiceProviderSystem system = new ServiceProviderSystem();
        system.run();
    }
}
