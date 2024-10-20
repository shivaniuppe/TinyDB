package org.example.query_handler;

import org.example.database.Database;
import org.example.manager.DatabaseManager;
import org.example.manager.LogManager;

import java.util.Map;

public class InsertIntoTableQueryHandler implements QueryHandler {

    @Override
    public void handle(String query) {
        Database currentDatabase = DatabaseManager.getCurrentDatabase();

        long startTime = System.currentTimeMillis();
        currentDatabase.insertIntoTable(query);
        long executionTime = System.currentTimeMillis() - startTime;

        Map<String, Long> dbState = DatabaseManager.getCurrentDatabase().getDatabaseMap();
        LogManager.logGeneral("insert into table executed", executionTime, dbState);
    }
}
