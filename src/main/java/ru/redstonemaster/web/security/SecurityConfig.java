package ru.redstonemaster.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers(
						"/css/**",
						"/mod-assets/**",
						"/avatars/**",
						"/api/**",
						"/profile",
						"/profile/register",
						"/profile/verify"
				).permitAll()
				.requestMatchers(
						"/profile/verify-email",
						"/profile/avatar",
						"/profile/resend-verification",
						"/profile/intro-seen",
						"/notifications"
				).authenticated()
				.requestMatchers("/moderation/**").hasAnyRole("MODERATOR", "ADMIN")
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.anyRequest().permitAll()
		);
		http.formLogin(form -> form
				.loginPage("/profile")
				.loginProcessingUrl("/profile/login")
				.usernameParameter("login")
				.passwordParameter("password")
				.successHandler((request, response, authentication) -> {
					String lang = request.getParameter("lang");
					if (lang == null || lang.isBlank()) {
						lang = "ru";
					}
					response.sendRedirect("/profile?lang=" + lang + "&login=success");
				})
				.failureUrl("/profile?error=login")
				.permitAll()
		);
		http.logout(logout -> logout
				.logoutUrl("/profile/logout")
				.logoutSuccessHandler((request, response, authentication) -> {
					String lang = request.getParameter("lang");
					if (lang == null || lang.isBlank()) {
						lang = "ru";
					}
					response.sendRedirect("/profile?lang=" + lang + "&logout=1");
				})
				.permitAll()
		);
		http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}

