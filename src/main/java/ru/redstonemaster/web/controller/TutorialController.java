package ru.redstonemaster.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import ru.redstonemaster.web.locale.WebLocale;
import ru.redstonemaster.web.model.TutorialLesson;
import ru.redstonemaster.web.model.TutorialSection;
import ru.redstonemaster.web.service.ModLangService;
import ru.redstonemaster.web.service.TutorialContentService;

@Controller
@RequestMapping("/tutorial")
public class TutorialController {

	private final TutorialContentService tutorialContentService;
	private final ModLangService modLangService;

	public TutorialController(TutorialContentService tutorialContentService, ModLangService modLangService) {
		this.tutorialContentService = tutorialContentService;
		this.modLangService = modLangService;
	}

	@GetMapping
	public String list(
			@RequestParam(name = "lang", defaultValue = "ru") String lang,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(lang);
		model.addAttribute("sections", this.tutorialContentService.getSections(locale));
		model.addAttribute("disclaimer", this.modLangService.get(locale, "gui.redstone-master.tutorial.disclaimer"));
		model.addAttribute("pageTitle", locale == WebLocale.EN ? "Tutorial" : "Обучение");
		return "tutorial/index";
	}

	@GetMapping("/{sectionId}")
	public String section(
			@PathVariable String sectionId,
			@RequestParam(name = "lang", defaultValue = "ru") String lang,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(lang);
		TutorialSection section = this.tutorialContentService.findSection(locale, sectionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		model.addAttribute("section", section);
		model.addAttribute("pageTitle", section.title());
		return "tutorial/section";
	}

	@GetMapping("/{sectionId}/{lessonId}")
	public String lesson(
			@PathVariable String sectionId,
			@PathVariable String lessonId,
			@RequestParam(name = "lang", defaultValue = "ru") String lang,
			Model model
	) {
		WebLocale locale = WebLocale.fromCode(lang);
		TutorialSection section = this.tutorialContentService.findSection(locale, sectionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		TutorialLesson lesson = this.tutorialContentService.findLesson(locale, sectionId, lessonId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		model.addAttribute("section", section);
		model.addAttribute("lesson", lesson);
		model.addAttribute("pageTitle", lesson.title());
		return "tutorial/lesson";
	}
}
