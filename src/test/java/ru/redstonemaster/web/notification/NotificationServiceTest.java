package ru.redstonemaster.web.notification;

import org.junit.jupiter.api.Test;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.user.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationServiceTest {

	private final NotificationService notificationService = new NotificationService();

	@Test
	void countsPendingNotificationsForNewUser() {
		User user = new User("player", "player@example.com", "hash");
		user.setEmailVerified(false);
		user.setProfileIntroSeen(false);

		assertEquals(2, this.notificationService.getNotificationCount(user));
		assertEquals("2", this.notificationService.formatBadgeCount(2));
	}

	@Test
	void formatsNinePlusBadge() {
		assertEquals("9+", this.notificationService.formatBadgeCount(10));
		assertEquals("", this.notificationService.formatBadgeCount(0));
	}

	@Test
	void returnsNoNotificationsWhenEverythingIsDone() {
		User user = new User("player", "player@example.com", "hash");
		user.setEmailVerified(true);
		user.setProfileIntroSeen(true);

		assertTrue(this.notificationService.getNotifications(user, WebLocale.RU).isEmpty());
		assertEquals(0, this.notificationService.getNotificationCount(user));
	}
}
