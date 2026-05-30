package com.techforum.controller;

import com.techforum.dto.request.CreateCommentRequest;
import com.techforum.dto.request.UpdateCommentRequest;
import com.techforum.dto.response.ApiResponse;
import com.techforum.dto.response.CommentResponse;
import com.techforum.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // ── GET COMMENTS ON POST ───────────────────────────────
    // GET /api/posts/{postId}/comments  (public)
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getPostComments(
            @PathVariable Long postId) {

        return ResponseEntity.ok(ApiResponse.success("Comments fetched",
                commentService.getCommentsByPost(postId)));
    }

    // ── ADD COMMENT ON POST ────────────────────────────────
    // POST /api/posts/{postId}/comments
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addCommentToPost(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        CommentResponse response = commentService.addCommentToPost(
                postId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added", response));
    }

    // ── GET COMMENTS ON ANSWER ─────────────────────────────
    // GET /api/answers/{answerId}/comments  (public)
    @GetMapping("/answers/{answerId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getAnswerComments(
            @PathVariable Long answerId) {

        return ResponseEntity.ok(ApiResponse.success("Comments fetched",
                commentService.getCommentsByAnswer(answerId)));
    }

    // ── ADD COMMENT ON ANSWER ──────────────────────────────
    // POST /api/answers/{answerId}/comments
    // FIX: This now sends a notification to the answer author (fixed in
    //      CommentServiceImpl.addCommentToAnswer).
    @PostMapping("/answers/{answerId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addCommentToAnswer(
            @PathVariable Long answerId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        CommentResponse response = commentService.addCommentToAnswer(
                answerId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added", response));
    }

    // ── GET REPLIES TO A COMMENT ───────────────────────────
    // GET /api/comments/{commentId}/replies  (public)
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getReplies(
            @PathVariable Long commentId) {

        return ResponseEntity.ok(ApiResponse.success("Replies fetched",
                commentService.getReplies(commentId)));
    }

    // ── UPDATE COMMENT ─────────────────────────────────────
    // PUT /api/comments/{commentId}
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success("Comment updated",
                commentService.updateComment(
                        commentId, request, userDetails.getUsername())));
    }

    // ── DELETE COMMENT ─────────────────────────────────────
    // DELETE /api/comments/{commentId}
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        commentService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Comment deleted"));
    }
}
