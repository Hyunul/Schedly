package hyunul.schedly.service;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import hyunul.schedly.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor @Slf4j
public class JwtService {
    
    @Value("${jwt.secret:mySecretKey}")
    private String secretKey;
    
    @Value("${jwt.expiration:86400}") // 24시간
    private Long expiration;
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";
    
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());
        
        return createToken(claims, user.getEmail());
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }
    
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }
    
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public Boolean isTokenBlacklisted(String token) {
        String blacklistKey = BLACKLIST_KEY_PREFIX + token;
        try {
            return redisTemplate.hasKey(blacklistKey);
        } catch (Exception e) {
            log.warn("Redis에서 블랙리스트 확인 실패: {}", e.getMessage());
            return false;
        }
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) 
                && !isTokenExpired(token) 
                && !isTokenBlacklisted(token));
    }
    
    public void blacklistToken(String token) {
        try {
            String blacklistKey = BLACKLIST_KEY_PREFIX + token;
            Date expiration = extractExpiration(token);
            long ttl = expiration.getTime() - System.currentTimeMillis();
            
            if (ttl > 0) {
                redisTemplate.opsForValue().set(blacklistKey, "blacklisted", 
                        Duration.ofMillis(ttl));
                log.info("토큰이 블랙리스트에 추가됨");
            }
        } catch (Exception e) {
            log.warn("토큰 블랙리스트 추가 실패: {}", e.getMessage());
        }
    }
}
