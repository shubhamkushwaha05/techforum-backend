package com.techforum.controller;

import com.techforum.dto.request.CreateAnswerRequest;
import com.techforum.dto.request.UpdateAnswerRequest;
import com.techforum.dto.response.AnswerResponse;
import com.techforum.dto.response.ApiResponse;
import com.techforum.service.AnswerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    // ── GET ANSWERS FOR A POST ─────────────────────────────
    // GET /api/posts/{postId}/answers  (public)
    @GetMapping("/api/posts/{postId}/answers")
    public ResponseEntity<ApiResponse<List<AnswerResponse>>> getAnswers(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails != null
                ? userDetails.getUsername() : null;
        return ResponseEntity.ok(ApiResponse.success("Answers fetched",
                answerService.getAnswersByPost(postId, username)));
    }

    // ── ADD ANSWER ─────────────────────────────────────────
    // POST /api/posts/{postId}/answers
    @PostMapping("/api/posts/{postId}/answers")
    public ResponseEntity<ApiResponse<AnswerResponse>> addAnswer(
            @PathVariable Long postId,
            @Valid @RequestBody CreateAnswerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        AnswerResponse response = answerService.createAnswer(
                postId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Answer posted", response));
    }

    // ── UPDATE ANSWER ──────────────────────────────────────
    // PUT /api/answers/{answerId}
    @PutMapping("/api/answers/{answerId}")
    public ResponseEntity<ApiResponse<AnswerResponse>> updateAnswer(
            @PathVariable Long answerId,
            @Valid @RequestBody UpdateAnswerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success("Answer updated",
                answerService.updateAnswer(
                        answerId, request, userDetails.getUsername())));
    }

    // ── DELETE ANSWER ──────────────────────────────────────
    // DELETE /api/answers/{answerId}
    @DeleteMapping("/api/answers/{answerId}")
    public ResponseEntity<ApiResponse<Void>> deleteAnswer(
            @PathVariable Long answerId,
            @AuthenticationPrincipal UserDetails userDetails) {

        answerService.deleteAnswer(answerId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Answer deleted"));
    }

    // ── VOTE ANSWER ────────────────────────────────────────
    // POST /api/answers/{answerId}/vote?type=upvote
    // FIX: BadRequestException is now thrown from AnswerServiceImpl for invalid
    //      vote type strings — caught by GlobalExceptionHandler → clean 400.
    @PostMapping("/api/answers/{answerId}/vote")
    public ResponseEntity<ApiResponse<AnswerResponse>> voteAnswer(
            @PathVariable Long answerId,
            @RequestParam String type,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success("Vote recorded",
                answerService.voteAnswer(
                        answerId, type, userDetails.getUsername())));
    }

    // ── VERIFY ANSWER (Moderator / Admin only) ─────────────
    // POST /api/moderator/verify-answer/{answerId}
    @PostMapping("/api/moderator/verify-answer/{answerId}")
    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    public ResponseEntity<ApiResponse<AnswerResponse>> verifyAnswer(
            @PathVariable Long answerId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success("Answer verified",
                answerService.verifyAnswer(
                        answerId, userDetails.getUsername())));
    }

    // ── UNVERIFY ANSWER (Moderator / Admin only) ───────────
    // POST /api/moderator/unverify-answer/{answerId}
    @PostMapping("/api/moderator/unverify-answer/{answerId}")
    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    public ResponseEntity<ApiResponse<AnswerResponse>> unverifyAnswer(
            @PathVariable Long answerId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success("Answer unverified",
                answerService.unverifyAnswer(
                        answerId, userDetails.getUsername())));
    }
}
