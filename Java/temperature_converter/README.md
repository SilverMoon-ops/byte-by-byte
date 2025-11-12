# 🌡️ Temperature Conversion Program (Java)

## 📘 Overview
This is a simple **Java program** that converts temperatures between **Celsius** and **Fahrenheit** using the **ternary operator**.  
It takes user input for the temperature value and the target unit, then performs the conversion and displays the result with proper formatting.

---

## 🧠 Concepts Used
- **Ternary Operator** (`condition ? value_if_true : value_if_false`)
- **Scanner Class** for user input
- **String Methods** (`toUpperCase()` and `equals()`)
- **Formatted Output** using `System.out.printf()`

---

## 🧩 Code Explanation

```java
newTemp = (unit.equals("C")) ? (temp - 32) * 5 / 9 : (temp * 9 / 5) + 32;
