package main;

import system.ServiceProviderSystem;

/**
 * ============================================================
 *  CONSOLE MODE ENTRY POINT  (NOT the Spring Boot main class)
 * ============================================================
 *  Run Console : mvn exec:java -Dexec.mainClass="main.MainApp"
 *  Run Spring  : mvn spring-boot:run   (uses web.SpringApp)
 * ============================================================
 *  NOTE: Spring Boot's main class is web.SpringApp
 *        This class is just for the console menu program.
 * ============================================================
 */
public class MainApp {

    public static void main(String[] args) {
        ServiceProviderSystem system = new ServiceProviderSystem();
        system.run();
    }
}
