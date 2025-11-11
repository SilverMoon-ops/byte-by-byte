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

    printf("\n----- Your Mad Lib Story -----\n");
    printf("Once upon a time, in a %s land, there was a %s %s.\n", adjective1, adjective2, noun);
    printf("Every day, it loved to %s while wearing a %s hat.\n", verb, adjective3);
    printf("Everyone who saw it couldn’t stop laughing — it was truly unforgettable!\n");
    
    return 0;


}