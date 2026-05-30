package com.techforum.service.impl;

import com.techforum.dto.request.ResolveReportRequest;
import com.techforum.dto.request.WarnUserRequest;
import com.techforum.dto.response.*;
import com.techforum.entity.*;
import com.techforum.enums.ReportStatus;
import com.techforum.exception.BadRequestException;
import com.techforum.exception.ResourceNotFoundException;
import com.techforum.repository.*;
import com.techforum.service.ModeratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModeratorServiceImpl implements ModeratorService {

    @Autowired private ReportRepository        reportRepository;
    @Autowired private PostRepository          postRepository;
    @Autowired private UserRepository          userRepository;
    @Autowired private WarningRepository       warningRepository;
    @Autowired private ModerationLogRepository logRepository;
    @Autowired private AnswerRepository        answerRepository;

    // ── GET REPORTS ────────────────────────────────────────
    @Override
    public PagedResponse<ReportResponse> getReports(String status,
                                                     int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        Page<Report> reports;

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            ReportStatus rs = ReportStatus.valueOf(status.toUpperCase());
            reports = reportRepository.findByStatus(rs, pageable);
        } else {
            reports = reportRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        List<ReportResponse> content = reports.getContent().stream()
                .map(this::toReportResponse).collect(Collectors.toList());

        return PagedResponse.<ReportResponse>builder()
                .content(content)
                .pageNumber(reports.getNumber()).pageSize(reports.getSize())
                .totalElements(reports.getTotalElements())
                .totalPages(reports.getTotalPages()).last(reports.isLast())
                .build();
    }

    // ── RESOLVE REPORT ─────────────────────────────────────
    @Override
    @Transactional
    public ReportResponse resolveReport(Long reportId,
                                         ResolveReportRequest request,
                                         String moderatorUsername) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Report not found: " + reportId));

        // FIX: Validate action string before parsing — prevents 500 on bad input
        ReportStatus newStatus;
        try {
            newStatus = ReportStatus.valueOf(request.getAction().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Invalid action. Must be RESOLVED or DISMISSED");
        }

        if (newStatus == ReportStatus.PENDING) {
            throw new BadRequestException(
                    "Cannot set status back to PENDING");
        }

        User moderator = getUser(moderatorUsername);
        report.setStatus(newStatus);
        report.setResolvedBy(moderator);
        report.setResolutionNote(request.getResolutionNote());
        reportRepository.save(report);
        logAction(moderator, "RESOLVE_REPORT", "REPORT", reportId,
                "Status → " + newStatus);
        return toReportResponse(report);
    }

    // ── PIN POST ───────────────────────────────────────────
    @Override
    @Transactional
    public void pinPost(Long postId, String moderatorUsername) {
        Post post = getActivePost(postId);
        User moderator = getUser(moderatorUsername);
        post.setIsPinned(true);
        postRepository.save(post);
        logAction(moderator, "PIN_POST", "POST", postId, "Post pinned");
    }

    // ── UNPIN POST ─────────────────────────────────────────
    @Override
    @Transactional
    public void unpinPost(Long postId, String moderatorUsername) {
        Post post = getActivePost(postId);
        User moderator = getUser(moderatorUsername);
        post.setIsPinned(false);
        postRepository.save(post);
        logAction(moderator, "UNPIN_POST", "POST", postId, "Post unpinned");
    }

    // ── WARN USER ──────────────────────────────────────────
    @Override
    @Transactional
    public void warnUser(Long userId, WarnUserRequest request,
                         String moderatorUsername) {
        User target    = getUserById(userId);
        User moderator = getUser(moderatorUsername);
        Warning warning = Warning.builder()
                .user(target)
                .issuedBy(moderator)
                .reason(request.getReason())
                .build();
        warningRepository.save(warning);
        logAction(moderator, "WARN_USER", "USER", userId, request.getReason());
    }

    // ── STATS ──────────────────────────────────────────────
    @Override
    public ModeratorStatsResponse getStats() {
        long pending = reportRepository.countByStatus(ReportStatus.PENDING);

        // FIX: Replaced answerRepository.findAll().stream().filter() and
        //      postRepository.findAll().stream().filter() with COUNT @Query
        //      methods — previously loaded entire tables into memory.
        long verified = answerRepository.countAllVerified();
        long warnings = warningRepository.count();
        long pinned   = postRepository.countPinnedPosts();

        return ModeratorStatsResponse.builder()
                .pendingReports(pending)
                .verifiedAnswersTotal(verified)
                .warningsIssuedTotal(warnings)
                .pinnedPostsTotal(pinned)
                .myActionsToday(0L)
                .build();
    }

    // ── MY ACTIONS ─────────────────────────────────────────
    @Override
    public PagedResponse<ReportResponse> getMyActions(
            String moderatorUsername, int page, int size) {
        User moderator = getUser(moderatorUsername);
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        Page<Report> resolved = reportRepository.findByStatus(
                ReportStatus.RESOLVED, pageable);

        List<ReportResponse> content = resolved.getContent().stream()
                .filter(r -> r.getResolvedBy() != null &&
                        r.getResolvedBy().getId().equals(moderator.getId()))
                .map(this::toReportResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ReportResponse>builder()
                .content(content)
                .pageNumber(resolved.getNumber()).pageSize(resolved.getSize())
                .totalElements((long) content.size())
                .totalPages(1).last(true)
                .build();
    }

    // ── HELPERS ────────────────────────────────────────────
    private void logAction(User mod, String action,
                            String targetType, Long targetId, String details) {
        logRepository.save(ModerationLog.builder()
                .moderator(mod).action(action)
                .targetType(targetType).targetId(targetId)
                .details(details).build());
    }

    private ReportResponse toReportResponse(Report r) {
        return ReportResponse.builder()
                .id(r.getId())
                .reporterUsername(r.getReporter().getUsername())
                .reportType(r.getReportType().name())
                .targetId(r.getTargetId())
                .reason(r.getReason())
                .status(r.getStatus().name())
                .resolvedByUsername(r.getResolvedBy() != null
                        ? r.getResolvedBy().getUsername() : null)
                .resolutionNote(r.getResolutionNote())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private Post getActivePost(Long id) {
        return postRepository.findById(id)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found: " + id));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + id));
    }
}
