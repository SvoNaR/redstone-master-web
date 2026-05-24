package ru.redstonemaster.web.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.profile.AvatarService;
import ru.redstonemaster.web.profile.RegisterForm;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

	private static final SecureRandom RANDOM = new SecureRandom();

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AvatarService avatarService;
	private final String adminUsername;
	private final String adminEmail;
	private final String adminPassword;

	public UserService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			AvatarService avatarService,
			@Value("${app.admin.username}") String adminUsername,
			@Value("${app.admin.email}") String adminEmail,
			@Value("${app.admin.password}") String adminPassword
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.avatarService = avatarService;
		this.adminUsername = adminUsername;
		this.adminEmail = adminEmail;
		this.adminPassword = adminPassword;
	}

	@Transactional
	public User register(RegisterForm form) {
		User user = new User(
				form.getUsername().trim(),
				form.getEmail().trim().toLowerCase(),
				this.passwordEncoder.encode(form.getPassword())
		);
		this.issueVerificationToken(user);
		this.avatarService.assignRandomDefaultAvatar(user);
		return this.userRepository.save(user);
	}

	@Transactional
	public void issueVerificationToken(User user) {
		user.setEmailVerificationToken(this.newToken());
		user.setEmailVerificationExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
	}

	@Transactional
	public boolean verifyEmail(String token) {
		Optional<User> userOptional = this.userRepository.findByEmailVerificationToken(token);
		if (userOptional.isEmpty()) {
			return false;
		}
		User user = userOptional.get();
		if (user.getEmailVerificationExpiresAt() == null
				|| user.getEmailVerificationExpiresAt().isBefore(Instant.now())) {
			return false;
		}
		user.setEmailVerified(true);
		user.setEmailVerificationToken(null);
		user.setEmailVerificationExpiresAt(null);
		return true;
	}

	public Optional<User> findByLogin(String login) {
		return this.userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(login.trim(), login.trim().toLowerCase());
	}

	public Optional<User> findByUsername(String username) {
		return this.userRepository.findByUsernameIgnoreCase(username);
	}

	public List<User> findUsersByRole(UserRole role) {
		return this.userRepository.findByRoleOrderByUsernameAsc(role);
	}

	@Transactional
	public void promoteToModerator(Long userId) {
		User user = this.userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		if (user.getRole() != UserRole.USER) {
			throw new IllegalStateException("Only regular users can be promoted");
		}
		user.setRole(UserRole.MODERATOR);
	}

	@Transactional
	public void demoteModerator(Long userId) {
		User user = this.userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		if (user.getRole() != UserRole.MODERATOR) {
			throw new IllegalStateException("Only moderators can be demoted");
		}
		user.setRole(UserRole.USER);
	}

	@Transactional
	public void ensureAdminExists() {
		var existing = this.userRepository.findByUsernameIgnoreCase(this.adminUsername);
		if (existing.isPresent()) {
			User admin = existing.get();
			if (admin.getAvatarPath() == null || admin.getAvatarPath().isBlank()) {
				this.avatarService.assignRandomDefaultAvatar(admin);
			}
			return;
		}
		User admin = new User(
				this.adminUsername,
				this.adminEmail.toLowerCase(),
				this.passwordEncoder.encode(this.adminPassword)
		);
		admin.setRole(UserRole.ADMIN);
		admin.setEmailVerified(true);
		this.avatarService.assignRandomDefaultAvatar(admin);
		this.userRepository.save(admin);
	}

	private String newToken() {
		byte[] bytes = new byte[24];
		RANDOM.nextBytes(bytes);
		return HexFormat.of().formatHex(bytes);
	}
}
