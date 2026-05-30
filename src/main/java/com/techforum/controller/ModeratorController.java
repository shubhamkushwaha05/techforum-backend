package com.techforum.controller;

import com.techforum.dto.request.ResolveReportRequest;
import com.techforum.dto.request.WarnUserRequest;
import com.techforum.dto.response.*;
import com.techforum.service.ModeratorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/moderator")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
public class ModeratorController {

    @Autowired
    private ModeratorService moderatorService;

    // GET /api/moderator/stats
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ModeratorStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(
                "Stats fetched", moderatorService.getStats()));
    }

    // GET /api/moderator/reports?status=PENDING&page=0&size=10
    // FIX: Invalid status string now returns 400 (BadRequestException in service)
    //      instead of 500 (uncaught IllegalArgumentException from valueOf).
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<PagedResponse<ReportResponse>>> getReports(
            @RequestParam(defaultValue = "ALL")  String status,
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "10")   int    size) {

        return ResponseEntity.ok(ApiResponse.success("Reports fetched",
                moderatorService.getReports(status, page, size)));
    }

    // PUT /api/moderator/reports/{id}/resolve
    @PutMapping("/reports/{id}/resolve")
    public ResponseEntity<ApiResponse<ReportResponse>> resolveReport(
            @PathVariable Long id,
            @Valid @RequestBody ResolveReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success("Report resolved",
                moderatorService.resolveReport(
                        id, request, userDetails.getUsername())));
    }

    // POST /api/moderator/pin-post/{postId}
    @PostMapping("/pin-post/{postId}")
    public ResponseEntity<ApiResponse<Void>> pinPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        moderatorService.pinPost(postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Post pinned"));
    }

    // POST /api/moderator/unpin-post/{postId}
    @PostMapping("/unpin-post/{postId}")
    public ResponseEntity<ApiResponse<Void>> unpinPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        moderatorService.unpinPost(postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Post unpinned"));
    }

    // POST /api/moderator/warn-user/{userId}
    @PostMapping("/warn-user/{userId}")
    public ResponseEntity<ApiResponse<Void>> warnUser(
            @PathVariable Long userId,
            @Valid @RequestBody WarnUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        moderatorService.warnUser(userId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Warning issued"));
    }
}
