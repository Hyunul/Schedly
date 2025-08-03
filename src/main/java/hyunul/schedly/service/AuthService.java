package hyunul.schedly.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import hyunul.schedly.dto.AuthResponseDto;
import hyunul.schedly.dto.LoginRequest;
import hyunul.schedly.dto.RegisterRequest;
import hyunul.schedly.dto.UserDto;
import hyunul.schedly.entity.User;
import hyunul.schedly.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor @Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public AuthResponseDto register(RegisterRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        
        // 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();
        
        User savedUser = userRepository.save(user);
        
        // JWT 토큰 생성
        String token = jwtService.generateToken(savedUser);
        
        return AuthResponseDto.builder()
                .token(token)
                .user(convertToUserDto(savedUser))
                .build();
    }
    
    public AuthResponseDto login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 잘못되었습니다."));
        
        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 잘못되었습니다.");
        }
        
        // JWT 토큰 생성
        String token = jwtService.generateToken(user);
        
        return AuthResponseDto.builder()
                .token(token)
                .user(convertToUserDto(user))
                .build();
    }
    
    public void logout(String token) {
        jwtService.blacklistToken(token);
    }
    
    private UserDto convertToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}