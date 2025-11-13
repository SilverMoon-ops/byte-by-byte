import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){

        Random random = new Random();
        Scanner scanner = new Scanner(System.in);

        int guess;
        int attempts = 0;
        int randomNumber = random.nextInt(1, 11);

        System.out.println("Number Guessing Game");
        System.out.println("Guess the number between 1 - 10");

        do {
            System.out.print("Enter a guess:");
            guess = scanner.nextInt();
            attempts++;

            if (guess < randomNumber) {
                System.out.println("TOO LOW! Try again");
            } else if (guess > randomNumber) {
                System.out.println("TOO HIGH! Try again");
            } else {
                System.out.println("CORRECT! The number was " + randomNumber);
                System.out.println("# of attempts " + attempts);
            }
        }
        while (guess != randomNumber) ;
        scanner.close();

    }
}
