package org.example.service;

import org.example.database.Column;
import org.example.database.Database;
import org.example.database.Relationship;
import org.example.database.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ERDGenerator {

    private final Database database;
    private final Map<String, List<Relationship>> tableRelationships;

    public ERDGenerator(Database database, Map<String, List<Relationship>> tableRelationships) {
        this.database = database;
        this.tableRelationships = tableRelationships;
    }

    /**
     * this is the main method responsible for generating the ERD
     * with the database and relationships given from constructor
     *
     * @return a String with the needed ERD made from given `database`
     */
    public String generateERD() {
        StringBuilder erd = new StringBuilder();
        erd.append("Entity-Relationship Diagram for Database: ").append(database.getName()).append("\n\n");

        for (Table table : database.getTables()) {
            erd.append("Table: ").append(table.getName()).append("\n");
            erd.append("Columns:\n");

            for (Column column : table.getColumns()) {
                erd.append("  - ").append(column.name()).append(" (").append(column.type()).append(")");
                if (!column.constraints().isEmpty()) {
                    erd.append(" ").append(String.join(", ", column.constraints()));
                }
                erd.append("\n");

                if (column.foreignKeyTable() != null && column.foreignKeyColumn() != null) {
                    erd.append("    Foreign Key -> ").append(column.foreignKeyTable())
                            .append(".").append(column.foreignKeyColumn()).append("\n");

                    Table rightTable = database.getTable(column.foreignKeyTable());
                    if (rightTable == null)
                        continue;
                    Column rightTableColumn = rightTable.getColumn(column.foreignKeyColumn());
                    if (rightTableColumn == null)
                        continue;

                    Relationship relationship = new Relationship(
                            column.name(),
                            Relationship.getCardinalityTypeFromConstraints(column.constraints()),
                            column.foreignKeyColumn(),
                            Relationship.getCardinalityTypeFromConstraints(rightTableColumn.constraints())
                    );
                    tableRelationships.computeIfAbsent(table.getName(), k -> new ArrayList<>())
                            .add(relationship);
                }
            }

            erd.append("\n");
        }

        erd.append("Relationships and Cardinality:\n");
        for (Map.Entry<String, List<Relationship>> tableRelationshipsEntry : tableRelationships.entrySet()) {
            String tableName = tableRelationshipsEntry.getKey();
            List<Relationship> relationships = tableRelationshipsEntry.getValue();

            erd.append("Table: ").append(tableName).append("\n");
            for (Relationship relationship : relationships) {
                erd.append("  - ").append(relationship.leftColumn())
                        .append(" (").append(relationship.leftCardinality()).append(") ")
                        .append("-> ")
                        .append(" (").append(relationship.rightCardinality()).append(") ")
                        .append(relationship.rightColumn()).append("\n");
            }
            erd.append("\n");
        }

        return erd.toString();
    }
}

