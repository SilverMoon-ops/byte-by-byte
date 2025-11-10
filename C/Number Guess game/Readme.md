# 🔢 Number Guessing Game in C

This is a simple number guessing game written in the C programming language. The program generates a random number between 1 and 100, and the user must guess it. After each guess, the program gives a hint whether the number is higher or lower than the guess.

## 🎮 How to Play

- The computer generates a random number between **1 to 100**
- You try to guess it by entering a number
- The game will guide you:
    - If your guess is too high → it will say "Lower number please!"
    - If your guess is too low → it will say "Higher number please!"
- Once you guess it correctly, it will tell you how many attempts you took to get the right answer.

## 🧠 Concepts Used

- `Loops` (`do-while`)
- `Conditionals` (`if-else`)
- `Random number generation` using `rand()` and `srand()`
- `User input` via `scanf()`
- `Header files` like `stdio.h`, `stdlib.h`, and `time.h`

## 💻 How to Run

1. **Copy the code into a file** — name it for example: `guessing_game.c`
2. Open your terminal and **compile the code** using:

   ```bash
   gcc guessing_game.c -o guessing_game
