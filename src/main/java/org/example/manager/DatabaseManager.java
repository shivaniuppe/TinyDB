package org.example.manager;

import org.example.database.Database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class will be responsible for all management related tasks of Databases
 * in the system
 */
public class DatabaseManager {
    private static final Map<String, Database> databases = new HashMap<>();
    private static Database currentDatabase = null;

    static {
        FileManager.loadDatabases().forEach(db -> databases.put(db.getName(), db));
    }

    /**
     * adds a Database with name `databaseName` in the stored `databases`
     * if it does not already exists. if a Database with the
     * given name exists, throws an Exception.
     * @param databaseName the name of database to be added.
     */
    public static void createDatabase(String databaseName) {
        if (databases.containsKey(databaseName)) {
            throw new RuntimeException("Database already exists: " + databaseName);
        }
        databases.put(databaseName, new Database(databaseName));
        FileManager.createDatabaseDirectory(databaseName);
        FileManager.saveDatabases(databases.keySet().stream().toList());
        System.out.println("Database created: " + databaseName);
    }

    /**
     * changes the `currentDatabase` to the Database with name `databaseName`
     * if it exists in `databases`. If it does not, throws an Exception.
     * @param databaseName the name of Database to be used.
     */
    public static void useDatabase(String databaseName) {
        if (!databases.containsKey(databaseName)) {
            throw new RuntimeException("Database does not exist: " + databaseName);
        }
        currentDatabase = databases.get(databaseName);
        System.out.println("Using database: " + databaseName);
    }

    /**
     * method to receive the currently used database.
     * @return `currentDatabase` if it is not null, else
     * throws an Exception.
     */
    public static Database getCurrentDatabase() {
        if (!isAnyDatabaseInUse()) {
            throw new RuntimeException("No database selected");
        }
        return currentDatabase;
    }

    /**
     * Get all tha databases added
     * @return List of all databases added
     */
    public static List<Database> getDatabases() {
        return databases.values().stream().toList();
    }

    /**
     * Should be called to know if any Database is in use or not.
     * @return True if `currentDatabase` is not null, else false
     */
    private static boolean isAnyDatabaseInUse() {
        return currentDatabase != null;
    }
}
