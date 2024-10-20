package org.example.database;

import org.example.database.Column.Constraint;
import org.example.database.Column.Type;
import org.example.enums.QueryType;
import org.example.manager.FileManager;
import org.example.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.example.manager.FileManager.DATABASES_DIRECTORY;

/**
 * Represents a database that stores all tables
 */
public class Database {
    private final String name;
    private final List<Table> tables = new ArrayList<>();

    public Database(String name) {
        this.name = name;
    }

    // Region: public methods

    /**
     * Provide the name of the database
     *
     * @return String denoting name of the database
     */
    public String getName() {
        return name;
    }

    /**
     * Provide all the tables of database
     *
     * @return List of all tables added to the database
     */
    public List<Table> getTables() {
        return tables;
    }

    /**
     * Create a table
     *
     * @param query MySQL query for creating a table
     */
    public void createTable(String query) {
        if (!isCreateTableQueryValid(query))
            throw new RuntimeException("Invalid query");

        String tableName = query.split("\\s+")[2];
        if (!validateTableName(tableName))
            throw new RuntimeException("Either the table exists or table name is invalid.");

        String columnsData = getStringInsideBrackets(query);
        List<Column> columns = getColumns(columnsData);
        if (columns == null || columns.isEmpty())
            throw new RuntimeException("Invalid column definition: " + columnsData);

        tables.add(new Table(name, tableName, columns));
        FileManager.createNecessaryTableFiles(name, tableName, columns);
        System.out.println("Table created: " + tableName);
    }

    /**
     * Method to get Tables along with their number of records present in it
     *
     * @return A HashMap of Table to Long integer
     */
    public Map<String, Long> getDatabaseMap() {
        Map<String, Long> dbMap = new HashMap<>();
        List<Table> tables = getTables();
        for (Table table: tables) {
            dbMap.put(table.getName(), table.getNumberOfRows());
        }

        return dbMap;
    }

    /**
     * Insert a row in a table
     *
     * @param query MySQL query to write a row in a table
     */
    public void insertIntoTable(String query) {
        if (!isInsertQueryValid(query))
            throw new RuntimeException("Invalid query");

        String tableName = query.split("\\s+")[2];
        Table table = getTable(tableName);
        if (table == null)
            throw new RuntimeException("Table not found: " + tableName);

        String columnsData = getStringInsideBrackets(query.substring(0, query.toUpperCase().indexOf("VALUES")));
        if (columnsData.isBlank())
            columnsData = table.getColumnsString();

        String valuesData = getStringInsideBrackets(query.substring(query.toUpperCase().indexOf("VALUES") + 6).trim());
        if (valuesData.isBlank())
            throw new RuntimeException("Invalid values provided");

        table.insertRow(getRow(table, columnsData, valuesData));
    }

    /**
     * Read values from a table
     *
     * @param query MySQL query to read data from a table
     */
    public void selectFromTable(String query) {
        if (!isSelectQueryValid(query))
            throw new RuntimeException("Invalid query");

        String upperQuery = query.toUpperCase();
        String columns = query.substring(upperQuery.indexOf("SELECT") + 6, upperQuery.indexOf("FROM")).trim();
        String queryAfterFrom = query.substring(upperQuery.indexOf("FROM") + 4).trim();

        String tableName = queryAfterFrom.split("\\s+")[0];
        Table table = getTable(tableName);
        if (table == null)
            throw new RuntimeException("Table not found: " + tableName);

        List<String> columnsToSelect = columns.equals("*") ? null : Arrays.asList(columns.split("\\s*,\\s*"));
        table.selectRows(query, columnsToSelect);
    }

    /**
     * Update values from a table
     *
     * @param query MySQL query to update data from a table
     */
    public void updateTable(String query) {
        if (!isUpdateQueryValid(query))
            throw new RuntimeException("Invalid query");

        String tableName = query.split("\\s+")[1];
        Table table = getTable(tableName);
        if (table == null)
            throw new RuntimeException("Table not found: " + tableName);

        String upperQuery = query.toUpperCase();
        String setPart = query.substring(upperQuery.indexOf("SET") + 3, upperQuery.indexOf("WHERE")).trim();
        String conditionPart = query.substring(upperQuery.indexOf("WHERE") + 5).trim();
        table.updateRows(setPart, conditionPart);
    }

    /**
     * Delete a row from a table
     *
     * @param query MySQL query to delete data from a table
     */
    public void deleteFromTable(String query) {
        if (!isDeleteQueryValid(query))
            throw new RuntimeException("Invalid query");

        String tableName = query.split("\\s+")[2];
        Table table = getTable(tableName);
        if (table == null)
            throw new RuntimeException("Table not found: " + tableName);

        String upperQuery = query.toUpperCase();
        String conditionPart = query.substring(upperQuery.indexOf("WHERE") + 5).trim();
        table.deleteRows(conditionPart);
    }

    /**
     * Delete the table
     *
     * @param query MySQL query to delete table
     */
    public void dropTable(String query) {
        if (!isDropQueryValid(query))
            throw new RuntimeException("Invalid query");

        String tableName = query.split("\\s+")[2];
        Table table = getTable(tableName);
        if (table == null)
            throw new RuntimeException("Table not found: " + tableName);

        tables.remove(table);
        File tableFile = new File(DATABASES_DIRECTORY + File.separator + name + File.separator + tableName + ".txt");
        File tableMetadataFile = new File(DATABASES_DIRECTORY + File.separator + name + File.separator + tableName + "_metadata.txt");
        boolean tableFileDeleted = tableFile.delete();
        boolean tableMetadataFileDeleted = tableMetadataFile.delete();
        if (tableFileDeleted && tableMetadataFileDeleted) {
            System.out.println("Table dropped: " + tableName);
        } else {
            System.out.println("Failed to drop table: " + tableName);
        }
    }

    /**
     * Add a predefined table to database
     *
     * @param table Table instance to add
     */
    public void addTable(Table table) {
        tables.add(table);
    }

    /**
     * Provides the Table for the requested table name
     *
     * @param tableName Name of the table
     * @return Table instance having name as tableName
     */
    public Table getTable(String tableName) {
        return tables.stream()
                .filter((table) -> Objects.equals(table.getName(), tableName))
                .findFirst()
                .orElse(null);
    }
    // End region

    // Region: private methods

    /**
     * Verify if provided "create table" query is valid or not
     *
     * @param query MySQL query to create table
     * @return True if query is valid, otherwise False
     */
    private boolean isCreateTableQueryValid(String query) {
        if (!query.toUpperCase().startsWith(QueryType.CREATE_TABLE.getPrefix()))
            return false;
        String[] queryData = query.split("\\s+");
        return queryData.length > 3;
    }

    /**
     * Verify if table name is valid or not
     *
     * @param name Name of the table to verify
     * @return True if valid, otherwise False
     */
    private boolean validateTableName(String name) {
        if (getTable(name) != null)
            return false;
        return !name.contains("(") && !name.contains(")");
    }

    /**
     * Provides all columns from the input provided
     *
     * @param columnsData All necessary data to create a column of a table, extracted from query
     * @return List of Column instances created based on input provided
     */
    private List<Column> getColumns(String columnsData) {
        List<Column> columns = new ArrayList<>();
        String[] columnDefs = columnsData.split(",");
        for (String columnDef : columnDefs) {
            String[] parts = columnDef.trim().split("\\s+");
            if (parts.length < 2)
                return null;
            String columnName = parts[0].toLowerCase();
            String columnType = parts[1].toLowerCase();
            if (!Column.isValidType(columnType))
                throw new RuntimeException("Invalid column type: " + columnType +
                        ". Allowed types: " + Type.getValues());

            List<String> constraints = new ArrayList<>();
            String foreignKeyTable = null;
            String foreignKeyColumn = null;

            if (parts.length > 2) {
                for (int index = 2; index < parts.length; index++) {
                    if (parts[index].equalsIgnoreCase("foreign_key")) {
                        if (index + 1 < parts.length && parts[index + 1].contains(".")) {
                            String[] foreignKeyParts = parts[index + 1].split("\\.");
                            foreignKeyTable = foreignKeyParts[0];
                            foreignKeyColumn = foreignKeyParts[1];
                            Table table = getTable(foreignKeyTable);

                            if (table == null)
                                throw new RuntimeException("Invalid table in foreign key definition");
                            if (!table.hasColumn(foreignKeyColumn))
                                throw new RuntimeException("Invalid column in foreign key definition");

                            // Skip the foreign key column part
                            index++;
                        } else {
                            throw new RuntimeException("Invalid foreign key definition");
                        }
                    } else {
                        String constraint = parts[index];
                        if (!Column.isValidConstraint(constraint))
                            throw new RuntimeException("Invalid constraint: " + constraint +
                                    ". Allowed constraints: " + Constraint.getValues());
                        constraints.add(constraint.toLowerCase());
                    }
                }
            }

            if (constraints.contains(Constraint.AUTO_INCREMENT.getName()) && !columnType.equals(Type.INT.getName()))
                throw new RuntimeException("Auto increment constraint can only be applied to int types");
            Column column = new Column(columnName, columnType, constraints, foreignKeyTable, foreignKeyColumn);
            columns.add(column);
        }
        return columns;
    }

    /**
     * Verify if provided "insert into" query is valid or not
     *
     * @param query MySQL query to write data to a table
     * @return True if query is valid, otherwise False
     */
    private boolean isInsertQueryValid(String query) {
        if (!query.toUpperCase().startsWith(QueryType.INSERT_INTO_TABLE.getPrefix()))
            return false;
        String[] queryData = query.split("\\s+");
        if (queryData.length <= 3)
            return false;
        return query.toUpperCase().contains("VALUES");
    }

    /**
     * Provides a row from provided columns and their values
     * @param table Table instance to get row
     * @param columnsData String representing columns names, extracted from query
     * @param valuesData  String representing values, extracted from query
     * @return Map of ColumnName to ColumnData, indicating a row
     */
    private Map<String, String> getRow(Table table, String columnsData, String valuesData) {
        Map<String, String> row = new LinkedHashMap<>();
        List<Column> tableColumns = table.getColumns();
        List<String> columns = Arrays.stream(columnsData.split(",")).map(String::trim).toList();
        List<String> values = getValues(valuesData);
        if (columns.size() != values.size())
            throw new IllegalArgumentException("Number of columns and values do not match");

        tableColumns.forEach(column -> {
            String name = column.name();
            List<String> constraints = column.constraints();

            if (constraints.contains(Constraint.AUTO_INCREMENT.getName())) {
                row.put(name, table.getAutoIncrementValueFor(name));
            } else {
                if (columns.contains(name)) {
                    String value = values.get(columns.indexOf(name)).trim();
                    if (table.isValidColumnValue(name, value)) {
                        row.put(name, value);
                    } else {
                        throw new RuntimeException("Invalid value: " + value + " for column: " + column);
                    }
                } else {
                    boolean hasNonNullConstraint = constraints.contains(Constraint.NON_NULL.getName());
                    boolean hasPrimaryKeyConstraint = constraints.contains(Constraint.PRIMARY_KEY.getName());
                    if (hasNonNullConstraint || hasPrimaryKeyConstraint) {
                        throw new RuntimeException("Column " + name + " must have a value");
                    } else {
                        row.put(name, null);
                    }
                }
            }
        });
        return row;
    }

    /**
     * Provides list of values extracted from the value's part from query
     *
     * @param valuesPart String representing values to be added to specific column
     * @return List of String representing values extracted from the input
     */
    private List<String> getValues(String valuesPart) {
        List<String> values = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"([^\"]*)\"|'([^']*)'|([^,]+)").matcher(valuesPart);
        while (matcher.find()) {
            // First group ("([^\"]*)") captures double-quoted string without the quotes.
            // Second group ('([^']*)') captures single-quoted string without the quotes.
            // Third group (([^,]+)) captures unquoted value.
            IntStream.range(1, 4).forEach((index) -> {
                if (matcher.group(index) != null) {
                    String value = StringUtils.getStringWithoutSurroundingQuotes(matcher.group(index).trim());
                    values.add(value);
                }
            });
        }
        return values;
    }

    /**
     * Verify if provided "select (row/rows/*)" query is valid or not
     *
     * @param query MySQL query to read data from a table
     * @return True if query is valid, otherwise False
     */
    private boolean isSelectQueryValid(String query) {
        if (!query.toUpperCase().startsWith(QueryType.SELECT_FROM_TABLE.getPrefix()))
            return false;
        if (!query.toUpperCase().contains("FROM"))
            return false;
        String queryAfterFrom = query.substring(query.toUpperCase().indexOf("FROM") + 4).trim();
        return !queryAfterFrom.isEmpty();
    }

    /**
     * Utility method to get substring inside brackets
     *
     * @param query Query containing opening and closing circular brackets
     * @return Sub string inside brackets extracted from input
     * Example:
     * <pre>{@code
     * String input = "(id, name)"
     * String columnsData = getStringInsideBrackets(input);
     * // columnsData will be "id, name"
     * }</pre>
     */
    private String getStringInsideBrackets(String query) {
        if (!query.contains("(") || !query.contains(")"))
            return "";
        return query.substring(query.indexOf("(") + 1, query.indexOf(")"));
    }

    /**
     * Verify if the provided query is valid in terms of update value query
     *
     * @param query MySQL query to update data from a table
     * @return True if query is valid, otherwise False
     */
    private boolean isUpdateQueryValid(String query) {
        if (!query.toUpperCase().startsWith(QueryType.UPDATE_TABLE.getPrefix()))
            return false;
        return query.toUpperCase().contains("SET") && query.toUpperCase().contains("WHERE");
    }

    /**
     * Verify if the provided query is valid in terms of delete row query
     *
     * @param query MySQL query to delete data from a table
     * @return True if query is valid, otherwise False
     */
    private boolean isDeleteQueryValid(String query) {
        if (!query.toUpperCase().startsWith(QueryType.DELETE_FROM_TABLE.getPrefix()))
            return false;
        return query.toUpperCase().contains("WHERE");
    }

    /**
     * Verify if the provided query is valid in terms of drop table query
     *
     * @param query MySQL query to delete table
     * @return True if query is valid, otherwise False
     */
    private boolean isDropQueryValid(String query) {
        return query.toUpperCase().startsWith(QueryType.DROP_TABLE.getPrefix());
    }
    // End region
}
