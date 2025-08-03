package hyunul.schedly.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hyunul.schedly.entity.ScheduleRecommendation;

@Repository
public interface ScheduleRecommendationRepository extends JpaRepository<ScheduleRecommendation, Long> {
    List<ScheduleRecommendation> findByGroupIdAndTargetDateOrderByAvailabilityScoreDesc(
            Long groupId, LocalDate targetDate);
    
    List<ScheduleRecommendation> findByGroupIdAndTargetDateBetweenOrderByAvailabilityScoreDesc(
            Long groupId, LocalDate startDate, LocalDate endDate);
    
    @Modifying
    @Query("DELETE FROM ScheduleRecommendation sr WHERE sr.group.id = :groupId " +
           "AND sr.targetDate = :targetDate")
    void deleteByGroupIdAndTargetDate(@Param("groupId") Long groupId, 
                                     @Param("targetDate") LocalDate targetDate);
    
    // Redis 캐시와 동기화를 위한 최신 추천 데이터 조회
    @Query("SELECT sr FROM ScheduleRecommendation sr WHERE sr.group.id = :groupId " +
           "AND sr.targetDate = :targetDate AND sr.createdAt > :since")
    List<ScheduleRecommendation> findRecentRecommendations(
            @Param("groupId") Long groupId,
            @Param("targetDate") LocalDate targetDate,
            @Param("since") LocalDateTime since);
}