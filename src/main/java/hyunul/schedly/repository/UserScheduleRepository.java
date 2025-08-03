package hyunul.schedly.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hyunul.schedly.entity.UserSchedule;

@Repository
public interface UserScheduleRepository extends JpaRepository<UserSchedule, Long> {
    List<UserSchedule> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<UserSchedule> findByUserIdAndDate(Long userId, LocalDate date);
    
    @Query("SELECT us FROM UserSchedule us WHERE us.user.id IN :userIds " +
           "AND us.date = :date AND us.type = :type")
    List<UserSchedule> findByUserIdsAndDateAndType(
            @Param("userIds") List<Long> userIds, 
            @Param("date") LocalDate date,
            @Param("type") UserSchedule.ScheduleType type);
    
    @Query("SELECT us FROM UserSchedule us WHERE us.user.id IN :userIds " +
           "AND us.date BETWEEN :startDate AND :endDate")
    List<UserSchedule> findByUserIdsAndDateRange(
            @Param("userIds") List<Long> userIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    // 특정 시간대에 바쁜 사용자들 조회
    @Query("SELECT us FROM UserSchedule us WHERE us.user.id IN :userIds " +
           "AND us.date = :date AND us.type = 'BUSY' " +
           "AND ((us.startTime <= :startTime AND us.endTime > :startTime) " +
           "OR (us.startTime < :endTime AND us.endTime >= :endTime) " +
           "OR (us.startTime >= :startTime AND us.endTime <= :endTime))")
    List<UserSchedule> findConflictingSchedules(
            @Param("userIds") List<Long> userIds,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}