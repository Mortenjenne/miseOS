package app.services;

import app.dtos.notification.AdminNotificationSnapshotDTO;
import app.exceptions.UnauthorizedActionException;
import app.persistence.daos.interfaces.readers.IDishSuggestionReader;
import app.persistence.daos.interfaces.readers.IIngredientRequestReader;
import app.persistence.daos.interfaces.readers.IUserReader;
import app.persistence.entities.User;
import app.utils.ValidationUtil;

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
