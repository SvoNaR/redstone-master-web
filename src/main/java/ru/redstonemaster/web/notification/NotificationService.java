package ru.redstonemaster.web.notification;

import org.springframework.stereotype.Service;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.user.User;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

	public List<NotificationView> getNotifications(User user, WebLocale locale) {
		List<NotificationView> notifications = new ArrayList<>();
		if (!user.isEmailVerified()) {
			notifications.add(this.emailVerificationNotification(locale));
		}
		if (!user.isProfileIntroSeen()) {
			notifications.add(this.avatarSetupNotification(locale));
		}
		return notifications;
	}

	public int getNotificationCount(User user) {
		int count = 0;
		if (!user.isEmailVerified()) {
			count++;
		}
		if (!user.isProfileIntroSeen()) {
			count++;
		}
		return count;
	}

	public String formatBadgeCount(int count) {
		if (count <= 0) {
			return "";
		}
		if (count > 9) {
			return "9+";
		}
		return Integer.toString(count);
	}

	private NotificationView emailVerificationNotification(WebLocale locale) {
		if (locale == WebLocale.EN) {
			return new NotificationView(
					NotificationType.EMAIL_VERIFICATION,
					"Confirm your email",
					"Your email address is not verified yet. Open the confirmation link we sent or request a new email.",
					"/profile/verify-email?lang=en",
					"Confirm email"
			);
		}
		return new NotificationView(
				NotificationType.EMAIL_VERIFICATION,
				"Подтвердите почту",
				"Адрес почты ещё не подтверждён. Перейдите по ссылке из письма или запросите новое.",
				"/profile/verify-email?lang=ru",
				"Подтвердить почту"
		);
	}

	private NotificationView avatarSetupNotification(WebLocale locale) {
		if (locale == WebLocale.EN) {
			return new NotificationView(
					NotificationType.AVATAR_SETUP,
					"Set up your avatar",
					"Upload a Minecraft skin (64×64) or a ready-made 8×8 avatar for your profile.",
					"/profile/avatar?lang=en",
					"Change avatar"
			);
		}
		return new NotificationView(
				NotificationType.AVATAR_SETUP,
				"Настройте аватарку",
				"Загрузите скин Minecraft 64×64 или готовую аватарку 8×8 для профиля.",
				"/profile/avatar?lang=ru",
				"Сменить аватарку"
		);
	}
}
