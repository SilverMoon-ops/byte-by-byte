#include <stdio.h>

int main() {
    
    //Shopping cart program
 
    char item[50] = "";
    float price = 0.0f;
    int quantity = 0;
    char currency = '$';
    float total = 0.0f;

    // i have initialized the variables right here to avoid any undefined behavior

    printf("What item would you like to purchase?: ");
    fgets(item, sizeof(item), stdin);

    printf("What is the price for each?: ");
    scanf("%f", &price);

    printf("How many would you like to buy?: ");
    scanf("%d", &quantity);

    total = price * quantity;

    printf("You have purchased %d of %s at a price of %.2f%c each for a total of %.2f%c\n", quantity, item, price, currency, total, currency);
    return 0;
}