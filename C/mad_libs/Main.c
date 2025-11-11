#include <stdio.h>
#include <string.h>

int main() {
   
    // Mad Libs Game

    char noun[50] = "";
    char verb[50] = "";
    char adjective1[50] = "";
    char adjective2[50] = "";
    char adjective3[50] = "";

    printf("Enter an adjective (descripition): ");
    fgets(adjective1, sizeof(adjective1), stdin);
    adjective1[strlen(adjective1) - 1] = '\0'; // Remove newline character

    printf("Enter a noun (person, place, or thing): ");
    fgets(noun, sizeof(noun), stdin);
    noun[strlen(noun) - 1] = '\0'; // Remove newline character

    printf("Enter an adjective (descripition): ");
    fgets(adjective2, sizeof(adjective2), stdin);
    adjective2[strlen(adjective2) - 1] = '\0'; // Remove newline character

    printf("Enter a verb (action word): ");
    fgets(verb, sizeof(verb), stdin);
    verb[strlen(verb) - 1] = '\0'; // Remove newline character

    printf("Enter an adjective (descripition): ");
    fgets(adjective3, sizeof(adjective3), stdin);
    adjective3[strlen(adjective3) - 1] = '\0'; // Remove newline character

    printf("%s\n", noun);
    printf("%s\n", verb);
    printf("%s\n", adjective1);
    printf("%s\n", adjective2);
    printf("%s\n", adjective3);

    return 0;


}