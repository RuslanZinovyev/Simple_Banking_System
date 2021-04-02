package banking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class CardService {

    private static final String BALANCE = "1. Balance";
    private static final String ADD_INCOME = "2. Add income";
    private static final String DO_TRANSFER = "3. Do transfer";
    private static final String CLOSE_ACCOUNT = "4. Close account";
    private static final String LOG_OUT = "5. Log out";
    private static final String EXIT = "0. Exit";

    private static final String BANK_IDENTIFICATION_NUMBER = "400000";
    private static final String YOUR_CARD_HAS_BEEN_CREATED = "Your card has been created";
    private static final String YOUR_CARD_NUMBER = "Your card number:";
    private static final String YOUR_CARD_PIN = "Your card PIN:";


    public static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private final DatabaseService databaseService;

    public CardService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public void logToAccount() throws IOException {
        System.out.println('\n' + "Enter your card number:");
        String creditCardNumber = reader.readLine();
        System.out.println("Enter your PIN:");
        String pinCode = reader.readLine();
        if (databaseService.checkIfCardAndPinExist(creditCardNumber, pinCode) != 0) {
            System.out.println('\n' + "You have successfully logged in!" + '\n');
            String input;
            do {
                System.out.println(BALANCE);
                System.out.println(ADD_INCOME);
                System.out.println(DO_TRANSFER);
                System.out.println(CLOSE_ACCOUNT);
                System.out.println(LOG_OUT);
                System.out.println(EXIT);
                input = reader.readLine();
                accountDetails(input, creditCardNumber);
            } while (!input.equals("0") && !input.equals("5"));
        } else {
            System.out.println("Wrong card number or PIN!\n");
        }
    }

    private void accountDetails(String input, String fromCard) throws IOException {
        switch (input) {
            case "1":
                System.out.println('\n' + "Balance: " + getBalance(fromCard) + '\n');
                break;
            case "2":
                System.out.println("\nEnter income:");
                String income = reader.readLine();
                addIncome(income, fromCard);
                System.out.println("Income was added!\n");
                break;
            case "3":
                System.out.println("\nTransfer\n" +
                        "Enter card number:");
                String toCard = reader.readLine();
                if (!doesItPassLuhnAlgorithm(toCard)) {
                    System.out.println("Probably you made a mistake in the card number. Please try again!\n");
                    break;
                } else if (!databaseService.checkIfCardExists(toCard)) {
                    System.out.println("Such a card does not exist.\n");
                    break;
                } else if (fromCard.equals(toCard)) {
                    System.out.println("You can't transfer money to the same account!\n");
                    break;
                } else {
                    System.out.println("Enter how much money you want to transfer:");
                    String amount = reader.readLine();
                    doTransfer(toCard, fromCard, amount);
                }
                break;
            case "4":
                closeAccount(fromCard);
                System.out.println("The account has been closed!\n");
                break;
            case "5":
                System.out.println('\n' + "You have successfully logged out!" + '\n');
                break;
            case "0":
                exit();
                break;
            default:
                System.out.println("Please type a number from 0 to 2");
        }
    }

    public void createAccount() {
        String accountIdentifier = getRandomTenDigitsNumberString();
        String pinCode = getRandomFourDigitsNumberString();
        String checkSum = generateCheckSum(BANK_IDENTIFICATION_NUMBER, accountIdentifier);
        String cardNumber = BANK_IDENTIFICATION_NUMBER + accountIdentifier + checkSum;

        System.out.println(YOUR_CARD_HAS_BEEN_CREATED);
        System.out.println(YOUR_CARD_NUMBER);
        System.out.println(BANK_IDENTIFICATION_NUMBER + accountIdentifier + checkSum);
        System.out.println(YOUR_CARD_PIN);
        System.out.println(pinCode + '\n');

        databaseService.addCard(cardNumber, pinCode);
    }

    public void exit() {
        System.out.println("\nBye!\n");
        databaseService.closeDatabaseConnection();
        System.exit(0);
    }

    private int getBalance(String cardNumber) {
        return databaseService.getCardBalance(cardNumber);
    }

    private void addIncome(String income, String cardNumber) {
        databaseService.addIncomeToCardBalance(Integer.parseInt(income), cardNumber);
    }

    private void doTransfer(String toCard, String fromCard, String amount) {
         if (!databaseService.checkIfAmountExceedBalance(fromCard, amount)) {
            System.out.println("Not enough money!");
        } else {
             databaseService.transferMoneyFromCardToCard(toCard, fromCard, amount);
             System.out.println("Success!\n");
         }
    }

    private void closeAccount(String cardNumber) {
        databaseService.deleteCard(cardNumber);
    }

    private boolean doesItPassLuhnAlgorithm(String cardNumber) {
        String bankIdentificationNumber = cardNumber.substring(0, 6);
        String accountIdentifier = cardNumber.substring(6, 15);
        String checkSum = cardNumber.substring(15, 16);

        if (generateCheckSum(bankIdentificationNumber, accountIdentifier).equals(checkSum)) {
            return true;
        }
        return false;
    }

    private String getRandomFourDigitsNumberString() {
        Random random = new Random();
        int pinCode = random.nextInt(9999);

        return String.format("%04d", pinCode);
    }

    private String getRandomTenDigitsNumberString() {
        // It will generate 9 digit BIN random Number from 0 to 999999999.
        Random rnd = new Random();
        int number = rnd.nextInt(999999999);

        // this will convert any number sequence into 9 character.
        return String.format("%09d", number);
    }

    private String generateCheckSum(String bin, String accountIdentifier) {
        String cardNumber = bin + accountIdentifier;
        String[] arr = cardNumber.split("");
        int[] nums = new int[15];
        for (int i = 0; i < arr.length; i++) {
            nums[i] = Integer.parseInt(arr[i]);
        }
        // Multiply odd digits by two
        for (int i = 0; i < nums.length; i++) {
            if ((i + 1) % 2 != 0) {
                nums[i] = nums[i] * 2;
            }
        }
        // Subtract 9 to numbers over 9
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] > 9) {
                nums[i] = nums[i] - 9;
            }
        }
        int sum  = 0;
        // Add all numbers
        for (int i = 0; i < nums.length; i++) {
            sum += nums[i];
        }

        int checkSum = (10 - (sum % 10)) % 10;

        return String.valueOf(checkSum);
    }
}
