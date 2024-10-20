package org.example.query_handler;

import org.example.manager.LogManager;
import org.example.manager.TransactionManager;

import java.time.LocalDateTime;
import java.util.Objects;

public class SetAutoCommitQueryHandler implements QueryHandler {

    @Override
    public void handle(String query) {
        if (!isQueryValid(query))
            return;
        boolean shouldAutoCommit = Objects.equals(query.split("=")[1].trim(), "1");
        TransactionManager.setShouldAutoCommit(shouldAutoCommit);
        System.out.println("Set auto commit status set to: " + shouldAutoCommit);
        LogManager.logEvent("Auto commit modified", String.format("Set auto commit status set to: %s", shouldAutoCommit), LocalDateTime.now());
    }

    /**
     * Checks if the provided query is valid or not
     * @param query Query to check for
     * @return True if query is valid, otherwise false
     */
    private boolean isQueryValid(String query) {
        String[] queryParts = query.split("=");
        String autoCommitQuery = queryParts[1].trim();
        return autoCommitQuery.equals("1") || autoCommitQuery.equals("0");
    }
}
