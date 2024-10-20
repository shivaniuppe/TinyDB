package org.example.query_handler;

import org.example.database.Database;
import org.example.manager.DatabaseManager;
import org.example.manager.LogManager;

import java.time.LocalDateTime;

public class UpdateTableQueryHandler implements QueryHandler {

    @Override
    public void handle(String query) {
        Database currentDatabase = DatabaseManager.getCurrentDatabase();
        currentDatabase.updateTable(query);
        LogManager.logEvent("table updated", "a table was updated", LocalDateTime.now());
    }
}

