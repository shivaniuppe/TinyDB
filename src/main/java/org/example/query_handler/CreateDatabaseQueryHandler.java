package org.example.query_handler;

import org.example.enums.QueryType;
import org.example.manager.DatabaseManager;
import org.example.manager.LogManager;

import java.time.LocalDateTime;

public class CreateDatabaseQueryHandler implements QueryHandler {
    @Override
    public void handle(String query) {
        if (!isValidCreateDatabaseQuery(query)) {
            throw new RuntimeException("Invalid CREATE DATABASE query");
        }
        String databaseName = getDabaseNameFromQuery(query);
        DatabaseManager.createDatabase(databaseName);
        LogManager.logEvent("database created", String.format("new database was created: %s", databaseName), LocalDateTime.now());
    }

    // Region: private helper methods
    // This method returns true if the query is valid create database query, otherwise false.
    private static boolean isValidCreateDatabaseQuery(String query) {
        if (!query.toUpperCase().startsWith(QueryType.CREATE_DATABASE.getPrefix()))
            return false;
        String[] queryData = query.split("\\s+");
        return queryData.length == 3;
    }

    // This method assumes the incoming query to be a valid query
    // and returns the database name from the query.
    private static String getDabaseNameFromQuery(String query) {
        String databaseName = query.split("\\s+")[2];
        return databaseName;
    }
    // End region
}
