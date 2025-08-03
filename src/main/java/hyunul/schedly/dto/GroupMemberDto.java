package hyunul.schedly.dto;

import java.time.LocalDateTime;

import hyunul.schedly.entity.GroupMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupMemberDto {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private GroupMember.MemberRole role;
    private LocalDateTime joinedAt;
}