package ru.redstonemaster.web.notification;

public record NotificationView(
		NotificationType type,
		String title,
		String message,
		String actionUrl,
		String actionLabel
) {
}
