package ru.redstonemaster.web.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface UserMuteRepository extends JpaRepository<UserMute, Long> {

	Optional<UserMute> findFirstByUserIdAndMutedUntilGreaterThanOrderByMutedUntilDesc(
			Long userId,
			Instant now
	);
}
