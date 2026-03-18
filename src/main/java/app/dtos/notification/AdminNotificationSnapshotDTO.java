package app.dtos.notification;

public record AdminNotificationSnapshotDTO(
    int pendingDishSuggestions,
    int pendingIngredientRequests,
    int totalPending
)
{
}
