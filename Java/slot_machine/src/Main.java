import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){

        // JAVA SLOT MACHINE

        Scanner scanner = new Scanner(System.in);

        int balance;
        int bet;
        int payout;
        String[] row;
        String playAgain;

        System.out.println("***************************");
        System.out.println("   Welcome to Java Slots   ");
        System.out.println("Symbols: 🍒 🍉 🍋 🔔 ⭐");
        System.out.println("***************************");

        System.out.println("Choose difficulty:");
        System.out.println("1. Easy   (Recommended: $200)");
        System.out.println("2. Normal (Recommended: $100)");
        System.out.println("3. Hard   (Recommended: $50)");
        System.out.print("Enter choice (1-3): ");

        int difficulty = scanner.nextInt();
        scanner.nextLine();

        int recommendedBalance;

        switch (difficulty) {
            case 1 -> recommendedBalance = 200;
            case 2 -> recommendedBalance = 100;
            case 3 -> recommendedBalance = 50;
            default -> {
                recommendedBalance = 100;
                System.out.println("Invalid choice. Defaulting to NORMAL mode.");
            }
        }

        System.out.println("Recommended starting balance: $" + recommendedBalance);
        System.out.print("Enter your actual starting balance: $");
         balance = scanner.nextInt();
        scanner.nextLine();

        if (balance <= 0) {
            System.out.println("Invalid! Setting balance to recommended: $" + recommendedBalance);
            balance = recommendedBalance;
        }

        System.out.println("\nYou are starting the game with $" + balance);


        while (balance > 0){
            System.out.println("Current balance: $" + balance);
            System.out.print("Place your bet amount: ");
            bet = scanner.nextInt();
            scanner.nextLine();

            if(bet > balance){
                System.out.println("INSUFFICIENT FUNDS!");
                continue;
            }
            else if (bet <= 0){
                System.out.println("Bet must be greater than 0!");
                continue;
            }
            else {
                balance -= bet;

            }
            System.out.println("Spinning...");
            row = spinRow();
            printRow(row);
            payout = getPayout(row, bet);
            if (payout > 0){
                System.out.println("You won $" + payout);
                balance += payout;
            }
            else {
                System.out.println("Sorry you lost this round");
            }
            System.out.print("Do you want to play again? (Y/N): ");
            playAgain = scanner.nextLine().toUpperCase();

            if (!playAgain.equals("Y")){
                break;
            }
        }

        System.out.println("GAME OVER! Your final balance is $" + balance);

        scanner.close();
    }
    static String[] spinRow(){

        String[] symbols = {"🍒", "🍉", "🍋", "🔔", "⭐"};
        String[] row = new String[3];
        Random random = new Random();

        for (int i = 0; i < 3; i++) {
            row [i] = symbols[random.nextInt(symbols.length)];
        }

        return row;
    }
    static void printRow(String[] row){
        System.out.println("**************");
        System.out.println(" " + String.join(" | ", row));
        System.out.println("**************");
    }
    static int getPayout(String[] row, int bet){

        if (row[0].equals(row[1]) && row[1].equals(row[2])){
            return switch (row[0]){
                case "🍒" -> bet * 3;
                case "🍉" -> bet * 5;
                case "🍋" -> bet * 7;
                case "🔔" -> bet * 10;
                case "⭐" -> bet * 20;
                default -> 0;

            };
        }
        if (row[0].equals(row[1])){
            return switch (row[0]){
                case "🍒" -> bet * 2;
                case "🍉" -> bet * 3;
                case "🍋" -> bet * 4;
                case "🔔" -> bet * 7;
                case "⭐" -> bet * 10;
                default -> 0;

            };
        }
        if (row[1].equals(row[2])){
            return switch (row[1]){
                case "🍒" -> bet * 2;
                case "🍉" -> bet * 3;
                case "🍋" -> bet * 4;
                case "🔔" -> bet * 7;
                case "⭐" -> bet * 10;
                default -> 0;

            };
        }

        return 0;
    }
}
