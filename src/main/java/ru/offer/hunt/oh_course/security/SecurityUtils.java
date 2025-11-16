package ru.offer.hunt.oh_course.security;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public final class SecurityUtils {

    private static final UUID LOCAL_STUB_USER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private SecurityUtils() {
    }

    public static UUID getUserId(Authentication authentication) {
        if (authentication == null) {
            // ВРЕМЕННАЯ заглушка для local-профиля
            log.warn("Authentication is null, using stub user id: {}", LOCAL_STUB_USER_ID);
            return LOCAL_STUB_USER_ID;
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String sub = jwtAuth.getToken().getSubject();
            try {
                return UUID.fromString(sub);
            } catch (IllegalArgumentException e) {
                log.error("Invalid user id in token 'sub': {}", sub);
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Некорректный идентификатор пользователя в токене");
            }
        }

        log.error("Unsupported authentication type: {}", authentication.getClass());
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Некорректный тип аутентификации");
    }
}
