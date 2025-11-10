#include <stdio.h>
#include <string.h>

int main() {

    char item[50] = "";
    float price = 0.0f;
    int quantity = 0;
    char currency = '$';
    float total = 0.0f;

    printf("What item would you like to purchase? ");
    fgets(item, sizeof(item), stdin);
    item[strlen(item) - 1 ] = 0; // Remove newline character
    
    printf("What is the price of the item? ");
    scanf("%f", &price);

    printf("How many would you like to purchase? ");
    scanf("%d", &quantity);

    total = price * quantity;
    printf("You have purchased %d %s(s) at a price of %.2f%c each for a total of %.2f%c.\n", quantity, item, price, currency, total, currency);

    return 0;
}