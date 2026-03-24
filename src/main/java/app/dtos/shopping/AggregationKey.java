package app.dtos.shopping;

import app.enums.Unit;

public record AggregationKey(
    String normalizedName,
    Unit unit
)
{
}
