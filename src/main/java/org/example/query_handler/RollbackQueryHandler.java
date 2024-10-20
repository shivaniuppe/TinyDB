package org.example.query_handler;

import org.example.manager.LogManager;
import org.example.manager.TransactionManager;

import java.time.LocalDateTime;

public class RollbackQueryHandler implements QueryHandler {

    @Override
    public void handle(String query) {
        if (!isQueryValid(query))
            return;
        if (!TransactionManager.getIsTransactionInProgress())
            throw new RuntimeException("No transaction in progress to rollback");
        TransactionManager.rollbackTransaction();
        System.out.println("Changes rolled back");
        LogManager.logEvent("Rollback performed", "Changes were rolled back", LocalDateTime.now());
    }

    /**
     * Checks if the provided query is valid or not
     * @param query Query to check for
     * @return True if query is valid, otherwise false
     */
    private boolean isQueryValid(String query) {
        return query.split("\\s+").length == 1;
    }
}
