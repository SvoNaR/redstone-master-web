package ru.redstonemaster.web.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.notification.NotificationType;
import ru.redstonemaster.web.notification.UserNotificationRepository;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;
import ru.redstonemaster.web.user.UserRole;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class LessonCommentMuteTest {

	@Autowired
	private LessonCommentService commentService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserNotificationRepository notificationRepository;

	@Test
	void mutedUserCannotPostComments() {
		User author = this.userRepository.save(new User("muted_user", "muted@test.com", "hash"));
		User moderator = this.userRepository.save(new User("mod_user", "mod@test.com", "hash"));
		moderator.setRole(UserRole.MODERATOR);
		this.userRepository.save(moderator);

		this.commentService.muteUser(moderator, author.getId(), 60, "spam");

		assertTrue(this.commentService.isMuted(author));
		assertThrows(IllegalStateException.class, () -> this.commentService.postComment(
				author,
				"redstone_signal",
				"carry_signal",
				"test",
				null
		));
	}

	@Test
	void muteCreatesNotificationForTarget() {
		User author = this.userRepository.save(new User("notify_muted", "notify-muted@test.com", "hash"));
		User moderator = this.userRepository.save(new User("notify_mod", "notify-mod@test.com", "hash"));
		moderator.setRole(UserRole.MODERATOR);
		this.userRepository.save(moderator);

		this.commentService.muteUser(moderator, author.getId(), 30, "rules");

		long notifications = this.notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(author.getId())
				.stream()
				.filter(notification -> notification.getType() == NotificationType.USER_MUTE)
				.count();
		assertEquals(1, notifications);
	}

	@Test
	void moderatorCannotMuteAnotherModerator() {
		User target = this.userRepository.save(new User("mod_target", "mod-target@test.com", "hash"));
		target.setRole(UserRole.MODERATOR);
		this.userRepository.save(target);
		User moderator = this.userRepository.save(new User("mod_actor", "mod-actor@test.com", "hash"));
		moderator.setRole(UserRole.MODERATOR);
		this.userRepository.save(moderator);

		assertThrows(IllegalArgumentException.class, () -> this.commentService.muteUser(
				moderator,
				target.getId(),
				10,
				"test"
		));
		assertFalse(this.commentService.isMuted(target));
	}

	@Test
	void adminCanMuteModerator() {
		User target = this.userRepository.save(new User("mod_for_admin", "mod-admin@test.com", "hash"));
		target.setRole(UserRole.MODERATOR);
		this.userRepository.save(target);
		User admin = this.userRepository.save(new User("admin_actor", "admin-actor@test.com", "hash"));
		admin.setRole(UserRole.ADMIN);
		this.userRepository.save(admin);

		this.commentService.muteUser(admin, target.getId(), 15, "policy");
		assertTrue(this.commentService.isMuted(target));
	}
}
