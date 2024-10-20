package org.example.query_handler;

import org.example.manager.LogManager;
import org.example.manager.TransactionManager;

import java.time.LocalDateTime;

public class StartTransactionQueryHandler implements QueryHandler {

    @Override
    public void handle(String query) {
        if (!isQueryValid(query))
            return;
        TransactionManager.startTransaction();
        System.out.println("Started transaction");
        LogManager.logEvent("Transaction status modified", "A transaction is started", LocalDateTime.now());
    }

    /**
     * Checks if the provided query is valid or not
     * @param query Query to check for
     * @return True if query is valid, otherwise false
     */
    private boolean isQueryValid(String query) {
        return query.split("\\s+").length == 2;
    }
}
