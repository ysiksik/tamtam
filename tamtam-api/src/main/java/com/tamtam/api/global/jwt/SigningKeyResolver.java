package com.tamtam.api.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import java.security.Key;

/**
 * JwsHeader를 통해 Signature 검증에 필요한 Key를 가져오는 코드를 구현합니다.
 */
public class SigningKeyResolver extends SigningKeyResolverAdapter {
    public static final SigningKeyResolver INSTANCE = new SigningKeyResolver();

    private SigningKeyResolver() {
    }


    @Override
    public Key resolveSigningKey(JwsHeader jwsHeader, Claims claims) {
        String kid = jwsHeader.getKeyId();
        boolean isRefreshToken = claims.get("isRefreshToken", Boolean.class);
        if (kid == null)
            return null;
        return JwtKey.getKey(kid, isRefreshToken);
    }
}
