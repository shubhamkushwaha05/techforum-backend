package com.techforum.controller;

import com.techforum.dto.request.CreatePostRequest;
import com.techforum.dto.request.UpdatePostRequest;
import com.techforum.dto.response.*;
import com.techforum.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class PostController {

    @Autowired
    private PostService postService;

    // ── GET ALL POSTS ──────────────────────────────────────
    // GET /api/posts?page=0&size=10&sortBy=newest  (public)
    // sortBy: newest | votes | views | trending
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getAllPosts(
            @RequestParam(defaultValue = "0")      int    page,
            @RequestParam(defaultValue = "10")     int    size,
            @RequestParam(defaultValue = "newest") String sortBy) {

        return ResponseEntity.ok(ApiResponse.success("Posts fetched",
                postService.getAllPosts(page, size, sortBy)));
    }

    // ── GET POST BY ID ─────────────────────────────────────
    // GET /api/posts/{id}  (public)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(ApiResponse.success("Post fetched",
                postService.getPostById(id, username)));
    }

    // ── CREATE POST ────────────────────────────────────────
    // POST /api/posts
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        PostResponse response =
                postService.createPost(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created successfully", response));
    }

    // ── UPDATE POST ────────────────────────────────────────
    // PUT /api/posts/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success("Post updated",
                postService.updatePost(id, request, userDetails.getUsername())));
    }

    // ── DELETE POST ────────────────────────────────────────
    // DELETE /api/posts/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        postService.deletePost(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Post deleted"));
    }

    // ── SEARCH ─────────────────────────────────────────────
    // GET /api/posts/search?keyword=java&tag=spring&author=john&status=OPEN  (public)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success("Search results",
                postService.searchPosts(keyword, tag, author,
                        status, page, size)));
    }

    // ── TRENDING ───────────────────────────────────────────
    // GET /api/posts/trending  (public)
    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getTrending(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success("Trending posts",
                postService.getTrendingPosts(page, size)));
    }

    // ── POSTS BY USER ──────────────────────────────────────
    // GET /api/posts/user/{userId}  (public)
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getPostsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success("User posts",
                postService.getPostsByUser(userId, page, size)));
    }

    // ── VOTE ───────────────────────────────────────────────
    // POST /api/posts/{id}/vote?type=upvote
    // FIX: Invalid type now returns 400 (BadRequestException in service)
    //      instead of 500 (uncaught IllegalArgumentException).
    //      Response now includes userVote field so frontend knows the new state.
    @PostMapping("/{id}/vote")
    public ResponseEntity<ApiResponse<PostResponse>> votePost(
            @PathVariable Long id,
            @RequestParam String type,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success("Vote recorded",
                postService.votePost(id, type, userDetails.getUsername())));
    }

    // ── BOOKMARK ───────────────────────────────────────────
    // POST /api/posts/{id}/bookmark
    @PostMapping("/{id}/bookmark")
    public ResponseEntity<ApiResponse<PostResponse>> bookmarkPost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success("Bookmark toggled",
                postService.bookmarkPost(id, userDetails.getUsername())));
    }

    // ── MY BOOKMARKS ───────────────────────────────────────
    // GET /api/posts/bookmarks/me
    @GetMapping("/bookmarks/me")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getMyBookmarks(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success("Bookmarks fetched",
                postService.getBookmarkedPosts(
                        userDetails.getUsername(), page, size)));
    }
}
