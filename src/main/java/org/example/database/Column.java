package org.example.database;

import java.util.Arrays;
import java.util.List;

/**
 * Data holder class for Column of table
 * @param name Name of the column
 * @param type Type of data stored in the column
 * @param constraints Specific constraints applied to the column
 * @param foreignKeyTable Foreign key table name
 * @param foreignKeyColumn Foreign key column name
 */
public record Column(
        String name,
        String type,
        List<String> constraints,
        String foreignKeyTable,
        String foreignKeyColumn
) {

    public enum Type {
        INT("int"),
        STRING("string"),
        DOUBLE("double");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        static List<String> getValues() {
            return Arrays.stream(values()).map(Type::getName).toList();
        }
    }

    public enum Constraint {
        PRIMARY_KEY("primary_key"),
        NON_NULL("non_null"),
        AUTO_INCREMENT("auto_increment"),
        UNIQUE("unique");

        private final String name;

        Constraint(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        static List<String> getValues() {
            return Arrays.stream(values()).map(Constraint::getName).toList();
        }
    }

    /**
     * Checks if the column type is valid or not
     * @param type Type of the data stored in column
     * @return True if type is valid, False otherwise
     */
    public static boolean isValidType(String type) {
        return Arrays.stream(Type.values())
                .map(Type::getName).toList().contains(type.toLowerCase());
    }

    /**
     * Checks if the column constraint is valid or not
     * @param constraint Constraint given to the column
     * @return True if constraint is valid, False otherwise
     */
    public static boolean isValidConstraint(String constraint) {
        return Arrays.stream(Constraint.values())
                .map(Constraint::getName).toList().contains(constraint.toLowerCase());
    }
}
