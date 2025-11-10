import java.util.Random;

public class Main {
    public static void main(String[] args) {

        Random random = new Random();
        Boolean isHead;
        isHead = random.nextBoolean();


        if(isHead) {
            System.out.println("HEADS");
        }

        else{
            System.out.println("TAILS");
        }

    }
}


