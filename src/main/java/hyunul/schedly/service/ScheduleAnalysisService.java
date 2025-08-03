package hyunul.schedly.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import hyunul.schedly.dto.ScheduleRecommendationDto;
import hyunul.schedly.dto.TimeSlot;
import hyunul.schedly.entity.Group;
import hyunul.schedly.entity.ScheduleRecommendation;
import hyunul.schedly.entity.User;
import hyunul.schedly.entity.UserSchedule;
import hyunul.schedly.repository.GroupMemberRepository;
import hyunul.schedly.repository.ScheduleRecommendationRepository;
import hyunul.schedly.repository.UserScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor @Slf4j
public class ScheduleAnalysisService {
    
    private final GroupMemberRepository groupMemberRepository;
    private final UserScheduleRepository userScheduleRepository;
    private final ScheduleRecommendationRepository recommendationRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String RECOMMENDATION_CACHE_KEY = "schedule:recommendation:";
    private static final Duration CACHE_TTL = Duration.ofHours(2);
    
    /**
     * 그룹의 공통 가능 시간대 분석 및 추천
     */
    public List<ScheduleRecommendationDto> analyzeAndRecommendSchedule(
            Long groupId, LocalDate targetDate, Duration desiredDuration) {
        
        String cacheKey = RECOMMENDATION_CACHE_KEY + groupId + ":" + targetDate;
        
        // Redis 캐시 확인
        List<ScheduleRecommendationDto> cachedResult = getCachedRecommendations(cacheKey);
        if (cachedResult != null) {
            log.info("캐시에서 추천 결과 반환: groupId={}, date={}", groupId, targetDate);
            return cachedResult;
        }
        
        // 그룹 멤버들 조회
        List<User> groupMembers = groupMemberRepository.findUsersByGroupId(groupId);
        if (groupMembers.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Long> memberIds = groupMembers.stream().map(User::getId).toList();
        
        // 해당 날짜의 모든 멤버 일정 조회
        List<UserSchedule> memberSchedules = userScheduleRepository
                .findByUserIdsAndDateAndType(memberIds, targetDate, UserSchedule.ScheduleType.BUSY);
        
        // 시간대별 가용성 분석
        List<TimeSlot> availableSlots = analyzeAvailableTimeSlots(
                groupMembers, memberSchedules, targetDate, desiredDuration);
        
        // 추천 결과 생성 및 저장
        List<ScheduleRecommendation> recommendations = createRecommendations(
                groupId, targetDate, availableSlots, groupMembers.size());
        
        // DB 저장
        recommendationRepository.deleteByGroupIdAndTargetDate(groupId, targetDate);
        recommendationRepository.saveAll(recommendations);
        
        // DTO 변환
        List<ScheduleRecommendationDto> result = recommendations.stream()
                .map(this::convertToDto)
                .toList();
        
        // Redis 캐시 저장
        cacheRecommendations(cacheKey, result);
        
        return result;
    }
    
    /**
     * 시간대별 가용성 분석
     */
    private List<TimeSlot> analyzeAvailableTimeSlots(
            List<User> members, List<UserSchedule> busySchedules, 
            LocalDate date, Duration desiredDuration) {
        
        // 업무 시간 설정 (9시-18시)
        LocalTime workStart = LocalTime.of(9, 0);
        LocalTime workEnd = LocalTime.of(18, 0);
        
        // 30분 단위로 시간 슬롯 생성
        List<TimeSlot> timeSlots = generateTimeSlots(workStart, workEnd, Duration.ofMinutes(30));
        
        // 각 시간 슬롯별 가용 멤버 수 계산
        for (TimeSlot slot : timeSlots) {
            int availableCount = calculateAvailableMembers(
                    members, busySchedules, slot.getStartTime(), slot.getEndTime());
            slot.setAvailableMembers(availableCount);
            slot.setTotalMembers(members.size());
        }
        
        // 원하는 기간만큼의 연속된 가용 시간대 찾기
        return findContinuousAvailableSlots(timeSlots, desiredDuration);
    }
    
    private List<TimeSlot> generateTimeSlots(LocalTime start, LocalTime end, Duration interval) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime current = start;
        
        while (current.plus(interval).isBefore(end) || current.plus(interval).equals(end)) {
            slots.add(TimeSlot.builder()
                    .startTime(current)
                    .endTime(current.plus(interval))
                    .build());
            current = current.plus(interval);
        }
        
        return slots;
    }
    
    private int calculateAvailableMembers(List<User> members, List<UserSchedule> busySchedules,
                                        LocalTime slotStart, LocalTime slotEnd) {
        Set<Long> busyMemberIds = busySchedules.stream()
                .filter(schedule -> isTimeConflict(schedule.getStartTime(), schedule.getEndTime(),
                                                 slotStart, slotEnd))
                .map(schedule -> schedule.getUser().getId())
                .collect(Collectors.toSet());
        
        return (int) members.stream()
                .filter(member -> !busyMemberIds.contains(member.getId()))
                .count();
    }
    
    private boolean isTimeConflict(LocalTime scheduleStart, LocalTime scheduleEnd,
                                 LocalTime slotStart, LocalTime slotEnd) {
        return scheduleStart.isBefore(slotEnd) && scheduleEnd.isAfter(slotStart);
    }
    
    private List<TimeSlot> findContinuousAvailableSlots(List<TimeSlot> slots, Duration desiredDuration) {
        List<TimeSlot> result = new ArrayList<>();
        int requiredSlots = (int) (desiredDuration.toMinutes() / 30); // 30분 단위
        
        for (int i = 0; i <= slots.size() - requiredSlots; i++) {
            List<TimeSlot> continuous = slots.subList(i, i + requiredSlots);
            
            // 연속된 시간대의 최소 가용 멤버 수 계산
            int minAvailable = continuous.stream()
                    .mapToInt(TimeSlot::getAvailableMembers)
                    .min()
                    .orElse(0);
            
            if (minAvailable > 0) {
                TimeSlot combinedSlot = TimeSlot.builder()
                        .startTime(continuous.get(0).getStartTime())
                        .endTime(continuous.get(continuous.size() - 1).getEndTime())
                        .availableMembers(minAvailable)
                        .totalMembers(continuous.get(0).getTotalMembers())
                        .build();
                
                result.add(combinedSlot);
            }
        }
        
        // 가용성 점수로 정렬
        return result.stream()
                .sorted((a, b) -> Double.compare(
                        b.getAvailabilityScore(), a.getAvailabilityScore()))
                .limit(10) // 상위 10개만 반환
                .toList();
    }
    
    private List<ScheduleRecommendation> createRecommendations(
            Long groupId, LocalDate targetDate, List<TimeSlot> availableSlots, int totalMembers) {
        
        return availableSlots.stream()
                .map(slot -> ScheduleRecommendation.builder()
                        .group(Group.builder().id(groupId).build())
                        .targetDate(targetDate)
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .availableMembers(slot.getAvailableMembers())
                        .totalMembers(totalMembers)
                        .availabilityScore(slot.getAvailabilityScore())
                        .build())
                .toList();
    }
    
    @SuppressWarnings("unchecked")
    private List<ScheduleRecommendationDto> getCachedRecommendations(String cacheKey) {
        try {
            return (List<ScheduleRecommendationDto>) redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.warn("Redis 캐시 조회 실패: {}", e.getMessage());
            return null;
        }
    }
    
    private void cacheRecommendations(String cacheKey, List<ScheduleRecommendationDto> recommendations) {
        try {
            redisTemplate.opsForValue().set(cacheKey, recommendations, CACHE_TTL);
        } catch (Exception e) {
            log.warn("Redis 캐시 저장 실패: {}", e.getMessage());
        }
    }
    
    private ScheduleRecommendationDto convertToDto(ScheduleRecommendation recommendation) {
        return ScheduleRecommendationDto.builder()
                .id(recommendation.getId())
                .targetDate(recommendation.getTargetDate())
                .startTime(recommendation.getStartTime())
                .endTime(recommendation.getEndTime())
                .availableMembers(recommendation.getAvailableMembers())
                .totalMembers(recommendation.getTotalMembers())
                .availabilityScore(recommendation.getAvailabilityScore())
                .build();
    }
    
    /**
     * 캐시 무효화 (스케줄 변경 시 호출)
     */
    public void invalidateRecommendationCache(Long groupId, LocalDate date) {
        String cacheKey = RECOMMENDATION_CACHE_KEY + groupId + ":" + date;
        try {
            redisTemplate.delete(cacheKey);
            log.info("추천 캐시 무효화: groupId={}, date={}", groupId, date);
        } catch (Exception e) {
            log.warn("캐시 무효화 실패: {}", e.getMessage());
        }
    }
}