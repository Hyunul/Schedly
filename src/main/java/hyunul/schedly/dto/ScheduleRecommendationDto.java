package hyunul.schedly.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduleRecommendationDto {
    private Long id;
    private LocalDate targetDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer availableMembers;
    private Integer totalMembers;
    private Double availabilityScore;
    private String formattedTimeRange;
    private String availabilityText;
    
    @JsonIgnore
    public String getFormattedTimeRange() {
        if (formattedTimeRange == null && startTime != null && endTime != null) {
            formattedTimeRange = startTime.format(DateTimeFormatter.ofPattern("HH:mm")) 
                    + " - " + endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return formattedTimeRange;
    }
    
    @JsonIgnore
    public String getAvailabilityText() {
        if (availabilityText == null && availableMembers != null && totalMembers != null) {
            availabilityText = String.format("%d/%d명 참석 가능 (%.0f%%)", 
                    availableMembers, totalMembers, availabilityScore * 100);
        }
        return availabilityText;
    }
}