package com.techforum.service;
import com.techforum.dto.request.ChangePasswordRequest;
import com.techforum.dto.request.UpdateProfileRequest;
import com.techforum.dto.response.PagedResponse;
import com.techforum.dto.response.PostResponse;
import com.techforum.dto.response.UserProfileResponse;
public interface UserProfileService {
    UserProfileResponse getProfile(Long userId);
    UserProfileResponse getMyProfile(String username);
    UserProfileResponse updateProfile(String username, UpdateProfileRequest request);
    void changePassword(String username, ChangePasswordRequest request);
    PagedResponse<PostResponse> getUserPosts(Long userId, int page, int size);
    PagedResponse<PostResponse> getMyBookmarks(String username, int page, int size);
}
