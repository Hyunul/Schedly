package hyunul.schedly.config;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import hyunul.schedly.dto.CustomUserPrincipal;
import hyunul.schedly.entity.User;
import hyunul.schedly.repository.UserRepository;
import hyunul.schedly.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor @Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // ✅ 정적 리소스는 필터 통과시키기
        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/") || uri.startsWith("/favicon.ico")) {
            chain.doFilter(request, response);
            return;
        }
        
        final String requestTokenHeader = request.getHeader("Authorization");
        
        String email = null;
        String jwtToken = null;
        
        // JWT 토큰은 "Bearer token" 형식
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                email = jwtService.extractEmail(jwtToken);
            } catch (IllegalArgumentException e) {
                log.warn("JWT 토큰을 가져올 수 없습니다: {}", e.getMessage());
            } catch (ExpiredJwtException e) {
                log.warn("JWT 토큰이 만료되었습니다: {}", e.getMessage());
            } catch (Exception e) {
                log.warn("JWT 토큰 파싱 오류: {}", e.getMessage());
            }
        }
        
        // 토큰 유효성 검사 및 인증 설정
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                CustomUserPrincipal userPrincipal = new CustomUserPrincipal(
                        user.getId(), user.getEmail(), user.getName(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
                
                if (jwtService.validateToken(jwtToken, userPrincipal)) {
                    UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(
                                    userPrincipal, null, userPrincipal.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        
        chain.doFilter(request, response);
    }
}
