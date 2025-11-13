import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(){

        Random random = new Random();
        Scanner scanner = new Scanner(System.in);

        int guess;
        int attempts;
        int randomNumber = random.nextInt(1, 11);

        System.out.println(randomNumber);

        scanner.close();

    }
}
