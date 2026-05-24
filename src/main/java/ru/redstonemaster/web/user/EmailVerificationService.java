package ru.redstonemaster.web.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmailVerificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerificationService.class);

	private final String baseUrl;
	private final String fromAddress;
	private final boolean mailEnabled;
	private final Optional<JavaMailSender> mailSender;

	public EmailVerificationService(
			@Value("${app.base-url}") String baseUrl,
			@Value("${app.mail.from}") String fromAddress,
			@Value("${app.mail.enabled}") boolean mailEnabled,
			Optional<JavaMailSender> mailSender
	) {
		this.baseUrl = baseUrl;
		this.fromAddress = fromAddress;
		this.mailEnabled = mailEnabled;
		this.mailSender = mailSender;
	}

	public boolean isMailConfigured() {
		return this.mailEnabled && this.mailSender.isPresent();
	}

	public String buildVerificationUrl(User user, String langCode) {
		return this.baseUrl + "/profile/verify?token=" + user.getEmailVerificationToken() + "&lang=" + langCode;
	}

	public void sendVerificationEmail(User user, String langCode) {
		String url = this.buildVerificationUrl(user, langCode);
		boolean russian = "ru".equals(langCode);
		String subject = russian
				? "Подтверждение аккаунта — Redstone Master"
				: "Confirm your account — Redstone Master";
		String body = russian
				? """
				Здравствуйте, %s!

				Спасибо за регистрацию на сайте Redstone Master.
				Чтобы подтвердить почту и активировать аккаунт, перейдите по ссылке:

				%s

				Ссылка действует 24 часа. Если вы не регистрировались — проигнорируйте это письмо.
				""".formatted(user.getUsername(), url)
				: """
				Hello, %s!

				Thank you for signing up at Redstone Master.
				To verify your email and activate your account, open this link:

				%s

				The link is valid for 24 hours. If you did not sign up, please ignore this email.
				""".formatted(user.getUsername(), url);

		if (this.isMailConfigured()) {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(this.fromAddress);
			message.setTo(user.getEmail());
			message.setSubject(subject);
			message.setText(body);
			this.mailSender.get().send(message);
			LOGGER.info("Verification email sent to {}", user.getEmail());
			return;
		}

		LOGGER.warn("SMTP is disabled (app.mail.enabled=false). Verification link for {}: {}", user.getEmail(), url);
	}
}
