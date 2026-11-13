package com.sms.security;

import com.sms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Règles d'accès "propriétaire" : un utilisateur ne peut accéder qu'à ses propres données.
 * Utilisé dans les expressions @PreAuthorize : @accessGuard.isSelf(authentication, #studentId)
 */
@Component("accessGuard")
@RequiredArgsConstructor
public class AccessGuard {

    private final UserRepository userRepository;

    /** Vrai si l'utilisateur connecté (identifié par son e-mail dans le JWT) a l'identifiant demandé. */
    public boolean isSelf(Authentication authentication, Long userId) {
        if (authentication == null || userId == null) {
            return false;
        }
        return userRepository.findByEmail(authentication.getName())
                .map(user -> userId.equals(user.getId()))
                .orElse(false);
    }
}
