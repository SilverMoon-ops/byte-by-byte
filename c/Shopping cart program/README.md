Shopping Cart Program in C

This is a simple Shopping Cart Program written in C. The program allows a user to input an item, its price, and the quantity they want to purchase. It then calculates the total cost and displays a summary of the purchase.

Features

Prompt user for an item name.

Prompt user for the price of a single item.

Prompt user for the quantity to purchase.

Calculates and displays the total cost.

Handles string input properly using fgets to avoid buffer issues.

Key Learnings

While creating this program, I learned:

Input buffers: Using fgets for string input avoids issues that scanf("%s") may cause with spaces and leftover newline characters.

Header files: Learned to include string.h to use functions like strlen() to process input strings.

Data handling: How to calculate totals and handle float and int types in calculations.

Basic program structure in C: Including initialization of variables, printf formatting, and scanf for numeric input.

How to Use

Compile the program using a C compiler (like gcc):

gcc shopping_cart.c -o shopping_cart


Run the program:

./shopping_cart


Follow the prompts to input:

Item name

Price per item

Quantity

The program will display a summary like:

You have purchased 3 of Apples/s at a price of 2.50$ each for a total of 7.50$

Example
What item would you like to purchase?: Apple
What is the price for each?: 2.50
How many would you like to buy?: 3
You have purchased 3 of Apple/s at a price of 2.50$ each for a total of 7.50$