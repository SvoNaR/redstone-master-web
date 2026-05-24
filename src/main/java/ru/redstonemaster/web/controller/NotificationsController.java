package ru.redstonemaster.web.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.notification.NotificationService;
import ru.redstonemaster.web.profile.AvatarService;
import ru.redstonemaster.web.profile.ProfileUserView;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserService;

@Controller
public class NotificationsController {

	private final UserService userService;
	private final AvatarService avatarService;
	private final NotificationService notificationService;

	public NotificationsController(
			UserService userService,
			AvatarService avatarService,
			NotificationService notificationService
	) {
		this.userService = userService;
		this.avatarService = avatarService;
		this.notificationService = notificationService;
	}

	@GetMapping("/notifications")
	public String notifications(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			Authentication authentication,
			Model model
	) {
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			return "redirect:/profile?lang=" + langCode;
		}
		WebLocale locale = WebLocale.fromCode(langCode);
		User user = this.userService.findByUsername(authentication.getName()).orElseThrow();
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Notifications" : "Уведомления");
		model.addAttribute("profileUser", ProfileUserView.from(user, this.avatarService));
		model.addAttribute("notifications", this.notificationService.getNotifications(user, locale));
		return "notifications/index";
	}
}
