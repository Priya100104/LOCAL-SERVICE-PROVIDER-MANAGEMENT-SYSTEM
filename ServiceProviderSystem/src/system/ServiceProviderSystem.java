package system;

import entities.User;
import java.util.Scanner;

public class ServiceProviderSystem {
    private Scanner scanner;
    private DataManager dataManager;
    
    public ServiceProviderSystem() {
        this.scanner = new Scanner(System.in);
        this.dataManager = new DataManager();
    }
    
    public void run() {
        System.out.println("================================================");
        System.out.println("    LOCAL SERVICE PROVIDER MANAGEMENT SYSTEM    ");
        System.out.println("================================================");
        
        User.showMainMenu(scanner, dataManager);
        scanner.close();
    }
}