package com.techforum.service.impl;

import com.techforum.dto.response.*;
import com.techforum.entity.*;
import com.techforum.enums.RoleName;
import com.techforum.exception.BadRequestException;
import com.techforum.exception.ResourceNotFoundException;
import com.techforum.repository.*;
import com.techforum.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired private UserRepository          userRepository;
    @Autowired private PostRepository          postRepository;
    @Autowired private AnswerRepository        answerRepository;
    @Autowired private ReportRepository        reportRepository;
    @Autowired private RoleRepository          roleRepository;
    @Autowired private WarningRepository       warningRepository;
    @Autowired private ModerationLogRepository logRepository;

    // ── STATS ──────────────────────────────────────────────
    @Override
    public AdminStatsResponse getStats() {
        // FIX: Replaced five findAll().stream().filter() calls with proper
        //      COUNT @Query methods in the repositories.
        //      Old code loaded every User, Post, and Answer row into the JVM
        //      heap just to count a boolean field — O(N) memory for no reason.
        long totalUsers  = userRepository.count();
        long totalPosts  = postRepository.count();
        long totalAns    = answerRepository.count();
        long pending     = reportRepository.countByStatus(
                com.techforum.enums.ReportStatus.PENDING);
        long banned      = userRepository.countByIsBannedTrue();       // FIX
        long solved      = postRepository.countSolvedPosts();          // FIX
        long mods        = userRepository.countByRole(com.techforum.enums.RoleName.ROLE_MODERATOR);           // FIX
        long admins      = userRepository.countByRole(com.techforum.enums.RoleName.ROLE_ADMIN);               // FIX

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalPosts(totalPosts)
                .totalAnswers(totalAns)
                .pendingReports(pending)
                .bannedUsers(banned)
                .solvedPosts(solved)
                .totalModerators(mods)
                .totalAdmins(admins)
                .build();
    }

    // ── GET ALL USERS ──────────────────────────────────────
    @Override
    public PagedResponse<UserSummaryResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        Page<User> users = userRepository.findAll(pageable);
        return buildUserPage(users);
    }

    // ── GET USER DETAIL ────────────────────────────────────
    @Override
    public UserSummaryResponse getUserDetail(Long userId) {
        return toUserSummary(getUserById(userId));
    }

    // ── BAN USER ───────────────────────────────────────────
    @Override
    @Transactional
    public void banUser(Long userId, String adminUsername) {
        User user = getUserById(userId);
        if (user.getIsBanned())
            throw new BadRequestException("User is already banned");
        user.setIsBanned(true);
        user.setIsActive(false);
        userRepository.save(user);
        logAdminAction(adminUsername, "BAN_USER", userId, "User banned");
    }

    // ── UNBAN USER ─────────────────────────────────────────
    @Override
    @Transactional
    public void unbanUser(Long userId, String adminUsername) {
        User user = getUserById(userId);
        if (!user.getIsBanned())
            throw new BadRequestException("User is not banned");
        user.setIsBanned(false);
        user.setIsActive(true);
        userRepository.save(user);
        logAdminAction(adminUsername, "UNBAN_USER", userId, "User unbanned");
    }

    // ── DELETE POST ────────────────────────────────────────
    @Override
    @Transactional
    public void deletePost(Long postId, String adminUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found: " + postId));
        post.setIsDeleted(true);
        postRepository.save(post);
        logAdminAction(adminUsername, "DELETE_POST", postId,
                "Post deleted by admin");
    }

    // ── PROMOTE TO MODERATOR ───────────────────────────────
    @Override
    @Transactional
    public void promoteToModerator(Long userId, String adminUsername) {
        User user = getUserById(userId);
        boolean alreadyMod = user.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.ROLE_MODERATOR);
        if (alreadyMod)
            throw new BadRequestException("User is already a moderator");
        Role modRole = roleRepository.findByName(RoleName.ROLE_MODERATOR)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Moderator role not found"));
        user.getRoles().add(modRole);
        userRepository.save(user);
        logAdminAction(adminUsername, "PROMOTE_MODERATOR", userId,
                "Promoted to moderator");
    }

    // ── DEMOTE FROM MODERATOR ──────────────────────────────
    @Override
    @Transactional
    public void demoteFromModerator(Long userId, String adminUsername) {
        User user = getUserById(userId);
        user.getRoles().removeIf(
                r -> r.getName() == RoleName.ROLE_MODERATOR);
        userRepository.save(user);
        logAdminAction(adminUsername, "DEMOTE_MODERATOR", userId,
                "Removed moderator role");
    }

    // ── SEARCH USERS ───────────────────────────────────────
    @Override
    public PagedResponse<UserSummaryResponse> searchUsers(
            String keyword, int page, int size) {
        // FIX: Previously called userRepository.findAll(pageable) and filtered
        //      in Java — pagination was wrong (first 20 users filtered, not first
        //      20 matching users). Now uses a proper DB LIKE query so pagination
        //      is correct and the result set is bounded.
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        Page<User> users = userRepository.searchByKeywordContaining(
                keyword, pageable);
        return buildUserPage(users);
    }

    // ── HELPERS ────────────────────────────────────────────
    private UserSummaryResponse toUserSummary(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roleNames)
                .reputationPoints(user.getReputationPoints())
                .isActive(user.getIsActive())
                .isBanned(user.getIsBanned())
                .emailVerified(user.getEmailVerified())
                .postCount(postRepository.countByAuthorIdAndIsDeletedFalse(
                        user.getId()))
                .warningCount(warningRepository.countByUserId(user.getId()))
                .createdAt(user.getCreatedAt())
                .build();
    }

    private PagedResponse<UserSummaryResponse> buildUserPage(Page<User> p) {
        List<UserSummaryResponse> content = p.getContent().stream()
                .map(this::toUserSummary).collect(Collectors.toList());
        return PagedResponse.<UserSummaryResponse>builder()
                .content(content)
                .pageNumber(p.getNumber()).pageSize(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages()).last(p.isLast())
                .build();
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + id));
    }

    private void logAdminAction(String adminUsername, String action,
                                 Long targetId, String details) {
        userRepository.findByUsername(adminUsername).ifPresent(admin ->
            logRepository.save(ModerationLog.builder()
                    .moderator(admin)
                    .action(action)
                    .targetType("USER")
                    .targetId(targetId)
                    .details(details)
                    .build())
        );
    }
}
