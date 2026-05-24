package ru.redstonemaster.web.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsernameIgnoreCase(String username);

	Optional<User> findByEmailIgnoreCase(String email);

	Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

	Optional<User> findByEmailVerificationToken(String token);

	boolean existsByUsernameIgnoreCase(String username);

	boolean existsByEmailIgnoreCase(String email);

	List<User> findByRoleOrderByUsernameAsc(UserRole role);
}
