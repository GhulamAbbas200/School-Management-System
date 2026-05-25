import java.io.*;
import java.sql.*;
import java.util.Properties;

/**
 * Helper class to manage MySQL JDBC connection and database initialization.
 */
public class DatabaseHelper {
    private static final String CONFIG_FILE = "db.properties";
    private static String host = "localhost";
    private static String port = "1433";
    private static String user = "sa";
    private static String password = "";
    private static boolean integratedSecurity = false;
    private static final String DB_NAME = "school_management_db";

    static {
        loadConfig();
    }

    /**
     * Loads database credentials from db.properties file.
     * Fallback to default values if file does not exist.
     */
    public static void loadConfig() {
        Properties props = new Properties();
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                props.load(input);
                host = props.getProperty("db.host", "localhost");
                port = props.getProperty("db.port", "1433");
                user = props.getProperty("db.user", "sa");
                password = props.getProperty("db.password", "");
                integratedSecurity = Boolean.parseBoolean(props.getProperty("db.integratedSecurity", "false"));
            } catch (IOException e) {
                System.err.println("Error loading config: " + e.getMessage());
            }
        } else {
            saveConfig(host, port, user, password);
        }
    }

    /**
     * Saves database credentials to db.properties file.
     */
    public static void saveConfig(String newHost, String newPort, String newUser, String newPassword) {
        host = newHost;
        port = newPort;
        user = newUser;
        password = newPassword;
        Properties props = new Properties();
        props.setProperty("db.host", host);
        props.setProperty("db.port", port);
        props.setProperty("db.user", user);
        props.setProperty("db.password", password);
        props.setProperty("db.integratedSecurity", String.valueOf(integratedSecurity));
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            props.store(output, "Database Configuration for School Management System");
        } catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
        }
    }

    /**
     * Obtains connection to the specific database 'school_management_db'.
     */
    public static Connection getConnection() throws SQLException {
        loadDriver();
        String url = "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + DB_NAME + ";encrypt=true;trustServerCertificate=true;";
        if (integratedSecurity) {
            url += "integratedSecurity=true;";
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Obtains connection to the SQL Server (without specifying DB).
     */
    public static Connection getServerConnection() throws SQLException {
        loadDriver();
        String url = "jdbc:sqlserver://" + host + ":" + port + ";encrypt=true;trustServerCertificate=true;";
        if (integratedSecurity) {
            url += "integratedSecurity=true;";
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Checks if a connection can be established with given credentials.
     */
    public static boolean testConnection(String testHost, String testPort, String testUser, String testPassword) {
        loadDriver();
        String url = "jdbc:sqlserver://" + testHost + ":" + testPort + ";encrypt=true;trustServerCertificate=true;";
        if (integratedSecurity) {
            url += "integratedSecurity=true;";
            try (Connection conn = DriverManager.getConnection(url)) {
                return conn != null;
            } catch (SQLException e) {
                return false;
            }
        }
        try (Connection conn = DriverManager.getConnection(url, testUser, testPassword)) {
            return conn != null;
        } catch (SQLException e) {
            return false;
        }
    }

    private static void loadDriver() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Warning: Microsoft SQL Server JDBC Driver not found in classpath.");
        }
    }

    /**
     * Dynamically initializes the database and creates tables if they don't exist.
     */
    public static void initializeDatabase() throws SQLException {
        // Step 1: Connect to server and create database if missing
        try (Connection serverConn = getServerConnection();
             Statement stmt = serverConn.createStatement()) {
            stmt.executeUpdate(
                "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'" + DB_NAME + "') " +
                "BEGIN " +
                "  CREATE DATABASE " + DB_NAME + "; " +
                "END"
            );
        }

        // Step 2: Connect to the DB and execute creation of tables
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create Users table
            stmt.executeUpdate(
                "IF OBJECT_ID(N'users', N'U') IS NULL " +
                "BEGIN " +
                "  CREATE TABLE users (" +
                "    username VARCHAR(50) PRIMARY KEY," +
                "    password VARCHAR(50) NOT NULL" +
                "  ); " +
                "END"
            );

            // Create Students table
            stmt.executeUpdate(
                "IF OBJECT_ID(N'students', N'U') IS NULL " +
                "BEGIN " +
                "  CREATE TABLE students (" +
                "    roll_number VARCHAR(50) PRIMARY KEY," +
                "    name VARCHAR(100) NOT NULL," +
                "    class_name VARCHAR(50) NOT NULL," +
                "    password VARCHAR(50) NOT NULL DEFAULT 'student123'," +
                "    status VARCHAR(20) NOT NULL DEFAULT 'Verified'," +
                "    enroll_date DATETIME NOT NULL DEFAULT GETDATE()" +
                "  ); " +
                "END ELSE " +
                "BEGIN " +
                "  IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'students' AND COLUMN_NAME = 'password') " +
                "  BEGIN " +
                "    ALTER TABLE students ADD password VARCHAR(50) NOT NULL DEFAULT 'student123'; " +
                "  END " +
                "  IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'students' AND COLUMN_NAME = 'status') " +
                "  BEGIN " +
                "    ALTER TABLE students ADD status VARCHAR(20) NOT NULL DEFAULT 'Verified'; " +
                "  END " +
                "  IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'students' AND COLUMN_NAME = 'enroll_date') " +
                "  BEGIN " +
                "    ALTER TABLE students ADD enroll_date DATETIME NOT NULL DEFAULT GETDATE(); " +
                "  END " +
                "END"
            );

            // Create Teachers table
            stmt.executeUpdate(
                "IF OBJECT_ID(N'teachers', N'U') IS NULL " +
                "BEGIN " +
                "  CREATE TABLE teachers (" +
                "    teacher_id VARCHAR(50) PRIMARY KEY," +
                "    name VARCHAR(100) NOT NULL," +
                "    subject VARCHAR(100) NOT NULL" +
                "  ); " +
                "END"
            );

            // Create Attendance table
            stmt.executeUpdate(
                "IF OBJECT_ID(N'attendance', N'U') IS NULL " +
                "BEGIN " +
                "  CREATE TABLE attendance (" +
                "    id INT IDENTITY(1,1) PRIMARY KEY," +
                "    roll_number VARCHAR(50)," +
                "    attendance_date DATE NOT NULL," +
                "    status VARCHAR(20) NOT NULL," +
                "    FOREIGN KEY (roll_number) REFERENCES students(roll_number) ON DELETE CASCADE" +
                "  ); " +
                "END"
            );

            // Create Results table
            stmt.executeUpdate(
                "IF OBJECT_ID(N'results', N'U') IS NULL " +
                "BEGIN " +
                "  CREATE TABLE results (" +
                "    id INT IDENTITY(1,1) PRIMARY KEY," +
                "    roll_number VARCHAR(50)," +
                "    subject VARCHAR(100) NOT NULL," +
                "    marks INT NOT NULL," +
                "    grade VARCHAR(5) NOT NULL," +
                "    FOREIGN KEY (roll_number) REFERENCES students(roll_number) ON DELETE CASCADE," +
                "    CONSTRAINT unique_student_subject UNIQUE (roll_number, subject)" +
                "  ); " +
                "END"
            );

            // Create Fees table
            stmt.executeUpdate(
                "IF OBJECT_ID(N'fees', N'U') IS NULL " +
                "BEGIN " +
                "  CREATE TABLE fees (" +
                "    roll_number VARCHAR(50) PRIMARY KEY," +
                "    total_fee DECIMAL(10, 2) NOT NULL," +
                "    paid_fee DECIMAL(10, 2) NOT NULL," +
                "    remaining_fee DECIMAL(10, 2) NOT NULL," +
                "    FOREIGN KEY (roll_number) REFERENCES students(roll_number) ON DELETE CASCADE" +
                "  ); " +
                "END"
            );

            // Create Student Subjects table
            stmt.executeUpdate(
                "IF OBJECT_ID(N'student_subjects', N'U') IS NULL " +
                "BEGIN " +
                "  CREATE TABLE student_subjects (" +
                "    roll_number VARCHAR(50) NOT NULL," +
                "    subject_name VARCHAR(100) NOT NULL," +
                "    PRIMARY KEY (roll_number, subject_name)," +
                "    FOREIGN KEY (roll_number) REFERENCES students(roll_number) ON DELETE CASCADE" +
                "  ); " +
                "END"
            );

            // Seed Admin user if not exists
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "IF NOT EXISTS (SELECT username FROM users WHERE username = ?) " +
                    "BEGIN " +
                    "  INSERT INTO users (username, password) VALUES (?, ?); " +
                    "END")) {
                pstmt.setString(1, "admin");
                pstmt.setString(2, "admin");
                pstmt.setString(3, "admin123");
                pstmt.executeUpdate();
            }
        }
    }

    public static String getHost() { return host; }
    public static String getPort() { return port; }
    public static String getUser() { return user; }
    public static String getPassword() { return password; }
}
