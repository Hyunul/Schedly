# 🗓️ Group Schedule Analyzer

그룹 내 구성원들의 일정을 분석하여 공통으로 가능한 시간대를 추천해주는 웹 애플리케이션입니다.

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-green.svg)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1+-orange.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)
![Redis](https://img.shields.io/badge/Redis-7.0+-red.svg)

## 📋 목차

- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [설치 및 실행](#-설치-및-실행)
- [API 문서](#-api-문서)
- [화면 구성](#-화면-구성)
- [개발 가이드](#-개발-가이드)
- [배포](#-배포)
- [기여하기](#-기여하기)
- [라이선스](#-라이선스)

## ✨ 주요 기능

### 🔐 사용자 인증
- JWT 기반 회원가입/로그인
- 안전한 토큰 기반 인증 시스템
- 자동 로그아웃 및 세션 관리

### 👥 그룹 관리
- 그룹 생성 및 관리
- 멤버 초대 및 역할 관리 (Owner, Admin, Member)
- 실시간 그룹 활동 모니터링

### 📅 개인 일정 관리
- 직관적인 일정 생성/수정/삭제
- 일정 타입별 분류 (바쁨/가능/선호)
- 날짜 범위별 일정 조회 및 필터링

### 🎯 스마트 일정 분석
- 그룹 멤버들의 일정을 종합 분석
- AI 기반 최적 회의 시간 추천
- 참석 가능 인원별 우선순위 제공
- 실시간 분석 결과 캐싱

### 📊 대시보드
- 개인화된 통계 및 인사이트
- 최근 활동 히스토리
- 예정된 회의 및 일정 요약

## 🛠 기술 스택

### Backend
- **Java 17** - 최신 LTS 버전
- **Spring Boot 3.2** - 웹 애플리케이션 프레임워크
- **Spring Security** - 인증/인가 처리
- **Spring Data JPA** - 데이터 액세스 계층
- **MySQL 8.0** - 메인 데이터베이스
- **Redis 7.0** - 캐싱 및 세션 스토어
- **JWT** - 토큰 기반 인증

### Frontend
- **Thymeleaf** - 서버사이드 템플릿 엔진
- **HTML5/CSS3** - 마크업 및 스타일링
- **JavaScript ES6+** - 클라이언트 로직
- **Font Awesome** - 아이콘 라이브러리
- **Responsive Design** - 모바일 우선 반응형

### DevOps & Tools
- **Gradle** - 빌드 도구
- **Docker** - 컨테이너화
- **Docker Compose** - 개발환경 구성
- **GitHub Actions** - CI/CD 파이프라인
- **Swagger** - API 문서화

## 📁 프로젝트 구조

```
group-schedule-analyzer/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/groupscheduler/
│   │   │       ├── GroupScheduleApplication.java
│   │   │       ├── config/          # 설정 클래스
│   │   │       ├── controller/      # REST 컨트롤러
│   │   │       ├── service/         # 비즈니스 로직
│   │   │       ├── repository/      # 데이터 액세스
│   │   │       ├── entity/          # JPA 엔티티
│   │   │       ├── dto/             # 데이터 전송 객체
│   │   │       ├── security/        # 보안 설정
│   │   │       └── util/            # 유틸리티 클래스
│   │   └── resources/
│   │       ├── templates/           # Thymeleaf 템플릿
│   │       │   ├── index.html
│   │       │   ├── login.html
│   │       │   └── fragments/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── style.css
│   │       │   ├── js/
│   │       │   │   └── app.js
│   │       │   └── images/
│   │       └── application.yml      # 애플리케이션 설정
│   └── test/                        # 테스트 코드
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
├── docs/
│   ├── api-spec.md                  # API 명세서
│   └── deployment.md                # 배포 가이드
├── scripts/
│   ├── setup.sh                     # 초기 설정 스크립트
│   └── deploy.sh                    # 배포 스크립트
├── gradle/
├── gradlew
├── gradlew.bat
├── build.gradle
└── README.md
```

## 🚀 설치 및 실행

### 사전 요구사항

- **Java 17** 이상
- **Node.js 16** 이상 (프론트엔드 빌드용)
- **Docker & Docker Compose** (권장)
- **MySQL 8.0** (로컬 설정 시)
- **Redis 7.0** (로컬 설정 시)

### 1. 저장소 클론

```bash
git clone https://github.com/your-username/group-schedule-analyzer.git
cd group-schedule-analyzer
```

### 2. Docker Compose로 실행 (권장)

```bash
# 전체 환경 구동
docker-compose up -d

# 로그 확인
docker-compose logs -f app
```

### 3. 로컬 개발 환경 설정

#### 데이터베이스 설정

```bash
# MySQL 서버 시작
sudo systemctl start mysql

# 데이터베이스 생성
mysql -u root -p
CREATE DATABASE group_scheduler;
CREATE USER 'scheduler_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON group_scheduler.* TO 'scheduler_user'@'localhost';
FLUSH PRIVILEGES;
```

#### Redis 설정

```bash
# Redis 서버 시작
sudo systemctl start redis
```

#### 애플리케이션 설정

```yaml
# src/main/resources/application-local.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/group_scheduler
    username: scheduler_user
    password: your_password
  redis:
    host: localhost
    port: 6379

jwt:
  secret: your-jwt-secret-key-here
  expiration: 86400000
```

#### 애플리케이션 실행

```bash
# 권한 부여
chmod +x gradlew

# 개발 모드로 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 또는 IDE에서 실행
# Main Class: com.groupscheduler.GroupScheduleApplication
# VM Options: -Dspring.profiles.active=local
```

### 4. 접속 확인

- **메인 애플리케이션**: http://localhost:8080
- **API 문서 (Swagger)**: http://localhost:8080/swagger-ui.html
- **액추에이터**: http://localhost:8080/actuator/health

## 📖 API 문서

### 인증 (Authentication)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | 회원가입 |
| POST | `/api/auth/login` | 로그인 |
| POST | `/api/auth/logout` | 로그아웃 |

### 그룹 관리 (Groups)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/groups` | 내 그룹 목록 조회 |
| POST | `/api/groups` | 그룹 생성 |
| GET | `/api/groups/{id}` | 그룹 상세 조회 |
| POST | `/api/groups/{id}/members` | 멤버 추가 |
| DELETE | `/api/groups/{id}/members/{memberId}` | 멤버 제거 |

### 일정 관리 (Schedules)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/schedules` | 일정 목록 조회 |
| POST | `/api/schedules` | 일정 생성 |
| PUT | `/api/schedules/{id}` | 일정 수정 |
| DELETE | `/api/schedules/{id}` | 일정 삭제 |

### 일정 분석 (Analysis)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/groups/{id}/analysis/recommend` | 일정 분석 및 추천 |
| GET | `/api/groups/{id}/analysis/recommendations` | 캐시된 추천 결과 조회 |
| DELETE | `/api/groups/{id}/analysis/cache` | 추천 캐시 삭제 |

상세한 API 명세는 [API 문서](docs/api-spec.md)를 참조하세요.

## 🎨 화면 구성

### 메인 대시보드
- 참여 그룹 수, 오늘 일정, 예정 회의 통계
- 최근 활동 타임라인
- 반응형 카드 레이아웃

### 그룹 관리
- 그룹 생성/편집 모달
- 그룹별 멤버 관리
- 실시간 그룹 활동 표시

### 일정 관리
- 직관적인 일정 입력 폼
- 날짜 필터링 기능
- 일정 타입별 색상 구분

### 일정 분석
- 그룹 선택 및 분석 옵션 설정
- 시각적인 추천 결과 표시
- 참석 가능률 기반 우선순위

## 👨‍💻 개발 가이드

### 코딩 컨벤션

```java
// 클래스명: PascalCase
public class ScheduleService {
    
    // 메서드명: camelCase
    public List<Schedule> findSchedulesByDateRange(LocalDate start, LocalDate end) {
        // 구현
    }
    
    // 상수: UPPER_SNAKE_CASE
    private static final String DEFAULT_TIME_ZONE = "Asia/Seoul";
}
```

### 커밋 메시지 규칙

```bash
# 타입: 제목 (50자 이내)
feat: 그룹 일정 분석 기능 추가
fix: JWT 토큰 만료 처리 버그 수정
docs: API 문서 업데이트
style: 코드 포맷팅 적용
refactor: 서비스 계층 구조 개선
test: 일정 생성 테스트 케이스 추가
chore: Gradle 의존성 업데이트
```

### Branch 전략

```bash
main        # 프로덕션 배포
├── develop # 개발 통합
├── feature/group-analysis    # 기능 개발
├── fix/auth-token-issue      # 버그 수정
└── release/v1.2.0           # 릴리즈 준비
```

### 테스트 작성

```java
@SpringBootTest
@Transactional
class ScheduleServiceTest {
    
    @Test
    @DisplayName("일정 생성 성공 테스트")
    void createSchedule_Success() {
        // Given
        CreateScheduleRequest request = CreateScheduleRequest.builder()
            .title("팀 회의")
            .date(LocalDate.now())
            .startTime(LocalTime.of(14, 0))
            .endTime(LocalTime.of(15, 0))
            .build();
        
        // When
        Schedule result = scheduleService.createSchedule(request, user);
        
        // Then
        assertThat(result.getTitle()).isEqualTo("팀 회의");
        assertThat(result.getUserId()).isEqualTo(user.getId());
    }
}
```

### 로깅 설정

```yaml
logging:
  level:
    com.groupscheduler: DEBUG
    org.springframework.security: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
    max-size: 10MB
    max-history: 30
```

## 🚀 배포

### 프로덕션 환경 변수

```bash
# 환경 변수 설정
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=your-db-host
export DB_PASSWORD=your-db-password
export REDIS_HOST=your-redis-host
export JWT_SECRET=your-jwt-secret
```

### Docker 배포

```bash
# 이미지 빌드
docker build -t group-scheduler:latest .

# 컨테이너 실행
docker run -d \
  --name group-scheduler \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=your-db-host \
  group-scheduler:latest
```

### GitHub Actions CI/CD

```yaml
# .github/workflows/deploy.yml
name: Deploy to Production

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Build with Gradle
      run: ./gradlew build
      
    - name: Build Docker image
      run: docker build -t group-scheduler .
      
    - name: Deploy to AWS
      run: ./scripts/deploy.sh
```

## 🤝 기여하기

### 이슈 리포팅

버그 발견이나 기능 제안이 있으시면 [GitHub Issues](https://github.com/your-username/group-schedule-analyzer/issues)를 통해 알려주세요.

### Pull Request 가이드

1. **Fork** 저장소를 포크합니다.
2. **Branch** 새로운 기능 브랜치를 생성합니다.
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit** 변경사항을 커밋합니다.
   ```bash
   git commit -m 'feat: Add amazing feature'
   ```
4. **Push** 브랜치에 푸시합니다.
   ```bash
   git push origin feature/amazing-feature
   ```
5. **PR** Pull Request를 생성합니다.

### 개발 환경 세팅

```bash
# 개발 의존성 설치
./gradlew build

# 테스트 실행
./gradlew test

# 코드 포맷팅 (선택사항)
./gradlew spotlessApply
```

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 👥 팀 정보

- **개발자**: [Your Name](https://github.com/your-username)
- **이메일**: your.email@example.com
- **프로젝트 홈페이지**: https://groupscheduler.com

## 🙏 감사의 말

이 프로젝트는 다음 오픈소스 프로젝트들의 도움을 받았습니다:

- [Spring Framework](https://spring.io/)
- [Thymeleaf](https://www.thymeleaf.org/)
- [Font Awesome](https://fontawesome.com/)
- [MySQL](https://www.mysql.com/)
- [Redis](https://redis.io/)

---

⭐ 이 프로젝트가 도움이 되셨다면 Star를 눌러주세요!

📧 문의사항이 있으시면 언제든지 연락주세요.

🚀 Happy Coding!