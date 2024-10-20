package org.example.database;

import org.example.enums.CardinalityType;

import java.util.List;

import static org.example.enums.CardinalityType.MANY;
import static org.example.enums.CardinalityType.ONE;

public record Relationship(
        String leftColumn,
        CardinalityType leftCardinality,
        String rightColumn,
        CardinalityType rightCardinality
) {

    /**
     * Provides CardinalityType based on the constraints provided
     * @param constraints List of constraints
     * @return CardinalityType instance
     * <li>ONE when constraint contains unique or primary key</li>
     * <li>MANY in other cases</li>
     */
    public static CardinalityType getCardinalityTypeFromConstraints(List<String> constraints) {
        boolean hasUniqueConstraint = constraints.contains(Column.Constraint.UNIQUE.getName());
        boolean hasPrimaryKeyConstraint = constraints.contains(Column.Constraint.PRIMARY_KEY.getName());
        return (hasUniqueConstraint || hasPrimaryKeyConstraint) ? ONE : MANY;
    }
}
