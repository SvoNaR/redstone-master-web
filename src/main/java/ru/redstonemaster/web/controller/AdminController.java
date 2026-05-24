package ru.redstonemaster.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.profile.AvatarService;
import ru.redstonemaster.web.profile.ProfileUserView;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRole;
import ru.redstonemaster.web.user.UserService;

import java.util.List;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	private final UserService userService;
	private final AvatarService avatarService;

	public AdminController(UserService userService, AvatarService avatarService) {
		this.userService = userService;
		this.avatarService = avatarService;
	}

	@GetMapping("/admin")
	public String admin(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(langCode);
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Administration" : "Администрация");
		model.addAttribute("regularUsers", this.toViews(this.userService.findUsersByRole(UserRole.USER)));
		model.addAttribute("moderators", this.toViews(this.userService.findUsersByRole(UserRole.MODERATOR)));
		return "admin/index";
	}

	@PostMapping("/admin/promote")
	public String promote(
			@RequestParam Long userId,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			RedirectAttributes redirectAttributes
	) {
		try {
			this.userService.promoteToModerator(userId);
			redirectAttributes.addFlashAttribute("adminMessage", "promoted");
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
		}
		return "redirect:/admin?lang=" + langCode;
	}

	@PostMapping("/admin/demote")
	public String demote(
			@RequestParam Long userId,
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			RedirectAttributes redirectAttributes
	) {
		try {
			this.userService.demoteModerator(userId);
			redirectAttributes.addFlashAttribute("adminMessage", "demoted");
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
		}
		return "redirect:/admin?lang=" + langCode;
	}

	private List<ProfileUserView> toViews(List<User> users) {
		return users.stream().map(user -> ProfileUserView.from(user, this.avatarService)).toList();
	}
}
