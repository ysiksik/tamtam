package com.tamtam.api.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import java.security.Key;
import java.util.Date;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.util.Pair;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtUtils {
    /**
     * 토큰에서 userId 찾기
     *
     * @param token 토큰
     * @return userId
     */
    public static String getUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKeyResolver(SigningKeyResolver.INSTANCE)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * user로 토큰 생성
     * HEADER : alg, kid
     * PAYLOAD : sub, iat, exp
     * SIGNATURE : JwtKey.getRandomKey로 구한 Secret Key
     *
     * @param user 유저
     * @return jwt token
     */
    public static String createToken(AuthPrincipal user) {
        Claims claims = Jwts.claims().setSubject(user.email()); // subject
        Date now = new Date(); // 현재 시간
        Pair<String, Key> key = JwtKey.getRandomKey(false);
        // JWT Token 생성
        return Jwts.builder()
                .setClaims(claims) // 정보 저장
                .claim("isRefreshToken", false)
                .setIssuedAt(now) // 토큰 발행 시간 정보
                .setExpiration(new Date(now.getTime() + JwtProperties.EXPIRATION_TIME)) // 토큰 만료 시간 설정
                .setHeaderParam(JwsHeader.KEY_ID, key.getFirst()) // kid
                .signWith(key.getSecond()) // signature
                .compact();
    }

    public static String createRefreshToken(AuthPrincipal user) {
        Claims claims = Jwts.claims().setSubject(user.email()); // subject
        Date now = new Date(); // 현재 시간
        Pair<String, Key> key = JwtKey.getRandomKey(true);
        // 리플레시 토큰 생성
        return Jwts.builder()
                .setClaims(claims) // 정보 저장
                .claim("isRefreshToken", true)
                .setIssuedAt(now) // 토큰 발행 시간 정보
                .setExpiration(new Date(now.getTime() + JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME)) // 토큰 만료 시간 설정
                .setHeaderParam(JwsHeader.KEY_ID, key.getFirst()) // kid
                .signWith(key.getSecond()) // signature
                .compact();
    }

}
