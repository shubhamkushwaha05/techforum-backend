package com.techforum.controller;

import com.techforum.dto.request.ChangePasswordRequest;
import com.techforum.dto.request.UpdateProfileRequest;
import com.techforum.dto.response.ApiResponse;
import com.techforum.dto.response.PagedResponse;
import com.techforum.dto.response.PostResponse;
import com.techforum.dto.response.UserProfileResponse;
import com.techforum.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    // GET /api/users/{userId}/profile  (public)
    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @PathVariable Long userId) {

        return ResponseEntity.ok(ApiResponse.success(
                "Profile fetched",
                userProfileService.getProfile(userId)));
    }

    // GET /api/users/me
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success(
                "Profile fetched",
                userProfileService.getMyProfile(userDetails.getUsername())));
    }

    // PUT /api/users/me
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success(
                "Profile updated",
                userProfileService.updateProfile(
                        userDetails.getUsername(), request)));
    }

    // PUT /api/users/me/password
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        userProfileService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully"));
    }

    // GET /api/users/{userId}/posts  (public)
    @GetMapping("/{userId}/posts")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                "User posts",
                userProfileService.getUserPosts(userId, page, size)));
    }

    // GET /api/users/me/bookmarks
    @GetMapping("/me/bookmarks")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getBookmarks(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                "Bookmarks fetched",
                userProfileService.getMyBookmarks(
                        userDetails.getUsername(), page, size)));
    }
}
