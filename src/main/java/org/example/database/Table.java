package org.example.database;

import org.example.manager.FileManager;
import org.example.manager.TransactionManager;
import org.example.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class Table {
    private static final int MAX_COLUMN_WIDTH = 50;
    private final String databaseName;
    private final String name;
    private final List<Column> columns;
    private final List<Map<String, String>> bufferData = new ArrayList<>();
    private final List<String> operators = List.of("<=", ">=", "!=", "=", "<", ">", " IN ");
    private boolean isBufferDataPopulated = false;

    public Table(String databaseName, String name, List<Column> columns) {
        this.databaseName = databaseName;
        this.name = name;
        this.columns = new ArrayList<>(columns);
    }

    public String getName() {
        return name;
    }

    /**
     * Insert a row in a table
     * @param columnValueMap Map of column name to its value
     */
    public void insertRow(Map<String, String> columnValueMap) {
        Map<String, String> row = new HashMap<>();
        columns.forEach((column) -> {
            String columnName = column.name();
            row.put(columnName, columnValueMap.getOrDefault(columnName, null));
        });
        if (isBufferDataPopulated) {
            bufferData.add(row);
        } else {
            if (TransactionManager.getShouldAutoCommit()) {
                FileManager.writeRowToFile(databaseName, name, row);
            } else {
                bufferData.clear();
                bufferData.addAll(FileManager.getRows(databaseName, name));
                bufferData.add(row);
                isBufferDataPopulated = true;
            }
        }
        System.out.println("Row added successfully.");
    }

    /**
     * Read values from a table
     * @param query Original query to read data from table
     * @param columnsToSelect Columns to select (null to select columns)
     */
    public void selectRows(String query, List<String> columnsToSelect) {
        String upperCaseQuery = query.toUpperCase();
        String condition = upperCaseQuery.contains("WHERE")
                ? query.substring(upperCaseQuery.indexOf("WHERE") + 5).trim()
                : null;
        List<Map<String, String>> currentRows = isBufferDataPopulated
                ? bufferData
                : FileManager.getRows(databaseName, name);
        List<Map<String, String>> filteredRows = currentRows.stream()
                .filter(row -> condition == null || evaluateCondition(row, condition))
                .toList();
        printTable(filteredRows, columnsToSelect);
    }

    /**
     * returns the number of rows present in the table
     *
     * @return a long representing the number of rows
     */
    public long getNumberOfRows() {
        List<Map<String, String>> rows = isBufferDataPopulated
                ? bufferData
                : FileManager.getRows(databaseName, name);
        long numRows = rows.size();
        return numRows;
    }

    /**
     * Updated specific row with the provided data
     * @param setPart Substring between "SET" and "WHERE" of original query
     * @param conditionPart Substring after "WHERE" of original query
     */
    public void updateRows(String setPart, String conditionPart) {
        String[] setParts = setPart.split("=", 2);
        if (setParts.length != 2) {
            System.out.println("Invalid SET clause.");
            return;
        }

        // Update rows
        String columnToUpdate = setParts[0].trim();
        String newValue = StringUtils.getStringWithoutSurroundingQuotes(setParts[1].trim());
        AtomicInteger rowsAffected = new AtomicInteger();
        List<Map<String, String>> currentRows = isBufferDataPopulated
                ? bufferData
                : FileManager.getRows(databaseName, name);
        List<Map<String, String>> updatedRows = currentRows.stream()
                .peek(row -> {
                    if (evaluateCondition(row, conditionPart)) {
                        row.put(columnToUpdate, newValue);
                        rowsAffected.getAndIncrement();
                    }
                })
                .toList();

        // Save rows
        saveRowData(updatedRows);
        System.out.println(rowsAffected.get() + " row(s) affected.");
    }

    /**
     * Delete row(s) from the table
     * @param conditionPart Substring after "WHERE" of original query
     */
    public void deleteRows(String conditionPart) {
        // Delete rows
        List<Map<String, String>> currentRows = isBufferDataPopulated
                ? bufferData
                : FileManager.getRows(databaseName, name);
        List<Map<String, String>> updatedRows = currentRows.stream()
                .filter(row -> !evaluateCondition(row, conditionPart))
                .toList();

        // Save rows
        saveRowData(updatedRows);
        System.out.println((currentRows.size() - updatedRows.size()) + " row(s) deleted successfully.");
    }

    /**
     * Add all the buffer data to the file
     */
    public void addBufferDataToFile() {
        if (isBufferDataPopulated)
            FileManager.writeRowsToFile(databaseName, name, bufferData);
        isBufferDataPopulated = false;
    }

    /**
     * Removes all the buffer data
     */
    public void clearBufferData() {
        bufferData.clear();
        isBufferDataPopulated = false;
    }

    /**
     * Check if table has specific column name added
     * @param columnName Name of the column
     * @return True if column exists, False otherwise
     */
    public boolean hasColumn(String columnName) {
        return columns.stream().anyMatch(column -> Objects.equals(column.name(), columnName));
    }

    /**
     * Provides columns string separated by comma
     * @return String representing all columns separated by comma
     */
    public String getColumnsString() {
        return columns.stream()
                .map(Column::name)
                .toList()
                .toString()
                .replace("[", "")
                .replace("]", "")
                .trim();
    }

    /**
     * Validates the value provided for the column
     * @param columnName Name of the column
     * @param value Value provided to column
     * @return True if value is valid, False otherwise
     */
    public boolean isValidColumnValue(String columnName, String value) {
        Optional<Column> column = columns.stream().filter(col -> Objects.equals(col.name(), columnName)).findFirst();
        if (column.isEmpty())
            return false;

        List<String> constraints = column.get().constraints();
        boolean hasPrimaryKeyConstraint = constraints.contains(Column.Constraint.PRIMARY_KEY.getName());
        boolean hasUniqueConstraint = constraints.contains(Column.Constraint.UNIQUE.getName());
        boolean hasNonNullConstraint = constraints.contains(Column.Constraint.NON_NULL.getName());
        boolean isValid = true;

        if (hasNonNullConstraint) {
            isValid = value != null && !Objects.equals(value, "null");
            if (!isValid)
                System.out.println(columnName + " has non_null constraint so it must not be null.");
        }
        if (hasPrimaryKeyConstraint || hasUniqueConstraint) {
            isValid = isValid && !isValueAlreadyAdded(columnName, value);
            if (!isValid)
                System.out.println(columnName + " has primary_key/unique constraint so it must have unique value.");
        }
        return isValid;
    }

    /**
     * Provides the incremented value from last value added
     * @param column Name of the column
     * @return String representing incremented value
     */
    public String getAutoIncrementValueFor(String column) {
        List<Map<String, String>> rows = FileManager.getRows(databaseName, name);
        List<Integer> values = rows.stream().map(row -> row.get(column)).map(Integer::parseInt).toList();
        if (values.isEmpty())
            return "1";
        return Integer.toString((Collections.max(values) + 1));
    }

    /**
     * Provides all the columns inside this table
     * @return List of Columns
     */
    public List<Column> getColumns() {
        return columns;
    }

    public Column getColumn(String columnName) {
        return columns.stream()
                .filter(column -> Objects.equals(column.name(), columnName))
                .findFirst()
                .orElse(null);
    }

    // End region

    // Region: Private methods

    /**
     * Provides if the provided row satisfies the condition or not
     * @param row Row to check condition for
     * @param condition the condition to check in the provided row
     * @return True if row satisfies the condition, otherwise false
     */
    private boolean evaluateCondition(Map<String, String> row, String condition) {
        String selectedOperator = operators.stream()
                .filter(condition.toUpperCase()::contains)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid condition operator."));

        String[] parts;
        if (selectedOperator.equals(" IN ")) {
            parts = condition.contains(" IN ") ? condition.split(" IN ") : condition.split(" in ");
        } else {
            parts = condition.split(Pattern.quote(selectedOperator), 2);
        }

        String column = parts[0].trim();
        String value = StringUtils.getStringWithoutSurroundingQuotes(parts[1].trim());

        return switch (selectedOperator) {
            case "=" -> Objects.equals(value, row.get(column));
            case "!=" -> !Objects.equals(value, row.get(column));
            case "<=" -> compareValues(row.get(column), value) <= 0;
            case ">=" -> compareValues(row.get(column), value) >= 0;
            case "<" -> compareValues(row.get(column), value) < 0;
            case ">" -> compareValues(row.get(column), value) > 0;
            case " IN " -> {
                List<String> values = Arrays.asList(value.substring(1, value.length() - 1).split(","));
                values = values.stream().map(String::trim).toList();
                yield values.contains(row.get(column));
            }
            default -> throw new IllegalArgumentException("Unsupported condition operator.");
        };
    }

    /**
     * Compares two values and provides integer representing comparison
     * @param value1 First value
     * @param value2 Second value
     * @return Integer representing the comparison.
     *         {@code 0} if {@code value1} is numerically equal to {@code value2};
     *         a value less than {@code 0} if {@code value1} is numerically less than {@code d2};
     *         and a value greater than {@code 0} if {@code value1} is numerically greater than {@code d2}.
     */
    private int compareValues(String value1, String value2) {
        try {
            double double1 = Double.parseDouble(value1);
            double double2 = Double.parseDouble(value2);
            return Double.compare(double1, double2);
        } catch (NumberFormatException e1) {
            try {
                int int1 = Integer.parseInt(value1);
                int int2 = Integer.parseInt(value2);
                return Integer.compare(int1, int2);
            } catch (NumberFormatException e2) {
                return value1.compareTo(value2);
            }
        }
    }

    /**
     * Prints the table with some predefined format
     * @param rows List of all rows to print
     * @param columnsToSelect List of columns to print
     */
    private void printTable(List<Map<String, String>> rows, List<String> columnsToSelect) {
        if (rows.isEmpty()) {
            System.out.println("No rows found.");
            return;
        }

        // If columnsToSelect is null, select all columns
        if (columnsToSelect == null) {
            columnsToSelect = new ArrayList<>(rows.get(0).keySet());
        }

        // Calculate the maximum width of each column
        Map<String, Integer> columnWidths = new HashMap<>();
        for (String column : columnsToSelect) {
            int maxWidth = Math.min(MAX_COLUMN_WIDTH, column.length());
            for (Map<String, String> row : rows) {
                String value = row.get(column);
                if (value != null) {
                    maxWidth = Math.min(MAX_COLUMN_WIDTH, Math.max(maxWidth, value.length()));
                }
            }
            columnWidths.put(column, maxWidth);
        }

        // Print the header row
        for (String column : columnsToSelect) {
            System.out.printf("%-" + columnWidths.get(column) + "s | ", column);
        }
        System.out.println();

        // Print the separator row
        for (String column : columnsToSelect) {
            System.out.print("-".repeat(columnWidths.get(column)) + "-+-");
        }
        System.out.println();

        // Print the data rows
        for (Map<String, String> row : rows) {
            Map<String, List<String>> wrappedRow = wrapRow(row, columnsToSelect, columnWidths);
            int maxLines = wrappedRow.values().stream().mapToInt(List::size).max().orElse(1);
            for (int line = 0; line < maxLines; line++) {
                for (String column : columnsToSelect) {
                    List<String> wrappedLines = wrappedRow.get(column);
                    String value = line < wrappedLines.size() ? wrappedLines.get(line) : "";
                    System.out.printf("%-" + columnWidths.get(column) + "s | ", value);
                }
                System.out.println();
            }
        }
    }

    /**
     * Wraps the row data if it exceeds the width limit
     * @param row All the rows that are to be print
     * @param columnsToSelect All columns that are to be print
     * @param columnWidths Map of column to its max width
     * @return Map of ColumnName to List of values that are wrapped
     */
    private Map<String, List<String>> wrapRow(Map<String, String> row,
                                              List<String> columnsToSelect,
                                              Map<String, Integer> columnWidths) {
        Map<String, List<String>> wrappedRow = new HashMap<>();
        for (String column : columnsToSelect) {
            String value = row.get(column);
            if (value == null)
                value = "";
            int width = columnWidths.get(column);
            List<String> wrappedLines = new ArrayList<>();
            while (value.length() > width) {
                wrappedLines.add(value.substring(0, width));
                value = value.substring(width);
            }
            wrappedLines.add(value);
            wrappedRow.put(column, wrappedLines);
        }
        return wrappedRow;
    }

    /**
     * Saves the data. Save to buffer data if buffer data is populated otherwise save to file
     * @param rows Rows data to save
     */
    private void saveRowData(List<Map<String, String>> rows) {
        if (isBufferDataPopulated) {
            bufferData.clear();
            bufferData.addAll(rows);
            return;
        }
        if (TransactionManager.getShouldAutoCommit()) {
            FileManager.writeRowsToFile(databaseName, name, rows);
            return;
        }
        bufferData.clear();
        bufferData.addAll(rows);
        isBufferDataPopulated = true;
    }

    /**
     * Check if value is already added to table
     * @param column Name of the column
     * @param value Value added to check
     * @return True if value is added, False otherwise
     */
    private boolean isValueAlreadyAdded(String column, String value) {
        List<Map<String, String>> rows = FileManager.getRows(databaseName, name);
        return rows.stream().anyMatch(row -> Objects.equals(row.get(column), value));
    }
    // End region
}
