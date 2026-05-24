package ru.redstonemaster.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.redstonemaster.web.locale.WebLocale;

@Controller
@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
public class ModerationController {

	@GetMapping("/moderation")
	public String moderation(
			@RequestParam(name = "lang", defaultValue = "ru") String langCode,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(langCode);
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Moderation" : "Модерация");
		return "moderation/index";
	}
}
