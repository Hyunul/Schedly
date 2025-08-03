package hyunul.schedly.dto;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TimeSlot {
    private LocalTime startTime;
    private LocalTime endTime;
    private int availableMembers;
    private int totalMembers;
    
    public double getAvailabilityScore() {
        return totalMembers > 0 ? (double) availableMembers / totalMembers : 0.0;
    }
}