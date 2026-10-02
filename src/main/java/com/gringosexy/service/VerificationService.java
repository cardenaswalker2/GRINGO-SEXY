package com.gringosexy.service;

import com.gringosexy.model.VerificationToken;

public interface VerificationService {

    VerificationToken createEmailVerificationToken(String userId, String email);

    VerificationToken createPasswordResetToken(String userId, String email);

    boolean verifyEmailToken(String token);

    VerificationToken validatePasswordResetToken(String token);

    void invalidateToken(String token);
}
