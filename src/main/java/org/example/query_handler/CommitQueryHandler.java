package org.example.query_handler;

import org.example.manager.LogManager;
import org.example.manager.TransactionManager;

import java.time.LocalDateTime;

public class CommitQueryHandler implements QueryHandler {

    @Override
    public void handle(String query) {
        if (!isQueryValid(query))
            return;
        if (!TransactionManager.getIsTransactionInProgress())
            throw new RuntimeException("No transaction in progress to commit");
        TransactionManager.commitTransaction();
        System.out.println("Changes committed");
        LogManager.logEvent("commit performed", "Changes has been committed", LocalDateTime.now());
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
