package org.example.query_handler;

import org.example.enums.QueryType;
import org.example.manager.DatabaseManager;
import org.example.manager.LogManager;

import java.time.LocalDateTime;

public class UseDatabaseQueryHandler implements QueryHandler {
    @Override
    public void handle(String query) {
        if (!isValidUseDatabaseQuery(query)) {
            throw new RuntimeException("Invalid USE DATABASE query");
        }
        String databaseName = getDatabaseNameFromQuery(query);
        DatabaseManager.useDatabase(databaseName);
        LogManager.logEvent("database changed", String.format("database changed to: %s", databaseName), LocalDateTime.now());
    }

    // Region: private helper methods
    // This method returns true if the query is valid use database query, otherwise false.
    private static boolean isValidUseDatabaseQuery(String query) {
        if (!query.toUpperCase().startsWith(QueryType.USE_DATABASE.getPrefix()))
            return false;
        String[] queryData = query.split("\\s+");
        return queryData.length == 2;
    }

    // This method assumes the incoming query to be a valid query
    // and returns the database name from the query.
    private static String getDatabaseNameFromQuery(String query) {
        String databaseName = query.split("\\s+")[1];
        return databaseName;
    }
    // End region
}
