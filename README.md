# 🗳️ Secure and Transparent Electronic Voting System

A secure and transparent electronic voting system built using **Java Swing** for the user interface and **MySQL** for persistent storage. This application allows users to view candidates, vote using a unique voter ID, and view live results.

---

## 📌 Features

- ✅ GUI interface using Java Swing  
- ✅ Voter authentication using unique Voter ID  
- ✅ View candidate list with names and IDs  
- ✅ Cast vote securely (1 vote per Voter ID)  
- ✅ View live voting results  
- ✅ Logs votes with timestamp for transparency  
- ✅ MySQL database integration

---

## 🖥️ Technologies Used

- Java (JDK 8 or higher)  
- Swing (GUI Toolkit)  
- MySQL  
- JDBC (Java Database Connectivity)

---

## ⚙️ Database Setup (MySQL)

1. **Create Database**  
   Name it: `evoting_system`

2. **Create Tables**

   - `candidates`  
     Columns:  
     - `id`: INT, PRIMARY KEY, AUTO_INCREMENT  
     - `name`: VARCHAR(100), NOT NULL  

   - `votes`  
     Columns:  
     - `id`: INT, PRIMARY KEY, AUTO_INCREMENT  
     - `voter_id`: VARCHAR(100), UNIQUE, NOT NULL  
     - `candidate_id`: INT, NOT NULL (foreign key to `candidates.id`)  
     - `vote_time`: TIMESTAMP, defaults to current time  

3. **Insert Sample Candidates**

   - Ram Kapoor  
   - Dhiraj Chopra  
   - Ayush Mehra  
   - Prem Chandak  
   - Karan Patel  

---

## 🚀 How to Run the Project

### 🧰 Prerequisites

- Java Development Kit (JDK 8 or above)  
- MySQL Server running  
- MySQL Connector/J (JDBC driver)

### 🔧 Configuration

- Add the MySQL JDBC driver JAR (`mysql-connector-j-8.0.xx.jar`) to your project
- Update DB credentials in your Java file:
  - `DB_URL = "jdbc:mysql://localhost:3306/evoting_system"`
  - `DB_USER = "your_mysql_username"`
  - `DB_PASSWORD = "your_mysql_password"`

### ▶️ Run

If using **IntelliJ IDEA**:
- Add JDBC JAR to Project Structure → Libraries
- Run the file as a standard Java application

If using terminal:
```bash
javac -cp .;mysql-connector-j-8.0.xx.jar EVotingSystem.java
java -cp .;mysql-connector-j-8.0.xx.jar EVotingSystem
```

### 🧠 Future Enhancements
Admin login for adding/updating candidates

Voter registration and secure login system

Export results to PDF/Excel

Blockchain-style ledger for vote immutability

Email notifications after voting

### 👩‍💻 Developed By
Ekta Naresh Chandak
B.Tech Artificial Intelligence and Data Science
GitHub: ektachandak12
LinkedIn: https://www.linkedin.com/in/ekta-chandak-bb43192b5/

### 📜 License
This project is open-source and free to use under the MIT License.
