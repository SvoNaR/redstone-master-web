package ru.redstonemaster.web.modauth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redstonemaster.web.profile.AvatarService;
import ru.redstonemaster.web.user.User;
import ru.redstonemaster.web.user.UserRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Service
public class ModAuthService {

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final int CODE_BYTES = 24;

	private final ModAuthRequestRepository modAuthRequestRepository;
	private final UserRepository userRepository;
	private final AvatarService avatarService;

	public ModAuthService(
			ModAuthRequestRepository modAuthRequestRepository,
			UserRepository userRepository,
			AvatarService avatarService
	) {
		this.modAuthRequestRepository = modAuthRequestRepository;
		this.userRepository = userRepository;
		this.avatarService = avatarService;
	}

	@Transactional
	public ModAuthRequest begin(String state, int callbackPort, String mode) {
		this.validateState(state);
		this.validatePort(callbackPort);
		String normalizedMode = "register".equals(mode) ? "register" : "login";
		ModAuthRequest request = new ModAuthRequest(
				state,
				callbackPort,
				normalizedMode,
				Instant.now().plus(15, ChronoUnit.MINUTES)
		);
		return this.modAuthRequestRepository.save(request);
	}

	@Transactional(readOnly = true)
	public ModAuthRequest requirePending(String state) {
		ModAuthRequest request = this.modAuthRequestRepository.findById(state)
				.orElseThrow(() -> new IllegalArgumentException("Mod auth request not found"));
		if (request.isConsumed() || request.isExpired()) {
			throw new IllegalArgumentException("Mod auth request expired");
		}
		if (request.getExchangeCode() != null) {
			throw new IllegalStateException("Mod auth request already completed");
		}
		return request;
	}

	@Transactional
	public ModAuthRequest complete(String state, User user) {
		ModAuthRequest request = this.requirePending(state);
		request.complete(user.getId(), this.newExchangeCode());
		return request;
	}

	@Transactional
	public ModAuthProfileResponse exchange(String state, String code) {
		ModAuthRequest request = this.modAuthRequestRepository.findById(state)
				.orElseThrow(() -> new IllegalArgumentException("Mod auth request not found"));
		if (request.isConsumed()) {
			throw new IllegalArgumentException("Mod auth code already used");
		}
		if (request.isExpired()) {
			throw new IllegalArgumentException("Mod auth request expired");
		}
		if (request.getExchangeCode() == null || !request.getExchangeCode().equals(code)) {
			throw new IllegalArgumentException("Invalid mod auth code");
		}
		User user = this.userRepository.findById(request.getUserId())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		request.markConsumed();
		return new ModAuthProfileResponse(
				user.getUsername(),
				this.avatarService.getAvatarUrl(user)
		);
	}

	@Transactional(readOnly = true)
	public ModAuthRequest requireCompleted(String state) {
		ModAuthRequest request = this.modAuthRequestRepository.findById(state)
				.orElseThrow(() -> new IllegalArgumentException("Mod auth request not found"));
		if (request.getExchangeCode() == null || request.getUserId() == null) {
			throw new IllegalStateException("Mod auth request is not completed");
		}
		if (request.isExpired()) {
			throw new IllegalArgumentException("Mod auth request expired");
		}
		return request;
	}

	@Transactional(readOnly = true)
	public User requireCompletedUser(String state) {
		ModAuthRequest request = this.requireCompleted(state);
		return this.userRepository.findById(request.getUserId())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
	}

	private void validateState(String state) {
		if (state == null || !state.matches("[0-9a-fA-F\\-]{16,64}")) {
			throw new IllegalArgumentException("Invalid mod auth state");
		}
	}

	private void validatePort(int callbackPort) {
		if (callbackPort < 1024 || callbackPort > 65535) {
			throw new IllegalArgumentException("Invalid callback port");
		}
	}

	private String newExchangeCode() {
		byte[] bytes = new byte[CODE_BYTES];
		RANDOM.nextBytes(bytes);
		return HexFormat.of().formatHex(bytes);
	}
}
