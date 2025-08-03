package hyunul.schedly.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hyunul.schedly.dto.AnalyzeScheduleRequest;
import hyunul.schedly.dto.ApiResponse;
import hyunul.schedly.dto.CustomUserPrincipal;
import hyunul.schedly.dto.GroupDto;
import hyunul.schedly.dto.GroupMemberDto;
import hyunul.schedly.dto.ScheduleAnalysisResultDto;
import hyunul.schedly.dto.ScheduleRecommendationDto;
import hyunul.schedly.service.GroupService;
import hyunul.schedly.service.ScheduleAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/groups/{groupId}/analysis")
@RequiredArgsConstructor @Slf4j
@CrossOrigin(origins = "*")
public class ScheduleAnalysisController {
    
    private final ScheduleAnalysisService scheduleAnalysisService;
    private final GroupService groupService;
    
    @PostMapping("/recommend")
    public ResponseEntity<ApiResponse<ScheduleAnalysisResultDto>> analyzeSchedule(
            @PathVariable Long groupId,
            @Valid @RequestBody AnalyzeScheduleRequest request,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        
        // 그룹 접근 권한 확인
        groupService.getGroup(groupId, userId);
        
        // 일정 분석 수행
        Duration desiredDuration = Duration.ofMinutes(request.getDurationMinutes());
        List<ScheduleRecommendationDto> recommendations = scheduleAnalysisService
                .analyzeAndRecommendSchedule(groupId, request.getTargetDate(), desiredDuration);
        
        // 그룹 정보 및 멤버 정보 조회
        GroupDto group = groupService.getGroup(groupId, userId);
        List<GroupMemberDto> members = groupService.getGroupMembers(groupId, userId);
        
        // 결과 구성
        ScheduleAnalysisResultDto result = ScheduleAnalysisResultDto.builder()
                .groupId(groupId)
                .groupName(group.getName())
                .targetDate(request.getTargetDate())
                .totalMembers(members.size())
                .recommendations(recommendations)
                .members(members)
                .metadata(ScheduleAnalysisResultDto.AnalysisMetadata.builder()
                        .analyzedAt(LocalDateTime.now())
                        .requestedDurationMinutes(request.getDurationMinutes())
                        .searchStartTime(request.getPreferredStartTime())
                        .searchEndTime(request.getPreferredEndTime())
                        .fromCache(false) // 실제로는 서비스에서 캐시 여부를 전달받아야 함
                        .build())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("일정 분석이 완료되었습니다.", result));
    }
    
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<ScheduleRecommendationDto>>> getRecommendations(
            @PathVariable Long groupId,
            @RequestParam LocalDate targetDate,
            @RequestParam(defaultValue = "60") Integer durationMinutes,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        
        // 그룹 접근 권한 확인
        groupService.getGroup(groupId, userId);
        
        // 캐시된 추천 결과 조회
        Duration desiredDuration = Duration.ofMinutes(durationMinutes);
        List<ScheduleRecommendationDto> recommendations = scheduleAnalysisService
                .analyzeAndRecommendSchedule(groupId, targetDate, desiredDuration);
        
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }
    
    @DeleteMapping("/cache")
    public ResponseEntity<ApiResponse<Void>> clearCache(
            @PathVariable Long groupId,
            @RequestParam LocalDate targetDate,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        
        // 그룹 접근 권한 확인 (관리자 이상만 캐시 삭제 가능하도록 추가 검증 필요)
        groupService.getGroup(groupId, userId);
        
        scheduleAnalysisService.invalidateRecommendationCache(groupId, targetDate);
        
        return ResponseEntity.ok(ApiResponse.success("캐시가 삭제되었습니다.", null));
    }
    
    private Long extractUserId(Authentication authentication) {
        return ((CustomUserPrincipal) authentication.getPrincipal()).getUserId();
    }
}
