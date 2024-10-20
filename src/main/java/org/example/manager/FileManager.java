package org.example.manager;

import org.example.database.Column;
import org.example.database.Database;
import org.example.database.Table;

import java.io.*;
import java.util.*;

public class FileManager {
    public static final String DATABASES_DIRECTORY = "Databases";
    private static final String DATABASES_FILE = DATABASES_DIRECTORY + "/databases.txt";

    /**
     * Create database directory if it does not exist
     */
    public static void createDatabaseDirectory() {
        File databasesDir = new File(DATABASES_DIRECTORY);
        if (!databasesDir.exists()) {
            boolean isCreated = databasesDir.mkdir();
            if (!isCreated)
                System.out.println("Failed to create database directory for path: " + DATABASES_DIRECTORY);
        }
    }

    /**
     * Create directory for specific database
     *
     * @param dbName Name of the database
     */
    public static void createDatabaseDirectory(String dbName) {
        File dbDirectory = new File(DATABASES_DIRECTORY + File.separator + dbName);
        if (dbDirectory.exists())
            return;
        if (!dbDirectory.mkdir())
            System.out.println("Failed to create database directory for path: " + dbDirectory);
    }

    /**
     * Create txt file for the database
     *
     * @param dbName    Name of the database
     * @param tableName Name of the table
     * @param columns   List of all columns of table
     */
    public static void createNecessaryTableFiles(String dbName, String tableName, List<Column> columns) {
        createTableFile(dbName, tableName, columns);
        createTableMetadataFile(dbName, tableName, columns);
    }

    /**
     * Write row data to file
     *
     * @param dbName    Name of the database
     * @param tableName Name of the table
     * @param row       Row data to add
     */
    public static void writeRowToFile(String dbName, String tableName, Map<String, String> row) {
        File tableFile = getTableFile(dbName, tableName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile, true))) {
            for (Map.Entry<String, String> entry : row.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue() + " | ");
            }
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Failed to write row to file for path: " + tableFile);
        }
    }

    /**
     * Save all database names into file database.txt
     *
     * @param databases List of all database's names
     */
    public static void saveDatabases(List<String> databases) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATABASES_FILE))) {
            for (String db : databases) {
                writer.write(db);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Failed to save databases to file: " + DATABASES_FILE);
        }
    }

    /**
     * Load all databases from the file into program
     *
     * @return List of all databases added
     */
    public static List<Database> loadDatabases() {
        List<Database> databases = new ArrayList<>();
        File file = new File(DATABASES_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Database db = new Database(line);
                    loadTables(db);
                    databases.add(db);
                }
            } catch (IOException e) {
                System.out.println("Failed to load databases from file: " + DATABASES_FILE);
            }
        }
        return databases;
    }

    /**
     * Get row data for provided database and table
     *
     * @param databaseName Name of the database
     * @param tableName    Name of the table
     * @return List of Map of ColumnName to ColumnValue
     */
    public static List<Map<String, String>> getRows(String databaseName, String tableName) {
        List<Map<String, String>> rows = new ArrayList<>();
        File dbDirectory = new File(DATABASES_DIRECTORY + File.separator + databaseName);
        File[] tableFiles = dbDirectory.listFiles((dir, name) -> name.endsWith(".txt"));
        if (tableFiles != null) {
            File tableFile = Arrays.stream(tableFiles)
                    .filter(file -> Objects.equals(file.getName().replace(".txt", ""), tableName))
                    .findFirst()
                    .orElse(null);
            if (tableFile == null)
                return rows;
            try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
                reader.readLine(); // Extra header line for column definition
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] rowValues = line.split(" \\| ");
                    Map<String, String> row = new HashMap<>();
                    for (String rowValue : rowValues) {
                        String[] parts = rowValue.split(": ");
                        row.put(parts[0], parts[1]);
                    }
                    rows.add(row);
                }
            } catch (IOException e) {
                System.out.println("Failed to load table file for path: " + tableFile);
            }
        }
        return rows;
    }

    /**
     * Write list of rows to the table file
     *
     * @param dbName    Name of the database
     * @param tableName Name of the table
     * @param rows      List of rows to be written
     */
    public static void writeRowsToFile(String dbName, String tableName, List<Map<String, String>> rows) {
        File tableFile = getTableFile(dbName, tableName);
        List<Column> columns = getTableColumns(dbName, tableName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
            // Add header line
            for (Column column : columns)
                writer.write(column.name() + ": " + column.type() + " " + column.constraints().toString() + " | ");
            writer.newLine();

            // Add row data
            for (Map<String, String> row : rows) {
                for (Map.Entry<String, String> entry : row.entrySet())
                    writer.write(entry.getKey() + ": " + entry.getValue() + " | ");
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Failed to write rows to file for path: " + tableFile);
        }
    }

    /**
     * Generate the sql dump file for requesting database
     * @param dbName Name of the database
     */
    public static void generateSQLDump(String dbName) {
        List<String> sqlDump = new ArrayList<>();
        sqlDump.add("CREATE DATABASE " + dbName + ";");
        sqlDump.add("USE " + dbName + ";");

        Database database = DatabaseManager.getDatabases().stream()
                .filter(db -> Objects.equals(dbName, db.getName()))
                .findFirst()
                .orElse(null);
        if (database == null)
            throw new RuntimeException("Database not present for name: " + dbName);

        File dbDirectory = new File(DATABASES_DIRECTORY + File.separator + dbName);
        File[] tableFiles = dbDirectory.listFiles((dir, name) -> name.endsWith(".txt"));
        if (tableFiles != null) {
            for (File tableFile : tableFiles) {
                if (tableFile.getName().contains("_metadata"))
                    continue;
                String tableName = tableFile.getName().replace(".txt", "");
                String createTableSQL = getCreateTableSQLQuery(dbName, tableName);
                sqlDump.add(createTableSQL);

                Table table = database.getTable(tableName);
                if (table == null)
                    throw new RuntimeException("Table not present for name: " + tableName);
                List<Column> tableColumns = table.getColumns();

                List<Map<String, String>> rows = getRows(dbName, tableName);
                for (Map<String, String> row : rows) {
                    String insertRowSQL = getInsertRowSQLQuery(tableName, tableColumns, row);
                    sqlDump.add(insertRowSQL);
                }
            }
        }

        StringBuilder dumpContent = new StringBuilder();
        for (String sql : sqlDump) {
            dumpContent.append(sql).append(System.lineSeparator());
        }
        String sqlDumpFilePath = "Databases/" + dbName + "_dump.sql";
        writeToFile(sqlDumpFilePath, dumpContent.toString());

        System.out.println(dumpContent);
    }
    // End region

    // Region: private methods
    /**
     * Create txt file for the database table
     *
     * @param dbName    Name of the database
     * @param tableName Name of the table
     * @param columns   List of all columns of table
     */
    private static void createTableFile(String dbName, String tableName, List<Column> columns) {
        File tableFile = getTableFile(dbName, tableName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
            for (Column column : columns) {
                StringBuilder header = new StringBuilder();
                // Name, type and constraints
                header.append(column.name()).append(": ")
                        .append(column.type()).append(" ")
                        .append(column.constraints().toString()).append(" ");
                // Foreign key
                if (column.foreignKeyTable() != null && column.foreignKeyColumn() != null) {
                    header.append("(")
                            .append(column.foreignKeyTable())
                            .append(": ")
                            .append(column.foreignKeyColumn())
                            .append(")");
                }
                // Column separator
                header.append(" | ");
                writer.write(header.toString());
            }
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Failed to create table file for path: " + tableFile + e.getMessage());
        }
    }

    /**
     * Create txt file for the database table metadata
     *
     * @param dbName    Name of the database
     * @param tableName Name of the table
     * @param columns   List of all columns of table
     */
    private static void createTableMetadataFile(String dbName, String tableName, List<Column> columns) {
        File tableFile = getMetadataFile(dbName, tableName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
            StringBuilder metadata = new StringBuilder();
            metadata.append("Metadata file for table: ").append(tableName).append("\n\n");
            metadata.append("Columns:\n");

            for (Column column : columns) {
                metadata.append(column.name()).append(": ").append(column.type()).append("\n");
                if (!column.constraints().isEmpty()) {
                    metadata.append("  Constraints: ").append(String.join(", ", column.constraints()));
                }
                metadata.append("\n");

                if (column.foreignKeyTable() != null && column.foreignKeyColumn() != null) {
                    metadata.append("  Foreign Key -> ").append(column.foreignKeyTable())
                            .append(".").append(column.foreignKeyColumn()).append("\n");
                }
                metadata.append("\n");
            }
            writer.write(metadata.toString());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Failed to create table file for path: " + tableFile + e.getMessage());
        }
    }

    /**
     * Provides File object for table with the requested tableName
     *
     * @param databaseName Name of the database
     * @param tableName    Name of the table
     * @return File object referencing table file
     */
    private static File getTableFile(String databaseName, String tableName) {
        String path = DATABASES_DIRECTORY + File.separator + databaseName;
        File file = new File(path);
        file.mkdirs();
        return new File(file, File.separator + tableName + ".txt");
    }

    /**
     * Provides File object for metadata with the requested tableName
     *
     * @param databaseName Name of the database
     * @param tableName    Name of the table
     * @return File object referencing table file
     */
    private static File getMetadataFile(String databaseName, String tableName) {
        String path = DATABASES_DIRECTORY + File.separator + databaseName;
        File file = new File(path);
        file.mkdirs();
        return new File(file, File.separator + tableName + "_metadata.txt");
    }

    /**
     * Loads all the table information into provided database
     *
     * @param database Database object to load tables
     */
    private static void loadTables(Database database) {
        File dbDirectory = new File(DATABASES_DIRECTORY + File.separator + database.getName());
        File[] tableFiles = dbDirectory.listFiles((dir, name) -> name.endsWith(".txt"));
        if (tableFiles != null) {
            for (File tableFile : tableFiles) {
                if (tableFile.getName().contains("_metadata"))
                    continue;
                String tableName = tableFile.getName().replace(".txt", "");
                List<Column> columns = getTableColumns(database.getName(), tableName);
                Table table = new Table(database.getName(), tableName, columns);
                database.addTable(table);
            }
        }
    }

    /**
     * Get columns of a table from the file
     *
     * @param databaseName Name of the database
     * @param tableName    Name of the table
     * @return List of columns
     */
    private static List<Column> getTableColumns(String databaseName, String tableName) {
        File tableFile = getTableFile(databaseName, tableName);
        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            List<Column> columns = new ArrayList<>();
            String headerLine = reader.readLine();
            if (headerLine != null) {
                String[] columnDefs = headerLine.split(" \\| ");
                for (String columnDef : columnDefs) {
                    String[] parts = columnDef.split(": ", 2);
                    String name = parts[0];
                    String[] typeAndConstraints = parts[1].split(" ", 2);
                    String type = typeAndConstraints[0];
                    Column column = getProcessedColumn(typeAndConstraints[1], name, type);
                    columns.add(column);
                }
            }
            return columns;
        } catch (IOException e) {
            System.out.println("Failed to read columns from table file for path: " + tableFile);
            return List.of();
        }
    }

    /**
     * Provides the SQL query for creating table
     * @param dbName Name of the database
     * @param tableName Name of the table
     * @return String representing SQL query for creating a table
     */
    private static String getCreateTableSQLQuery(String dbName, String tableName) {
        List<Column> columns = getTableColumns(dbName, tableName);
        StringBuilder sql = new StringBuilder("\nCREATE TABLE ").append(tableName).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            sql.append(column.name()).append(" ").append(column.type());
            if (!column.constraints().isEmpty()) {
                sql.append(" ").append(String.join(", ", column.constraints()));
            }
            if (column.foreignKeyTable() != null && column.foreignKeyColumn() != null) {
                sql.append(" foreign_key ").append(column.foreignKeyTable())
                        .append(".").append(column.foreignKeyColumn());
            }
            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(");");
        return sql.toString();
    }

    /**
     * Provides the SQL query for inserting row
     * @param tableName Name of the table
     * @param tableColumns List of all columns in table
     * @param row Row data map of Column to its value
     * @return String representing SQL query for inserting a row
     */
    private static String getInsertRowSQLQuery(String tableName, List<Column> tableColumns, Map<String, String> row) {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder values = new StringBuilder(" VALUES (");

        int index = 0;
        for (Map.Entry<String, String> entry : row.entrySet()) {
            sql.append(entry.getKey());
            Column tableColumn = tableColumns.stream()
                    .filter(column -> Objects.equals(column.name(), entry.getKey()))
                    .findFirst()
                    .orElse(null);
            if (tableColumn == null)
                throw new RuntimeException("Column not present for name: " + entry.getKey());

            if (entry.getValue().equals("null")) {
                values.append("null");
            } else if (tableColumn.type().equalsIgnoreCase("string")) {
                values.append("'").append(entry.getValue()).append("'");
            } else {
                values.append(entry.getValue());
            }

            if (index < row.size() - 1) {
                sql.append(", ");
                values.append(", ");
            }
            index++;
        }
        sql.append(")").append(values).append(");");
        return sql.toString();
    }

    /**
     * Writes the provided content to the file
     * @param filePath Path of the file
     * @param content String representing the content
     */
    public static void writeToFile(String filePath, String content) {
        File file = new File(filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        } catch (IOException e) {
            System.out.println("Failed to write to file: " + filePath + " - " + e.getMessage());
        }
    }

    /**
     * Provides the column instance after processing input
     * @param constraintsAndForeignKeys String representing column constraints and foreign key information
     * @param name Name of the column
     * @param type Data type for the column
     * @return Column instance
     */
    private static Column getProcessedColumn(String constraintsAndForeignKeys, String name, String type) {
        List<String> constraints = new ArrayList<>();
        String foreignKeyTable = null;
        String foreignKeyColumn = null;

        if (constraintsAndForeignKeys.contains("[") && constraintsAndForeignKeys.contains("]")) {
            String constraintsString = constraintsAndForeignKeys.substring(
                    constraintsAndForeignKeys.indexOf("[") + 1,
                    constraintsAndForeignKeys.indexOf("]")
            );
            if (!constraintsString.isBlank())
                constraints = Arrays.asList(constraintsString.split(", "));
        }
        if (constraintsAndForeignKeys.contains("(") && constraintsAndForeignKeys.contains(")")) {
            String foreignKeysString = constraintsAndForeignKeys.substring(
                    constraintsAndForeignKeys.indexOf("(") + 1,
                    constraintsAndForeignKeys.indexOf(")")
            );
            String[] tableNameAndColumn = foreignKeysString.split(": ");
            foreignKeyTable = tableNameAndColumn[0];
            foreignKeyColumn = tableNameAndColumn[1];
        }
        return new Column(name, type, constraints, foreignKeyTable, foreignKeyColumn);
    }
}
