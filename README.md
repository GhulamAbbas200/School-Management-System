# 🏫 EduCore: School Management System

A premium Java Swing desktop application integrated with Microsoft SQL Server using JDBC. Developed as a university Operating Systems Lab project, this application features a modern split-panel UI, real-time database management, automated database table setup/self-repair, and a dynamic Light/Dark theme toggler.

---

## ✨ Features Included

1. **🔐 Multi-Role Authentication**
   * **Admin Portal**: Restricted access for system staff and faculty (Default: `admin`/`admin123`).
   * **Student Portal**: Access for students to view grades, attendance, and fee history (Default password: `student123`).
2. **👤 Student Registry**
   * Add, search (dynamically by name/roll number), update, and delete student profiles.
3. **🎓 Faculty Management**
   * Add and view teacher profiles and assign subjects.
4. **📅 Attendance Tracker**
   * Mark daily student attendance (Present/Absent) and view historical logs.
5. **📊 Academic Reports (Result Module)**
   * Add subject marks, compute letter grades (`A`, `B`, `C`, `D`, `F`) automatically, and view student result transcripts.
6. **💳 Fee Modules**
   * Process tuition payments, view paid amounts, and monitor outstanding balances.
7. **⚙️ Auto-Repairing Database helper**
   * Self-initializing SQL Server JDBC integration. Automatically creates the database and all required tables if they do not exist.
8. **🌓 Theme Customization**
   * Instant transition between modern Dark Mode and clean Light Mode.

---

## 🛠️ Technology Stack

* **Language**: Java 11+
* **GUI Toolkit**: Java Swing (with custom rounded panels, cards, and vector icons)
* **Database**: Microsoft SQL Server
* **Driver**: Microsoft JDBC Driver for SQL Server (`mssql-jdbc-12.6.1.jre11.jar`)

---

## 📁 Repository Structure

```text
├── SchoolManagementSystem.java   # Main entry point & Auth modules
├── DatabaseHelper.java           # JDBC Connection & Database Initialization
├── ThemeConstants.java          # Global colors, styling tokens, and Theme engine
├── VectorIcon.java               # Programmatic UI vector icons
├── StudentPanel.java             # Student Registry management view
├── TeacherPanel.java             # Faculty management view
├── AttendancePanel.java          # Attendance log and marking view
├── ResultPanel.java              # Academic grades and transcripts view
├── FeePanel.java                 # Fee ledgers and transaction view
├── package.bat                   # Automation script to compile & package to JAR
├── db.properties.example         # Template database configuration
├── db_setup.sql                  # Database T-SQL schema reference
└── DEPLOYMENT.md                 # Step-by-step production deployment guide
```

🚀 Setup & Running Instructions
Prerequisite 1: Enable TCP/IP in SQL Server
Open SQL Server Configuration Manager.
Go to SQL Server Network Configuration -> Protocols for MSSQLSERVER.
Right-click TCP/IP and select Enable.
Right-click TCP/IP and select Properties. Under the IP Addresses tab, scroll to IPAll and set the TCP Port to 1433.
Restart the SQL Server service in Windows Services.
Prerequisite 2: Download the JDBC Driver
Download the Microsoft JDBC Driver for SQL Server (JAR) and place the .jar file directly in the project root directory.

Prerequisite 3: Local Configuration
Copy db.properties.example and rename the copy to db.properties.
Fill in your SQL Server database host (e.g., localhost), port, login credentials (user & password).
If using Windows Integrated Authentication, set db.integratedSecurity=true.
📦 How to Compile, Build, and Run
Option A: Using the Automated Script (Recommended)
Simply run the package script in your command line:

cmd
package.bat
This compiles the application, packages it, and places a runnable version inside a new dist/ directory. Go to dist/ and double-click run.bat to launch the app!

Option B: Manual Commands
Open your Command Prompt (cmd) in this directory and execute:

Compile:
cmd
javac -cp ".;mssql-jdbc-12.6.1.jre11.jar" *.java
Run:
cmd
java -cp ".;mssql-jdbc-12.6.1.jre11.jar" SchoolManagementSystem
🔐 Default Access Credentials
System Administrator
Username: admin
Password: admin123
Student Registry Profile (Default)
Username / Roll No: [Created Roll Number]
Password: student123
9:28 PM
