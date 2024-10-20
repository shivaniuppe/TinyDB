package org.example.enums;

/**
 * Enum that holds all the prefix that are possible when writing a query.
 * Useful to discriminate and process query based on the prefix of the query.
 */
public enum QueryType {
    CREATE_DATABASE("CREATE DATABASE"),
    CREATE_TABLE("CREATE TABLE"),
    USE_DATABASE("USE"),
    INSERT_INTO_TABLE("INSERT INTO"),
    SELECT_FROM_TABLE("SELECT"),
    UPDATE_TABLE("UPDATE"),
    DELETE_FROM_TABLE("DELETE FROM"),
    DROP_TABLE("DROP TABLE"),
    SET_AUTO_COMMIT("SET AUTOCOMMIT"),
    START_TRANSACTION("START TRANSACTION"),
    ROLLBACK("ROLLBACK"),
    COMMIT("COMMIT");

    private final String prefix;

    QueryType(String query) {
        this.prefix = query;
    }

    public String getPrefix() {
        return prefix;
    }
}
