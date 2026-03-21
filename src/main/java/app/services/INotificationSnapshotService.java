package app.services;

import app.dtos.notification.AdminNotificationSnapshotDTO;

public interface INotificationSnapshotService
{
    AdminNotificationSnapshotDTO getPendingSnapshot();
}
