import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserAuth auth = new UserAuth();
        RateFetcher fetcher = new ApiRateFetcher();
        CurrencyConverter converter = new CurrencyConverter(fetcher);

        System.out.println("=== Currency Converter ===");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.print("Choose option (1/2): ");
        int choice = Integer.parseInt(sc.nextLine());

        String user = "";
        String pass = "";

        if (choice == 1) {
            System.out.print("Enter new username: ");
            user = sc.nextLine();
            System.out.print("Enter new password: ");
            pass = sc.nextLine();
            if (auth.register(user, pass)) {
                System.out.println("Registration successful! Please login.");
            } else {
                System.out.println("Username already exists.");
                sc.close();
                return;
            }
        }

        System.out.print("Enter username: ");
        user = sc.nextLine();
        System.out.print("Enter password: ");
        pass = sc.nextLine();

        if (auth.login(user, pass)) {
            System.out.println("Login successful! Welcome " + user);
            while (true) {
                System.out.print("\nBase currency (e.g. USD): ");
                String base = sc.nextLine().toUpperCase();
                System.out.print("Target currency (e.g. BDT): ");
                String target = sc.nextLine().toUpperCase();
                System.out.print("Amount: ");
                double amount = Double.parseDouble(sc.nextLine());

                converter.convert(base, target, amount);

                System.out.print("Do you want another conversion? (yes/no): ");
                String again = sc.nextLine();
                if (!again.equalsIgnoreCase("yes")) {
                    System.out.println("Thank you for using the Currency Converter. Goodbye!");
                    break;
                }
            }
        } else {
            System.out.println("Login failed.");
        }

        sc.close();
    }
}
