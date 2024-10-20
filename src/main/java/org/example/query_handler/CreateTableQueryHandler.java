package org.example.query_handler;

import org.example.database.Database;
import org.example.manager.DatabaseManager;
import org.example.manager.LogManager;

import java.time.LocalDateTime;

public class CreateTableQueryHandler implements QueryHandler {
    @Override
    public void handle(String query) {
        Database currentDatabase = DatabaseManager.getCurrentDatabase();
        currentDatabase.createTable(query);
        LogManager.logEvent("table created", "new table was created", LocalDateTime.now());
    }
}
