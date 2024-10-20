package org.example.manager;

import org.example.database.Table;

public class TransactionManager {

    private static Boolean shouldAutoCommit = true;
    private static Boolean isTransactionInProgress = false;
    private static Boolean autoCommitStatusBeforeTransaction = true;

    /**
     * Set the auto commit status of the program
     * @param shouldAutoCommit True if auto commit, otherwise false
     */
    public static void setShouldAutoCommit(Boolean shouldAutoCommit) {
        TransactionManager.shouldAutoCommit = shouldAutoCommit;
    }

    /**
     * Provides the AUTOCOMMIT status of the program
     * @return Boolean representing if auto commit is on or off
     */
    public static Boolean getShouldAutoCommit() {
        return shouldAutoCommit;
    }

    /**
     * Provides the status of the transaction
     * @return Boolean representing if transaction is in progress
     */
    public static Boolean getIsTransactionInProgress() {
        return isTransactionInProgress;
    }

    /**
     * Start a transaction
     * NOTE: Nested transactions are not supported so previously ongoing transaction
     * will be committed if any new transaction has started
     */
    public static void startTransaction() {
        DatabaseManager.getDatabases().forEach(database ->
                database.getTables().forEach(table -> {
                    table.addBufferDataToFile();
                    table.clearBufferData();
                })
        );
        autoCommitStatusBeforeTransaction = shouldAutoCommit;
        isTransactionInProgress = true;
        setShouldAutoCommit(false);
    }

    /**
     * Commits transaction by adding/updating all data from buffer to file
     */
    public static void commitTransaction() {
        DatabaseManager.getDatabases().forEach(database ->
                database.getTables().forEach(Table::addBufferDataToFile)
        );
        isTransactionInProgress = false;
        setShouldAutoCommit(autoCommitStatusBeforeTransaction);
    }

    /**
     * Rolls back transaction by removing all data from buffer
     */
    public static void rollbackTransaction() {
        DatabaseManager.getDatabases().forEach(database ->
                database.getTables().forEach(Table::clearBufferData)
        );
        isTransactionInProgress = false;
        setShouldAutoCommit(autoCommitStatusBeforeTransaction);
    }
}
