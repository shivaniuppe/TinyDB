# TinyDb - A Lightweight DBMS Tool

# Project Overview

TinyDb is a lightweight, file-based database management system (DBMS) tool that simulates most of the core features of a traditional DBMS, such as MySQL. Instead of a complex back-end storage system, TinyDb uses a text file to store and manage the database structure and data. It allows users to perform operations like creating databases, tables, and handling data transactions, all within a simple interface.

# Features

User Authentication:
Users can create an account, log in, and authenticate using security questions and answers.
Database Management:
Create, delete, and switch between databases.
Table Management:
Create and delete tables, insert data, update existing records, and delete entries.
Query Processing:
TinyDb supports executing queries for managing databases and tables, using an intuitive command-line interface.
ERD Generation:
The system can generate an Entity-Relationship Diagram (ERD) for visualizing database structure.
SQL Export:
Export database structures and data as SQL dump files.
Transactions & Logging:
Supports transactions with commit and rollback functionalities, along with query logging for auditing and performance tracking.

# How to use

Login & Setup:
After launching TinyDb, users need to log in or create a new account. During registration, users are asked to set up a security question for future authentication.
Query Execution:
Use commands like:
CREATE DATABASE dbname; - to create a new database.
USE dbname; - to select a database.
CREATE TABLE tablename (column1 datatype, column2 datatype); - to create a new table.
INSERT INTO tablename VALUES (value1, value2); - to insert data into a table.
UPDATE tablename SET column1 = value WHERE condition; - to update data in a table.
DELETE FROM tablename WHERE condition; - to delete data from a table.
Queries are processed in a case-insensitive manner.
Transactions:
Start transactions using START TRANSACTION;, use COMMIT; to save changes, or ROLLBACK; to revert changes.
ERD Generation & SQL Export:
The system provides functionality to generate ERDs for visualizing the database structure and to export data as SQL dump files.

# Getting started

To get started with TinyDb, clone the repository and compile the Java project. The project contains a simple main interface to begin entering queries.

# Clone the repository
git clone https://github.com/shivaniuppe/dbms_builder.git

# Navigate to the project directory
cd dbms_builder

# Compile and run the project
javac -d bin src/org/example/*.java
java -cp bin org.example.QueryProcessor

# Project Structure

QueryProcessor.java:
The main class that handles query input and manages the lifecycle of the system.
LogManager.java:
Handles logging of queries for transaction tracking and execution time.
Query Handlers:
Individual query handlers for each query type such as CreateDatabaseQueryHandler, InsertIntoTableQueryHandler, etc.
Example Usage

Once the system is running, the interface will display:

Welcome to TinyDb, please start writing queries below.
dbms_builder_11 > 
You can now start entering queries like:

CREATE DATABASE example_db;
USE example_db;
CREATE TABLE users (id INT, name VARCHAR(100));
INSERT INTO users VALUES (1, 'John Doe');
SELECT * FROM users;

# Logging

Each query is logged with its execution time and timestamp for future reference. Log files are automatically generated in the system's log directory.

# Contributing

Feel free to submit issues or contribute to the development of TinyDb by submitting pull requests.

# License

This project is licensed under the MIT License.
