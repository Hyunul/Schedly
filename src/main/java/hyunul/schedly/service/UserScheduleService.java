package hyunul.schedly.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import hyunul.schedly.dto.CreateScheduleRequest;
import hyunul.schedly.dto.UpdateScheduleRequest;
import hyunul.schedly.dto.UserScheduleDto;
import hyunul.schedly.entity.User;
import hyunul.schedly.entity.UserSchedule;
import hyunul.schedly.repository.UserScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor @Slf4j  
public class UserScheduleService {
    
    private final UserScheduleRepository userScheduleRepository;
    private final ScheduleAnalysisService scheduleAnalysisService;
    
    public UserScheduleDto createSchedule(Long userId, CreateScheduleRequest request) {
        UserSchedule schedule = UserSchedule.builder()
                .user(User.builder().id(userId).build())
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType() != null ? request.getType() : UserSchedule.ScheduleType.BUSY)
                .build();
        
        UserSchedule saved = userScheduleRepository.save(schedule);
        
        // 관련 그룹들의 추천 캐시 무효화
        invalidateRelatedRecommendations(userId, request.getDate());
        
        return convertToDto(saved);
    }
    
    public List<UserScheduleDto> getUserSchedules(Long userId, LocalDate startDate, LocalDate endDate) {
        List<UserSchedule> schedules = userScheduleRepository
                .findByUserIdAndDateBetween(userId, startDate, endDate);
        
        return schedules.stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public UserScheduleDto updateSchedule(Long scheduleId, Long userId, UpdateScheduleRequest request) {
        UserSchedule schedule = userScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("스케줄을 찾을 수 없습니다."));
        
        if (!schedule.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("스케줄을 수정할 권한이 없습니다.");
        }
        
        LocalDate originalDate = schedule.getDate();
        
        // 스케줄 업데이트
        if (request.getDate() != null) schedule.setDate(request.getDate());
        if (request.getStartTime() != null) schedule.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) schedule.setEndTime(request.getEndTime());
        if (request.getTitle() != null) schedule.setTitle(request.getTitle());
        if (request.getDescription() != null) schedule.setDescription(request.getDescription());
        if (request.getType() != null) schedule.setType(request.getType());
        
        UserSchedule updated = userScheduleRepository.save(schedule);
        
        // 캐시 무효화 (원본 날짜와 변경된 날짜 모두)
        invalidateRelatedRecommendations(userId, originalDate);
        if (!originalDate.equals(updated.getDate())) {
            invalidateRelatedRecommendations(userId, updated.getDate());
        }
        
        return convertToDto(updated);
    }
    
    public void deleteSchedule(Long scheduleId, Long userId) {
        UserSchedule schedule = userScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("스케줄을 찾을 수 없습니다."));
        
        if (!schedule.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("스케줄을 삭제할 권한이 없습니다.");
        }
        
        LocalDate scheduleDate = schedule.getDate();
        userScheduleRepository.delete(schedule);
        
        // 캐시 무효화
        invalidateRelatedRecommendations(userId, scheduleDate);
    }
    
    private void invalidateRelatedRecommendations(Long userId, LocalDate date) {
        // 해당 사용자가 속한 모든 그룹의 추천 캐시를 무효화
        // 실제로는 GroupMemberRepository를 통해 사용자가 속한 그룹들을 조회해야 함
        // 여기서는 간단히 처리
        try {
            scheduleAnalysisService.invalidateRecommendationCache(null, date);
        } catch (Exception e) {
            log.warn("추천 캐시 무효화 실패: {}", e.getMessage());
        }
    }
    
    private UserScheduleDto convertToDto(UserSchedule schedule) {
        return UserScheduleDto.builder()
                .id(schedule.getId())
                .userId(schedule.getUser().getId())
                .date(schedule.getDate())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .type(schedule.getType())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }
}