package hyunul.schedly.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hyunul.schedly.dto.ApiResponse;
import hyunul.schedly.dto.CreateScheduleRequest;
import hyunul.schedly.dto.CustomUserPrincipal;
import hyunul.schedly.dto.UpdateScheduleRequest;
import hyunul.schedly.dto.UserScheduleDto;
import hyunul.schedly.service.UserScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor @Slf4j
@CrossOrigin(origins = "*")
public class UserScheduleController {
    
    private final UserScheduleService userScheduleService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<UserScheduleDto>> createSchedule(
            @Valid @RequestBody CreateScheduleRequest request,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        UserScheduleDto schedule = userScheduleService.createSchedule(userId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("일정이 생성되었습니다.", schedule));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserScheduleDto>>> getUserSchedules(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        List<UserScheduleDto> schedules = userScheduleService
                .getUserSchedules(userId, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }
    
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<UserScheduleDto>> updateSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody UpdateScheduleRequest request,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        UserScheduleDto schedule = userScheduleService.updateSchedule(scheduleId, userId, request);
        
        return ResponseEntity.ok(ApiResponse.success("일정이 수정되었습니다.", schedule));
    }
    
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @PathVariable Long scheduleId,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        userScheduleService.deleteSchedule(scheduleId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("일정이 삭제되었습니다.", null));
    }
    
    private Long extractUserId(Authentication authentication) {
        return ((CustomUserPrincipal) authentication.getPrincipal()).getUserId();
    }
}
