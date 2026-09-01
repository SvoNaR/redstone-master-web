package ru.redstonemaster.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.redstonemaster.web.profile.AvatarService;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;

import java.util.List;

@Component
@Order(1)
public class UserDatabaseMigration implements ApplicationRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserDatabaseMigration.class);

	private final JdbcTemplate jdbcTemplate;
	private final UserRepository userRepository;
	private final AvatarService avatarService;

	public UserDatabaseMigration(
			JdbcTemplate jdbcTemplate,
			UserRepository userRepository,
			AvatarService avatarService
	) {
		this.jdbcTemplate = jdbcTemplate;
		this.userRepository = userRepository;
		this.avatarService = avatarService;
	}

	@Override
	public void run(ApplicationArguments args) {
		this.addColumnIfMissing("avatar_path", "VARCHAR(128)");
		this.addColumnIfMissing("custom_avatar", "BOOLEAN");
		this.addColumnIfMissing("profile_intro_seen", "BOOLEAN");
		this.addColumnIfMissing("mod_sync_token", "VARCHAR(64)");

		this.jdbcTemplate.update("UPDATE users SET avatar_path = 'defaults/skin1.png' WHERE avatar_path IS NULL");
		this.jdbcTemplate.update("UPDATE users SET custom_avatar = FALSE WHERE custom_avatar IS NULL");
		this.jdbcTemplate.update("UPDATE users SET profile_intro_seen = FALSE WHERE profile_intro_seen IS NULL");

		List<User> usersWithoutAvatar = this.userRepository.findAll().stream()
				.filter(user -> user.getAvatarPath() == null || user.getAvatarPath().isBlank())
				.toList();
		for (User user : usersWithoutAvatar) {
			this.avatarService.assignRandomDefaultAvatar(user);
		}
		if (!usersWithoutAvatar.isEmpty()) {
			this.userRepository.saveAll(usersWithoutAvatar);
		}

		LOGGER.info("User profile columns migration completed");
	}

	private void addColumnIfMissing(String columnName, String sqlType) {
		Integer count = this.jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
				WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'USERS' AND COLUMN_NAME = ?
				""",
				Integer.class,
				columnName.toUpperCase()
		);
		if (count == null || count == 0) {
			this.jdbcTemplate.execute("ALTER TABLE users ADD COLUMN " + columnName + " " + sqlType);
			LOGGER.info("Added users.{}", columnName);
		}
	}
}
