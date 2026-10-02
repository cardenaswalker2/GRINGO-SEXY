package com.gringosexy.repository;

import com.gringosexy.model.VerificationToken;
import com.gringosexy.model.VerificationToken.TokenType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends MongoRepository<VerificationToken, String> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByTokenAndTokenType(String token, TokenType tokenType);

    void deleteByUserId(String userId);

    void deleteByUserIdAndTokenType(String userId, TokenType tokenType);
}
