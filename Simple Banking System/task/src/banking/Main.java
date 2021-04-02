package banking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Main {
    private static final String CREATE_AN_ACCOUNT = "1. Create an account";
    private static final String LOG_INTO_ACCOUNT = "2. Log into account";
    private static final String EXIT = "0. Exit";

    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws IOException {
        String input;

        DatabaseService databaseService = new DatabaseService(args[1]);
        CardService cardService = new CardService(databaseService);
        databaseService.createDatabase(args[1]);
        databaseService.createCardTable();

        do {
            System.out.println(CREATE_AN_ACCOUNT);
            System.out.println(LOG_INTO_ACCOUNT);
            System.out.println(EXIT);
            input = reader.readLine();

            switch (input) {
                case "1":
                    cardService.createAccount();
                    break;
                case "2":
                    cardService.logToAccount();
                    break;
                case "0":
                    cardService.exit();
                    break;
                default:
                    System.out.println("Please type a number from 0 to 2");
            }
        } while (!input.equals("0"));
    }
}
