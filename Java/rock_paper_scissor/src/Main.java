import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){

        // ROCK PAPER SCISSORS GAME

        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        String[] choices = {"rock", "paper", "scissor"};
        String playerChoice;
        String computerChoice;
        String playAgain = "yes";

        do {

            System.out.print("Enter your move (rock, paper, scissor): ");
            playerChoice = scanner.nextLine().toLowerCase();

            if (!playerChoice.equals("rock") &&
                    !playerChoice.equals("paper") &&
                    !playerChoice.equals("scissor")) {
                System.out.println("INVALID!");
                continue;
            }

            computerChoice = choices[random.nextInt(3)];
            System.out.println("Computer Choice: " + computerChoice);

            if (playerChoice.equals(computerChoice)) {
                System.out.println("It's a tie!");
            } else if (playerChoice.equals("rock") && computerChoice.equals("scissor") ||
                    playerChoice.equals("paper") && computerChoice.equals("rock") ||
                    playerChoice.equals("scissor") && computerChoice.equals("paper")) {
                System.out.println("You win!");
            } else {
                System.out.println("You lose!");
            }

            System.out.print("Play Again! (yes/no): ");
            playAgain = scanner.nextLine().toLowerCase();

        }while (playAgain.equals("yes"));

        System.out.println("Thanks for playing!");

        scanner.close();
    }
}
