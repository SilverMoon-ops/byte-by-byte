import java.util.Scanner;

public class Main {
    public static void main(String[] args){

        String[] questions = {"What does CPU stand for?",
                              "Which data type in Java is used to store whole numbers?",
                              "What symbol is used to start a single-line comment in Java?",
                              "Which company owns Android?",
                              "What is the shortcut for \"Copy\" on Windows?"};

        String[][] options = {{"1.Central Processing Unit", "2.Core Performance Utility", "3.Computer Power Unit", "4.Central Power Utility"},
                              {"1.float", "2.int", "3.char", "4.boolean"},
                              {"1.//", "2./**/", "3.#", "4.<>"},
                              {"1.Apple", "2.Google", "3.Microsoft", "4.Samsung"},
                              {"1.Ctrl+A", "2.Ctrl+C", "3.Ctrl+X", "4.Ctrl+V"}};

        int[] answers = {1, 2, 1, 2, 2};
        int score = 0;
        int guess;
        Scanner scanner = new Scanner(System.in);

        System.out.println("******************************");
        System.out.println("Welcome to the JAVA Quiz Game!");
        System.out.println("******************************");
        System.out.println();

        for (int i = 0; i < questions.length; i++){
            System.out.println(questions[i]);

            for (String option : options[i]){
                System.out.println(option);

            }

            System.out.print("Enter your guess: ");
            guess = scanner.nextInt();

            if (guess == answers[i]){
                System.out.println("********");
                System.out.println("CORRECT!");
                System.out.println("********");
                score++;
            }
            else {
                System.out.println("******");
                System.out.println("WRONG!");
                System.out.println("******");
            }
        }
        System.out.println("Your final score is: " + score + " out of " + questions.length);

        scanner.close();
    }
}
