package com.gringosexy.service.impl;

import com.gringosexy.enums.UserStatus;
import com.gringosexy.exception.ResourceNotFoundException;
import com.gringosexy.exception.ValidationException;
import com.gringosexy.model.User;
import com.gringosexy.model.VerificationToken;
import com.gringosexy.model.VerificationToken.TokenType;
import com.gringosexy.repository.UserRepository;
import com.gringosexy.repository.VerificationTokenRepository;
import com.gringosexy.service.ActivityLogService;
import com.gringosexy.service.VerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VerificationServiceImpl implements VerificationService {

    private static final Logger log = LoggerFactory.getLogger(VerificationServiceImpl.class);

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public VerificationServiceImpl(VerificationTokenRepository tokenRepository,
                                   UserRepository userRepository,
                                   ActivityLogService activityLogService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    @Override
    public VerificationToken createEmailVerificationToken(String userId, String email) {
        // Clean existing tokens for this user
        tokenRepository.deleteByUserIdAndTokenType(userId, TokenType.EMAIL_VERIFICATION);

        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        VerificationToken verificationToken = new VerificationToken(token, userId, email, TokenType.EMAIL_VERIFICATION, 24 * 60); // 24 hours
        return tokenRepository.save(verificationToken);
    }

    @Override
    public VerificationToken createPasswordResetToken(String userId, String email) {
        // Clean existing reset tokens for this user
        tokenRepository.deleteByUserIdAndTokenType(userId, TokenType.PASSWORD_RESET);

        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        VerificationToken resetToken = new VerificationToken(token, userId, email, TokenType.PASSWORD_RESET, 60); // 60 minutes
        return tokenRepository.save(resetToken);
    }

    @Override
    public boolean verifyEmailToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new ValidationException("Token de verificación no proporcionado.");
        }

        VerificationToken verificationToken = tokenRepository.findByTokenAndTokenType(token, TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new ResourceNotFoundException("El enlace de verificación no es válido o ya ha sido utilizado."));

        if (!verificationToken.isValid()) {
            throw new ValidationException("El enlace de verificación ha expirado o ya fue utilizado.");
        }

        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("El usuario asociado al token no existe."));

        user.setEmailVerified(true);
        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);

        // Mark token as used
        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        activityLogService.log(user.getId(), user.getUsername(), "EMAIL_VERIFIED", "Correo electrónico verificado exitosamente", null);
        return true;
    }

    @Override
    public VerificationToken validatePasswordResetToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new ValidationException("Token de restablecimiento no proporcionado.");
        }

        VerificationToken resetToken = tokenRepository.findByTokenAndTokenType(token, TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new ResourceNotFoundException("El enlace de restablecimiento es inválido."));

        if (!resetToken.isValid()) {
            throw new ValidationException("El enlace para cambiar la contraseña ha expirado o ya fue utilizado.");
        }

        return resetToken;
    }

    @Override
    public void invalidateToken(String token) {
        tokenRepository.findByToken(token).ifPresent(t -> {
            t.setUsed(true);
            tokenRepository.save(t);
        });
    }
}
