package hyunul.schedly.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

import hyunul.schedly.entity.UserSchedule;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateScheduleRequest {
    @NotNull
    private LocalDate date;
    
    @NotNull
    private LocalTime startTime;
    
    @NotNull
    private LocalTime endTime;
    
    private String title;
    private String description;
    private UserSchedule.ScheduleType type;
    
    @AssertTrue(message = "종료 시간은 시작 시간보다 늦어야 합니다.")
    public boolean isValidTimeRange() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }
}
