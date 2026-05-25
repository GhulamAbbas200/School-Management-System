# School Management System GUI

A premium Java Swing Desktop Application connected to a Microsoft SQL Server database using JDBC.

## Features Included
1. **Login Module**: Secure admin credentials checking with automatic database fallback.
2. **Student Module**: Add new student profiles, search dynamically, and delete students.
3. **Teacher Module**: Add and view teacher profiles with subjects.
4. **Attendance Module**: Mark daily attendance (Present/Absent) and filter log reports.
5. **Result Module**: Add subject marks, compute grades (`A`, `B`, `C`, `D`, `F`) automatically, and view reports.
6. **Fee Module**: Process student tuition payments and view balances.
7. **Database Module**: Fully integrated with SQL Server using JDBC with self-repairing/auto-initializing DB & table setups.
8. **Logout Module**: Securely clear and close dashboard panels.

---

## Setup and Running Instructions

### Prerequisite 1: Enable TCP/IP in SQL Server Configuration Manager
For Java to connect to Microsoft SQL Server, TCP/IP must be enabled:
1. Open **SQL Server Configuration Manager**.
2. Go to **SQL Server Network Configuration** -> **Protocols for MSSQLSERVER** (or your instance name).
3. Right-click **TCP/IP** and select **Enable**.
4. Right-click **TCP/IP** and select **Properties**. Go to the **IP Addresses** tab, scroll to **IPAll**, and ensure **TCP Port** is set to `1433`.
5. Restart the **SQL Server (MSSQLSERVER)** service in Windows Services.

---

### Prerequisite 2: SQL Server JDBC Connector (JAR)
The application requires the Microsoft SQL Server JDBC driver to connect.
1. Download **Microsoft JDBC Driver for SQL Server**:
   * **Direct Download Link (Maven Central)**: [mssql-jdbc-12.6.1.jre11.jar](https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/12.6.1.jre11/mssql-jdbc-12.6.1.jre11.jar)
   * **Official Page**: [Microsoft JDBC Download](https://learn.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server)
2. Copy the `.jar` file (e.g., `mssql-jdbc-12.6.1.jre11.jar`) directly into the folder:
   `c:\Users\Abbas\Desktop\MAJU\Semester No 3 at MAJU(6th)\Operating System Lab\Assignments\Assignment #3\`

---

### Prerequisite 3: Compilation
Open Command Prompt (cmd) or PowerShell in this directory:
```cmd
cd "c:\Users\Abbas\Desktop\MAJU\Semester No 3 at MAJU(6th)\Operating System Lab\Assignments\Assignment #3"
```

Compile all Java files with the JDBC driver in the classpath:
* **Option A (If Java/JDK is in your System PATH):**
  ```cmd
  javac -cp ".;mssql-jdbc-12.6.1.jre11.jar" *.java
  ```
* **Option B (Using the absolute JDK 26 path on your system):**
  ```cmd
  "C:\Program Files\Java\jdk-26.0.1\bin\javac.exe" -cp ".;mssql-jdbc-12.6.1.jre11.jar" *.java
  ```

---

### Prerequisite 4: Execution
Run the compiled application by including the classpath. Choose the execution command based on your authentication method:

#### Mode 1: SQL Server Authentication (Recommended & Easiest)
* **On Command Prompt (cmd):**
  ```cmd
  java -cp ".;mssql-jdbc-12.6.1.jre11.jar" SchoolManagementSystem
  ```
* **On PowerShell:**
  ```powershell
  java -cp ".;mssql-jdbc-12.6.1.jre11.jar" SchoolManagementSystem
  ```
* **Using absolute JDK 26 path (cmd):**
  ```cmd
  "C:\Program Files\Java\jdk-26.0.1\bin\java.exe" -cp ".;mssql-jdbc-12.6.1.jre11.jar" SchoolManagementSystem
  ```

#### Mode 2: Windows Authentication (Integrated Security)
Requires downloading the native `mssql-jdbc_auth-12.6.1.x64.dll` file and copying it to the directory.
* **On Command Prompt (cmd):**
  ```cmd
  java -Djava.library.path=. -cp ".;mssql-jdbc-12.6.1.jre11.jar" SchoolManagementSystem
  ```
* **On PowerShell:**
  ```powershell
  java -Djava.library.path=. -cp ".;mssql-jdbc-12.6.1.jre11.jar" SchoolManagementSystem
  ```
* **Using absolute JDK 26 path (cmd):**
  ```cmd
  "C:\Program Files\Java\jdk-26.0.1\bin\java.exe" -Djava.library.path=. -cp ".;mssql-jdbc-12.6.1.jre11.jar" SchoolManagementSystem
  ```

---

## Default Login Credentials
* **Username**: `admin`
* **Password**: `admin123`
