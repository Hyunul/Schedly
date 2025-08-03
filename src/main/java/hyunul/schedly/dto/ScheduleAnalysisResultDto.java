package hyunul.schedly.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduleAnalysisResultDto {
    private Long groupId;
    private String groupName;
    private LocalDate targetDate;
    private Integer totalMembers;
    private List<ScheduleRecommendationDto> recommendations;
    private List<GroupMemberDto> members;
    private AnalysisMetadata metadata;
    
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AnalysisMetadata {
        private LocalDateTime analyzedAt;
        private Integer requestedDurationMinutes;
        private LocalTime searchStartTime;
        private LocalTime searchEndTime;
        private Boolean fromCache;
        private String cacheKey;
    }
}