-- Database Setup for School Management System (Microsoft SQL Server / T-SQL)
-- Run this in SQL Server Management Studio (SSMS)

-- Create Database if it does not exist
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'school_management_db')
BEGIN
    CREATE DATABASE school_management_db;
END
GO

USE school_management_db;
GO

-- Table 1: Users (for Authentication)
IF OBJECT_ID(N'users', N'U') IS NULL
BEGIN
    CREATE TABLE users (
        username VARCHAR(50) PRIMARY KEY,
        password VARCHAR(50) NOT NULL
    );
END

-- Table 2: Students
IF OBJECT_ID(N'students', N'U') IS NULL
BEGIN
    CREATE TABLE students (
        roll_number VARCHAR(50) PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        class_name VARCHAR(50) NOT NULL
    );
END

-- Table 3: Teachers
IF OBJECT_ID(N'teachers', N'U') IS NULL
BEGIN
    CREATE TABLE teachers (
        teacher_id VARCHAR(50) PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        subject VARCHAR(100) NOT NULL
    );
END

-- Table 4: Attendance
IF OBJECT_ID(N'attendance', N'U') IS NULL
BEGIN
    CREATE TABLE attendance (
        id INT IDENTITY(1,1) PRIMARY KEY,
        roll_number VARCHAR(50),
        attendance_date DATE NOT NULL,
        status VARCHAR(20) NOT NULL,
        FOREIGN KEY (roll_number) REFERENCES students(roll_number) ON DELETE CASCADE
    );
END

-- Table 5: Results
IF OBJECT_ID(N'results', N'U') IS NULL
BEGIN
    CREATE TABLE results (
        id INT IDENTITY(1,1) PRIMARY KEY,
        roll_number VARCHAR(50),
        subject VARCHAR(100) NOT NULL,
        marks INT NOT NULL,
        grade VARCHAR(5) NOT NULL,
        FOREIGN KEY (roll_number) REFERENCES students(roll_number) ON DELETE CASCADE,
        CONSTRAINT unique_student_subject UNIQUE (roll_number, subject)
    );
END

-- Table 6: Fees
IF OBJECT_ID(N'fees', N'U') IS NULL
BEGIN
    CREATE TABLE fees (
        roll_number VARCHAR(50) PRIMARY KEY,
        total_fee DECIMAL(10, 2) NOT NULL,
        paid_fee DECIMAL(10, 2) NOT NULL,
        remaining_fee DECIMAL(10, 2) NOT NULL,
        FOREIGN KEY (roll_number) REFERENCES students(roll_number) ON DELETE CASCADE
    );
END

-- Seed initial admin user if not exists
IF NOT EXISTS (SELECT username FROM users WHERE username = 'admin')
BEGIN
    INSERT INTO users (username, password) VALUES ('admin', 'admin123');
END
