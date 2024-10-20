package org.example;

import org.example.enums.QueryType;
import org.example.manager.LogManager;
import org.example.query_handler.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class QueryProcessor {

    private final Map<QueryType, QueryHandler> queryHandlers = new HashMap<>();
    private boolean shouldGoBackToMainMenu = false;

    public QueryProcessor() {
        initializeQueryHandlers();
    }

    public void startAcceptingQueries() {
        System.out.println("""
                Welcome to TinyDb, please start writing queries below.
                """
        );
        Scanner scanner = new Scanner(System.in);
        while (!shouldGoBackToMainMenu) {
            System.out.print("dbms_builder_11 > ");
            String query = scanner.nextLine().trim();

            long startTime = System.currentTimeMillis();
            executeQuery(query);
            long executionTime = System.currentTimeMillis() - startTime;

            LogManager.logQuery(query, executionTime, LocalDateTime.now());
            System.out.println();        }
    }

    public void executeQuery(String query) {
        // Convert query to uppercase before processing to properly handle query starting
        String upperCaseQuery = query.toUpperCase();
        String queryWithoutSemiColon = query;
        // Remove semicolon if added at the end
        if (query.indexOf(";") == query.length() - 1)
            queryWithoutSemiColon = query.substring(0, query.length() - 1);

        // Go back to mein menu on "exit" input
        if (queryWithoutSemiColon.equalsIgnoreCase("exit")) {
            shouldGoBackToMainMenu = true;
            return;
        }

        // Find the right handler and let it handle the query
        for (Map.Entry<QueryType, QueryHandler> entry : queryHandlers.entrySet()) {
            if (upperCaseQuery.startsWith(entry.getKey().getPrefix())) {
                try {
                    entry.getValue().handle(queryWithoutSemiColon);
                } catch (RuntimeException e) {
                    System.out.println("Error: " + e.getMessage());
                }
                return;
            }
        }

        // Couldn't find specific handler for given query
        System.out.println("Invalid query.");
    }

    private void initializeQueryHandlers() {
        for (QueryType queryType : QueryType.values()) {
            QueryHandler handler = switch (queryType) {
                case CREATE_DATABASE -> new CreateDatabaseQueryHandler();
                case CREATE_TABLE -> new CreateTableQueryHandler();
                case USE_DATABASE -> new UseDatabaseQueryHandler();
                case INSERT_INTO_TABLE -> new InsertIntoTableQueryHandler();
                case SELECT_FROM_TABLE -> new SelectFromTableQueryHandler();
                case UPDATE_TABLE -> new UpdateTableQueryHandler();
                case DELETE_FROM_TABLE -> new DeleteFromTableQueryHandler();
                case DROP_TABLE -> new DropTableQueryHandler();
                case SET_AUTO_COMMIT -> new SetAutoCommitQueryHandler();
                case START_TRANSACTION -> new StartTransactionQueryHandler();
                case ROLLBACK -> new RollbackQueryHandler();
                case COMMIT -> new CommitQueryHandler();
            };
            queryHandlers.put(queryType, handler);
        }
    }
}
