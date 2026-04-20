package com.tamtam.api.global.jwt;


import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.Map;
import org.springframework.data.util.Pair;

/**
 * JWT Key를 제공하고 조회합니다.
 */
public class JwtKey {
    /**
     * Kid-Key List 외부로 절대 유출되어서는 안됩니다.
     */
    private static final Map<String, String> SECRET_KEY_SET = Map.of(
            "key1", "SpringSecurityJWTPracticeProjectIsSoGoodAndThisProjectIsSoFunSpringSecurityJWTPracticeProjectIsSoGoodAndThisProjectIsSoFun",
            "key2", "GoodSpringSecurityNiceSpringSecurityGoodSpringSecurityNiceSpringSecurityGoodSpringSecurityNiceSpringSecurityGoodSpringSecurityNiceSpringSecurity",
            "key3", "HelloSpringSecurityHelloSpringSecurityHelloSpringSecurityHelloSpringSecurityHelloSpringSecurityHelloSpringSecurityHelloSpringSecurityHelloSpringSecurity"
    );
    private static final String[] KID_SET = SECRET_KEY_SET.keySet().toArray(new String[0]);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * SECRET_KEY_SET 에서 랜덤한 KEY 가져오기
     *
     * @return kid와 key Pair
     */
    public static Pair<String, Key> getRandomKey(boolean isRefreshToken) {
        String kid = KID_SET[SECURE_RANDOM.nextInt(KID_SET.length)];
        String secretKey = SECRET_KEY_SET.get(kid);
        secretKey = MessageFormat.format("{0}{1}", secretKey, isRefreshToken);
        return Pair.of(kid, Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)));
    }
    /**
     * kid로 Key찾기
     *
     * @param kid kid
     * @return Key
     */
    public static Key getKey(String kid, boolean isRefreshToken) {
        String key = SECRET_KEY_SET.getOrDefault(kid, null);
        if (key == null)
            return null;
        key = MessageFormat.format("{0}{1}", key, isRefreshToken);
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }
}
