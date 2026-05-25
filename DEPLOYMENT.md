# School Management System Deployment Guide

This guide explains how to package, distribute, and deploy the **EduCore School Management System** on client machines or production environments.

---

## 📦 Step 1: Automatic Packaging (Recommended)

To make deployment as easy as possible, a script named [package.bat](file:///c:/Users/Abbas/Desktop/MAJU/Semester%20No%203%20at%20MAJU(6th)/Operating%20System%20Lab/Assignments/Assignment%20%233/package.bat) has been created in the root directory.

### How to use it:
1. Open a Command Prompt (cmd) in the project directory.
2. Run the script:
   ```cmd
   package.bat
   ```
3. The script will automatically:
   * Compile all your Java source files.
   * Create a JAR manifest with the correct `Main-Class` and classpath dependencies.
   * Package all `.class` files into a single runnable `SchoolManagementSystem.jar`.
   * Gather the JAR, the SQL Server JDBC Driver, a default `db.properties`, and a double-clickable launch script into a new `dist/` directory.

---

## 📂 Step 2: Distribution Folder Structure

Once packaged, the `dist/` folder will contain the following files:

```text
dist/
├── SchoolManagementSystem.jar     # The packaged application
├── mssql-jdbc-12.6.1.jre11.jar     # Microsoft SQL Server JDBC Driver
├── db.properties                  # Database connection credentials
└── run.bat                        # Double-clickable Windows runner script
```

> [!IMPORTANT]
> To deploy this application to any other client machine, simply copy the entire `dist/` folder to that machine. The target machine only needs **Java (JRE/JDK 11 or higher)** installed.

---

## ⚙️ Step 3: Database & Network Configuration

Since the application connects to a central Microsoft SQL Server database, you must configure the network settings:

### 1. Enable TCP/IP on SQL Server
For Java to communicate with your SQL Server over the network:
1. Open **SQL Server Configuration Manager** on the database server.
2. Go to **SQL Server Network Configuration** -> **Protocols for MSSQLSERVER** (or your named instance).
3. Right-click **TCP/IP** and select **Enable**.
4. Right-click **TCP/IP** and select **Properties**. Go to the **IP Addresses** tab, scroll down to **IPAll**, and verify **TCP Port** is set to `1433`.
5. Restart the **SQL Server** service in Windows Services.

### 2. Configure `db.properties`
On the client machine, open the `db.properties` file in a text editor and update the connection details to point to the server:

```properties
# Database Configuration for School Management System
db.host=192.168.1.10      # Replace with the SQL Server's IP address (use 'localhost' if running on the same machine)
db.port=1433              # SQL Server port
db.user=sa                # SQL Server Login Username
db.password=MyPassword123 # SQL Server Login Password
db.integratedSecurity=false
```

> [!TIP]
> **Database Self-Repair:** You don't need to manually run SQL scripts to create tables on the target database. When the application starts, it will automatically connect to SQL Server, create the `school_management_db` database if it doesn't exist, build all the necessary tables, and seed the default admin account.

---

## 🚀 Step 4: Running the App

On the client machine:
1. Ensure Java is installed (run `java -version` in cmd to verify).
2. Double-click the `run.bat` file inside the `dist/` folder.
3. The GUI login screen will launch instantly!

### Default Credentials:
* **Admin Username**: `admin`
* **Admin Password**: `admin123`
