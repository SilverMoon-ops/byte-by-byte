# 🎯 Number Guessing Game (Java)

A simple console-based **number guessing game** built in Java.  
This beginner exercise demonstrates how to use:

- `Random` for random number generation
- `Scanner` for user input
- `do-while` loops and `if-else` statements for logic control

---

## 🧠 Game Overview

The program randomly selects a number between **1 and 10**.  
You must guess the number — the game will tell you if your guess is too high or too low until you get it right.

Each guess is counted, and the total number of attempts is shown when you win.

---

## 🧱 How It Works

1. The game generates a random number:
   ```java
   int randomNumber = random.nextInt(1, 11);
   
2. It asks the player to guess a number.

It checks:

If the guess is lower → prints “Too low!”

If the guess is higher → prints “Too high!”

If the guess matches → congratulates the player and shows attempt count.

The loop repeats until the correct number is guessed.