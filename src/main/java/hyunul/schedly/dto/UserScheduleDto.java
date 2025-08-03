package hyunul.schedly.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import hyunul.schedly.entity.UserSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserScheduleDto {
    private Long id;
    private Long userId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String title;
    private String description;
    private UserSchedule.ScheduleType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}