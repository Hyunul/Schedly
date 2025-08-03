package hyunul.schedly.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hyunul.schedly.dto.AddMemberRequest;
import hyunul.schedly.dto.ApiResponse;
import hyunul.schedly.dto.CreateGroupRequest;
import hyunul.schedly.dto.CustomUserPrincipal;
import hyunul.schedly.dto.GroupDto;
import hyunul.schedly.dto.GroupMemberDto;
import hyunul.schedly.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor @Slf4j
@CrossOrigin(origins = "*")
public class GroupController {
    
    private final GroupService groupService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<GroupDto>> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        GroupDto group = groupService.createGroup(userId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("그룹이 생성되었습니다.", group));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupDto>>> getUserGroups(
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        List<GroupDto> groups = groupService.getUserGroups(userId);
        
        return ResponseEntity.ok(ApiResponse.success(groups));
    }
    
    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupDto>> getGroup(
            @PathVariable Long groupId,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        GroupDto group = groupService.getGroup(groupId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(group));
    }
    
    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @PathVariable Long groupId,
            @Valid @RequestBody AddMemberRequest request,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        groupService.addMember(groupId, userId, request.getEmail());
        
        return ResponseEntity.ok(ApiResponse.success("멤버가 추가되었습니다.", null));
    }
    
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        groupService.removeMember(groupId, userId, memberId);
        
        return ResponseEntity.ok(ApiResponse.success("멤버가 제거되었습니다.", null));
    }
    
    @GetMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<List<GroupMemberDto>>> getGroupMembers(
            @PathVariable Long groupId,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        List<GroupMemberDto> members = groupService.getGroupMembers(groupId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(members));
    }
    
    private Long extractUserId(Authentication authentication) {
        return ((CustomUserPrincipal) authentication.getPrincipal()).getUserId();
    }
}
