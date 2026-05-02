# 📚 Library Management System (Java + JPA + MySQL)

A robust and scalable **Library Management System** built using **Java**, **JPA (Java Persistence API)**, and **MySQL**.  
This project demonstrates clean architecture, object-relational mapping, and real-world database relationships.

---

## 🚀 Features

- 👤 Manage users (Persons)
- 📖 Manage books
- 🏛 Manage libraries
- 🔗 Handle many-to-many relationship between libraries and books
- 📦 Borrowing system
- 🕒 Track borrowed books
- 💾 Persistent data using JPA (Hibernate)

---

## 🧠 System Design

The system is designed using **Object-Oriented Programming (OOP)** principles and **ORM (Object Relational Mapping)** via JPA.

### 📌 Entities

| Entity         | Description |
|----------------|------------|
| `Person`       | Represents users who can borrow books |
| `Book`         | Represents books in the system |
| `Library`      | Represents a library |
| `LibraryBook`  | Join table between Library and Book |
| `Borrow`       | Represents borrowing transactions |

---

## 🗄 Database Design

### Relationships:

- 📚 A **Library** can have many **Books**
- 📖 A **Book** can exist in many **Libraries**
- 👤 A **Person** can borrow multiple **Books**
- 🔄 `LibraryBook` handles many-to-many relationship
- 📦 `Borrow` links:
  - Person → Book
  - Includes borrow metadata (date, etc.)

---

## ⚙️ Technologies Used

- **Java**
- **JPA (Jakarta Persistence API)**
- **Hibernate (JPA Implementation)**
- **MySQL**
- **Maven / Gradle (optional)**

---

## 🧾 ERD

<img width="850" height="580" alt="WhatsApp Image 2026-05-02 at 8 43 14 PM (1)" src="https://github.com/user-attachments/assets/9909bdec-6d45-4310-a049-f0e3c07b59b9" />

---

## 🔧 Setup & Installation

### 1. Clone the repository

```bash
git clone https://github.com/your-username/library-system.git
cd library-system
