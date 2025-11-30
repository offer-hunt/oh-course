package ru.offer.hunt.oh_course.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserDirectoryClient {
    /**
     * Stub implementation that generates a UUID based on the email.
     */

    public Optional<UserInfo> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            log.warn("StubUserDirectoryClient: empty email -> user not found");
            return Optional.empty();
        }

        String normalized = email.trim().toLowerCase(Locale.ROOT);

        UUID userId = UUID.nameUUIDFromBytes(
                normalized.getBytes(StandardCharsets.UTF_8));

        UserInfo user = new UserInfo(userId, normalized);

        log.info("StubUserDirectoryClient: resolved email {} to userId {}",
                user.getEmail(), user.getId());

        return Optional.of(user);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String email;
    }
}
