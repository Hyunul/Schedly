package hyunul.schedly.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import hyunul.schedly.dto.CreateGroupRequest;
import hyunul.schedly.dto.GroupDto;
import hyunul.schedly.dto.GroupMemberDto;
import hyunul.schedly.entity.Group;
import hyunul.schedly.entity.GroupMember;
import hyunul.schedly.entity.User;
import hyunul.schedly.repository.GroupMemberRepository;
import hyunul.schedly.repository.GroupRepository;
import hyunul.schedly.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor @Slf4j
public class GroupService {
    
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    
    public GroupDto createGroup(Long ownerId, CreateGroupRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();
        
        Group savedGroup = groupRepository.save(group);
        
        // 그룹 소유자를 멤버로 추가
        GroupMember ownerMember = GroupMember.builder()
                .group(savedGroup)
                .user(owner)
                .role(GroupMember.MemberRole.OWNER)
                .build();
        
        groupMemberRepository.save(ownerMember);
        
        return convertToDto(savedGroup);
    }
    
    public List<GroupDto> getUserGroups(Long userId) {
        List<Group> groups = groupRepository.findGroupsByMemberId(userId);
        return groups.stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public GroupDto getGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다."));
        
        // 멤버십 확인
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new AccessDeniedException("그룹에 접근할 권한이 없습니다.");
        }
        
        return convertToDto(group);
    }
    
    public void addMember(Long groupId, Long requesterId, String memberEmail) {
        // 권한 확인
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new AccessDeniedException("그룹에 멤버를 추가할 권한이 없습니다."));
        
        if (requester.getRole() == GroupMember.MemberRole.MEMBER) {
            throw new AccessDeniedException("멤버를 추가할 권한이 없습니다.");
        }
        
        // 추가할 사용자 조회
        User newMember = userRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        
        // 이미 멤버인지 확인
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, newMember.getId())) {
            throw new IllegalStateException("이미 그룹의 멤버입니다.");
        }
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다."));
        
        GroupMember member = GroupMember.builder()
                .group(group)
                .user(newMember)
                .role(GroupMember.MemberRole.MEMBER)
                .build();
        
        groupMemberRepository.save(member);
    }
    
    public void removeMember(Long groupId, Long requesterId, Long memberId) {
        // 권한 확인
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new AccessDeniedException("그룹에서 멤버를 제거할 권한이 없습니다."));
        
        GroupMember targetMember = groupMemberRepository.findByGroupIdAndUserId(groupId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("멤버를 찾을 수 없습니다."));
        
        // 소유자는 제거할 수 없음
        if (targetMember.getRole() == GroupMember.MemberRole.OWNER) {
            throw new IllegalStateException("그룹 소유자는 제거할 수 없습니다.");
        }
        
        // 권한 확인: 소유자나 관리자만 제거 가능, 본인은 언제든 탈퇴 가능
        if (requester.getRole() == GroupMember.MemberRole.MEMBER && !requesterId.equals(memberId)) {
            throw new AccessDeniedException("멤버를 제거할 권한이 없습니다.");
        }
        
        groupMemberRepository.deleteByGroupIdAndUserId(groupId, memberId);
    }
    
    public List<GroupMemberDto> getGroupMembers(Long groupId, Long userId) {
        // 멤버십 확인
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new AccessDeniedException("그룹 멤버 목록을 볼 권한이 없습니다.");
        }
        
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        return members.stream()
                .map(this::convertToMemberDto)
                .toList();
    }
    
    private GroupDto convertToDto(Group group) {
        return GroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .ownerId(group.getOwner().getId())
                .ownerName(group.getOwner().getName())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
    
    private GroupMemberDto convertToMemberDto(GroupMember member) {
        return GroupMemberDto.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .userName(member.getUser().getName())
                .userEmail(member.getUser().getEmail())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
