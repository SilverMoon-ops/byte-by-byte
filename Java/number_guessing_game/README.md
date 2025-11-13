# 🎯 Number Guessing Game (Java)

A fun, beginner-friendly **console game** written in Java.  
Your goal: guess the randomly generated number between a given range!

This program helps you learn **loops**, **conditional logic**, **user input**, and **random number generation** — all core Java fundamentals for building interactive applications.

---

## 🧠 Game Overview

- The program randomly selects a number between **1 and 100**.
- You enter guesses until you find the correct number.
- After each guess, the program tells you if it’s **too high** or **too low**.
- When you guess correctly, it shows the **number of attempts** you took.

---

## 🧱 Key Concepts Used

| Concept | Description |
|----------|--------------|
| `Random` | Generates a random number in a specified range |
| `Scanner` | Reads user input from the console |
| `do-while` loop | Ensures the player gets at least one attempt |
| `if-else` statements | Compares guesses and gives feedback |
| `printf()` | Used for formatted output |

---

## 🧩 Code Structure

```java
int min = 1;
int max = 100;
int randomNumber = random.nextInt(min, max + 1);  
