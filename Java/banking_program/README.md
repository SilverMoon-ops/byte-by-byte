# Banking Program (Java)

A simple console-based banking application written in Java. This program allows a user to:

* View account balance
* Deposit money
* Withdraw money
* Exit the program

This project is perfect for beginners who want to practice Java basics like:

* Loops
* Methods
* Conditionals
* Scanner input
* Simple arithmetic operations

---

## 🚀 Features

* **Show Balance:** Displays the current account balance with proper formatting.
* **Deposit Money:** Allows the user to add money while preventing negative deposits.
* **Withdraw Money:** Ensures users cannot withdraw more than the available balance.
* **Input Validation:** Protects against invalid and negative inputs.
* **Menu Loop:** Program continues running until the user chooses to exit.

---

## 📌 How It Works

The program runs inside a `while` loop and displays a menu with four options. Based on the user's choice, the corresponding method is executed:

* `showBalance(balance)`
* `deposit()`
* `withdraw(balance)`

Each action updates the `balance` variable accordingly.

---

## 🧩 Code Structure

```
Main
├── showBalance(double balance)
├── deposit()
└── withdraw(double balance)
```

---

## 💻 Running the Program

### 1. Compile the program

```bash
javac Main.java
```

### 2. Run the program

```bash
java Main
```

---

## 📝 Example Output

```
*****************
BANKING PROGRAM
*****************
1. Show Balance
2. Deposit
3. Withdraw
4. Exit
*****************
Enter your choice (1-4): 1
$0.00
```

---

## 📚 What I Learn From This Project

* Method creation and calling
* Passing parameters
* Returning values
* Basic input validation
* Loop control
* Building text-based user interfaces

---

## 🌟 Future Improvements

You can expand this project by adding:

* PIN authentication
* Transaction history
* Interest calculator
* Multiple accounts
* Data storage (text file or database)

---

## ❤️ Author

Created by **Kei** while learning Java and building foundational programming skills.
