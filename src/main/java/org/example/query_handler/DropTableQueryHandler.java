package org.example.query_handler;

import org.example.database.Database;
import org.example.manager.DatabaseManager;
import org.example.manager.LogManager;

import java.time.LocalDateTime;

public class DropTableQueryHandler implements QueryHandler {

    @Override
    public void handle(String query) {
        Database currentDatabase = DatabaseManager.getCurrentDatabase();
        currentDatabase.dropTable(query);
        LogManager.logEvent("table deleted", "a table was deleted", LocalDateTime.now());
    }
}

