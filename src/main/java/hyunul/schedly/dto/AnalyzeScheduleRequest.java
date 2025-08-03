package hyunul.schedly.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AnalyzeScheduleRequest {
    @NotNull
    private LocalDate targetDate;
    
    @Builder.Default
    private Integer durationMinutes = 60; // 기본 1시간
    
    @Builder.Default
    private LocalTime preferredStartTime = LocalTime.of(9, 0);
    
    @Builder.Default
    private LocalTime preferredEndTime = LocalTime.of(18, 0);
}