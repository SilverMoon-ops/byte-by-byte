import java.util.Scanner;

public class Main {
    public static void main(String[]args){

        Scanner scanner = new Scanner(System.in);

        double num1;
        double num2;
        String operator;
        double result;

        System.out.println("Enter the first number: ");
        num1 = scanner.nextDouble();

        System.out.println("Enter the operator ( +, -, *, /, ^, %, MOD ");
        operator = scanner.nextLine();

        System.out.println("Enter the second number: ");
        num2 = scanner.nextDouble();
        

        scanner.close();

    }
}
