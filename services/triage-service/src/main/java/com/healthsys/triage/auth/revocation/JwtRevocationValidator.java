package com.healthsys.triage.auth.revocation;

import java.util.UUID;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class JwtRevocationValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error REVOKED_TOKEN = new OAuth2Error(
        "invalid_token", "The token has been revoked.", null
    );

    private final TokenRevocationService tokenRevocationService;

    public JwtRevocationValidator(TokenRevocationService tokenRevocationService) {
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String tokenId = token.getId();
        if (tokenId == null || tokenId.isBlank()) {
            return OAuth2TokenValidatorResult.success();
        }
        boolean revoked = tokenRevocationService.isRevoked(UUID.fromString(tokenId));
        return revoked ? OAuth2TokenValidatorResult.failure(REVOKED_TOKEN) : OAuth2TokenValidatorResult.success();
    }
}
