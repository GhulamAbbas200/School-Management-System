# 🏫 EduCore: School Management System

A premium Java Swing desktop application integrated with Microsoft SQL Server using JDBC. Developed as a university Operating Systems Lab project, this application features a modern split-panel UI, real-time database management, automated database table setup/self-repair, and a dynamic Light/Dark theme toggler.

---

# ✨ Features Included

### 1. 🔐 Multi-Role Authentication

* **Admin Portal**
  Restricted access for system staff and faculty.
  **Default Credentials:** `admin / admin123`

* **Student Portal**
  Access for students to view grades, attendance, and fee history.
  **Default Password:** `student123`

---

### 2. 👤 Student Registry

* Add student profiles
* Search dynamically by:

  * Name
  * Roll Number
* Update student records
* Delete student records

---

### 3. 🎓 Faculty Management

* Add teacher profiles
* View faculty information
* Assign subjects to teachers

---

### 4. 📅 Attendance Tracker

* Mark daily attendance:

  * Present
  * Absent
* View attendance history and logs

---

### 5. 📊 Academic Reports (Result Module)

* Add subject marks
* Automatically compute letter grades:

  * `A`
  * `B`
  * `C`
  * `D`
  * `F`
* Generate and view student transcripts

---

### 6. 💳 Fee Modules

* Process tuition payments
* View payment history
* Monitor outstanding balances

---

### 7. ⚙️ Auto-Repairing Database Helper

* Self-initializing JDBC integration
* Automatically creates:

  * Database
  * Required tables
* Repairs missing schema components if needed

---

### 8. 🌓 Theme Customization

* Modern Dark Mode
* Clean Light Mode
* Instant theme switching

---

# 🛠️ Technology Stack

| Component       | Technology                                                           |
| --------------- | -------------------------------------------------------------------- |
| **Language**    | Java 11+                                                             |
| **GUI Toolkit** | Java Swing                                                           |
| **Database**    | Microsoft SQL Server                                                 |
| **Driver**      | Microsoft JDBC Driver for SQL Server (`mssql-jdbc-12.6.1.jre11.jar`) |

---

# 📁 Repository Structure

```text
├── SchoolManagementSystem.java    # Main entry point & authentication modules
├── DatabaseHelper.java            # JDBC connection & database initialization
├── ThemeConstants.java            # Global colors, styling tokens, and theme engine
├── VectorIcon.java                # Programmatic UI vector icons
├── StudentPanel.java              # Student registry management view
├── TeacherPanel.java              # Faculty management view
├── AttendancePanel.java           # Attendance log and marking view
├── ResultPanel.java               # Academic grades and transcripts view
├── FeePanel.java                  # Fee ledgers and transaction view
├── package.bat                    # Automation script to compile & package to JAR
├── db.properties.example          # Template database configuration
├── db_setup.sql                   # Database T-SQL schema reference
└── DEPLOYMENT.md                  # Production deployment guide
```

---

# 🚀 Setup & Running Instructions

## ✅ Prerequisite 1: Enable TCP/IP in SQL Server

1. Open **SQL Server Configuration Manager**
2. Navigate to:

```text
SQL Server Network Configuration
    └── Protocols for MSSQLSERVER
```

3. Right-click **TCP/IP** → Select **Enable**
4. Right-click **TCP/IP** → Select **Properties**
5. Open the **IP Addresses** tab
6. Scroll to **IPAll**
7. Set:

```text
TCP Port = 1433
```

8. Restart the SQL Server service from Windows Services

---

## ✅ Prerequisite 2: Download the JDBC Driver

Download the Microsoft JDBC Driver for SQL Server and place the `.jar` file directly inside the project root directory.

---

## ✅ Prerequisite 3: Local Configuration

1. Copy:

```text
db.properties.example
```

2. Rename the copied file to:

```text
db.properties
```

3. Configure:

* Database host
* Port
* Username
* Password

Example:

```properties
db.host=localhost
db.port=1433
db.user=sa
db.password=yourpassword
```

4. For Windows Integrated Authentication:

```properties
db.integratedSecurity=true
```

---

# 📦 How to Compile, Build, and Run

## Option A: Automated Script (Recommended)

Run the packaging script:

```cmd
package.bat
```

This will:

* Compile the project
* Package the application
* Create a runnable version inside the `dist/` directory

Then navigate to:

```text
dist/
```

and run:

```cmd
run.bat
```

---

## Option B: Manual Commands

Open Command Prompt (`cmd`) inside the project directory.

### Compile

```cmd
javac -cp ".;mssql-jdbc-12.6.1.jre11.jar" *.java
```

### Run

```cmd
java -cp ".;mssql-jdbc-12.6.1.jre11.jar" SchoolManagementSystem
```

---

# 🔐 Default Access Credentials

## 👨‍💼 System Administrator

```text
Username: admin
Password: admin123
```

---

## 👨‍🎓 Student Profile

```text
Username / Roll Number: [Created Roll Number]
Password: student123
```
