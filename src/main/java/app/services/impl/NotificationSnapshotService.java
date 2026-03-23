package app.services.impl;

import app.dtos.notification.AdminNotificationSnapshotDTO;
import app.persistence.daos.interfaces.readers.IDishSuggestionReader;
import app.persistence.daos.interfaces.readers.IIngredientRequestReader;
import app.services.INotificationSnapshotService;

public class NotificationSnapshotService implements INotificationSnapshotService
{
    private final IDishSuggestionReader dishSuggestionReader;
    private final IIngredientRequestReader ingredientRequestReader;

    public NotificationSnapshotService(IDishSuggestionReader dishSuggestionReader, IIngredientRequestReader ingredientRequestReader)
    {
        this.dishSuggestionReader = dishSuggestionReader;
        this.ingredientRequestReader = ingredientRequestReader;
    }

    @Override
    public AdminNotificationSnapshotDTO getPendingSnapshot()
    {
        int pendingSuggestions = dishSuggestionReader.getPendingSuggestionsCount();
        int pendingRequests = ingredientRequestReader.getPendingRequestCount();

        return new AdminNotificationSnapshotDTO(
            pendingSuggestions,
            pendingRequests,
            pendingSuggestions + pendingRequests
        );
    }
}
