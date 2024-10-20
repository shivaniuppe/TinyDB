package org.example.manager;

import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * This class is responsible for logging various types
 * of events, queries, and general information about the
 * database state and operations.
 */
public class LogManager {
    private static final String GENERAL_LOGS_FILE = "Databases/general_logs.json";
    private static final String EVENT_LOGS_FILE = "Databases/event_logs.json";
    private static final String QUERY_LOGS_FILE = "Databases/query_logs.json";

    /**
     * Logs general information such as query execution times and database state.
     *
     * @param action        A description of the action performed.
     * @param executionTime The time taken to execute the action in milliseconds.
     * @param dbState       A map containing the state of the database (i.e. tables and their number of records).
     */
    public static void logGeneral(String action, long executionTime, Map<String, Long> dbState) {
        JSONObject logEntry = new JSONObject();
        logEntry.put("action", action);
        logEntry.put("executionTime", executionTime);
        logEntry.put("dbState", dbState);

        appendToFile(GENERAL_LOGS_FILE, logEntry);
    }

    /**
     * Logs events such as changes to the database, transaction events, and errors.
     *
     * @param eventType A description of the type of event.
     * @param details   Additional details about the event.
     */
    public static void logEvent(String eventType, String details, LocalDateTime timestamp) {
        JSONObject logEntry = new JSONObject();
        logEntry.put("eventType", eventType);
        logEntry.put("details", details);
        logEntry.put("timestamp", timestamp);

        appendToFile(EVENT_LOGS_FILE, logEntry);
    }

    /**
     * Logs user queries, execution time, and their submission timestamps.
     *
     * @param query     The user query string.
     * @param timestamp The time the query was submitted, in milliseconds since epoch.
     */
    public static void logQuery(String query, long executionTime, LocalDateTime timestamp) {
        JSONObject logEntry = new JSONObject();
        logEntry.put("query", query);
        logEntry.put("execution_time", executionTime);
        logEntry.put("timestamp", timestamp);

        appendToFile(QUERY_LOGS_FILE, logEntry);
    }
    /**
     * Appends a JSON log entry to the specified log file.
     *
     * @param filePath The path of the log file.
     * @param logEntry The JSON object representing the log entry.
     */
    private static void appendToFile(String filePath, JSONObject logEntry) {
        try (FileWriter file = new FileWriter(filePath, true)) {
            file.write(logEntry.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}