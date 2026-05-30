package com.techforum.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techforum.config.PostMapper;
import com.techforum.dto.request.ChangePasswordRequest;
import com.techforum.dto.request.UpdateProfileRequest;
import com.techforum.dto.response.PagedResponse;
import com.techforum.dto.response.PostResponse;
import com.techforum.dto.response.UserProfileResponse;
import com.techforum.entity.Bookmark;
import com.techforum.entity.Post;
import com.techforum.entity.User;
import com.techforum.exception.BadRequestException;
import com.techforum.exception.ResourceNotFoundException;
import com.techforum.repository.AnswerRepository;
import com.techforum.repository.BookmarkRepository;
import com.techforum.repository.PostRepository;
import com.techforum.repository.UserRepository;
import com.techforum.repository.WarningRepository;
import com.techforum.service.UserProfileService;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired private UserRepository     userRepository;
    @Autowired private PostRepository     postRepository;
    @Autowired private AnswerRepository   answerRepository;
    @Autowired private WarningRepository  warningRepository;
    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private PasswordEncoder    passwordEncoder;
    @Autowired private PostMapper         postMapper;

    // ── GET PUBLIC PROFILE ─────────────────────────────────
    @Override
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + userId));
        return buildProfile(user);
    }

    // ── GET MY PROFILE ─────────────────────────────────────
    @Override
    public UserProfileResponse getMyProfile(String username) {
        return buildProfile(getUser(username));
    }

    // ── UPDATE PROFILE ─────────────────────────────────────
    @Override
    @Transactional
    public UserProfileResponse updateProfile(String username,
                                              UpdateProfileRequest request) {
        User user = getUser(username);
        if (request.getFullName()    != null) user.setFullName(request.getFullName());
        if (request.getBio()         != null) user.setBio(request.getBio());
        if (request.getProfileImage()!= null) user.setProfileImage(request.getProfileImage());
        return buildProfile(userRepository.save(user));
    }

    // ── CHANGE PASSWORD ────────────────────────────────────
    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = getUser(username);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException(
                    "New password must differ from current password");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ── GET USER POSTS ─────────────────────────────────────
    @Override
    public PagedResponse<PostResponse> getUserPosts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        Page<Post> posts = postRepository
                .findByAuthorIdAndIsDeletedFalse(userId, pageable);
        List<PostResponse> content = posts.getContent().stream()
                .map(postMapper::toPostResponse)
                .collect(Collectors.toList());
        return PagedResponse.<PostResponse>builder()
                .content(content)
                .pageNumber(posts.getNumber()).pageSize(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages()).last(posts.isLast())
                .build();
    }

    // ── GET BOOKMARKS ──────────────────────────────────────
    @Override
    public PagedResponse<PostResponse> getMyBookmarks(String username,
                                                       int page, int size) {
        User user = getUser(username);
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        Page<Bookmark> bookmarks = bookmarkRepository
                .findByUserId(user.getId(), pageable);
        List<PostResponse> content = bookmarks.getContent().stream()
                .map(b -> {
                    PostResponse r = postMapper.toPostResponse(b.getPost());
                    r.setIsBookmarked(true);
                    return r;
                })
                .collect(Collectors.toList());
        return PagedResponse.<PostResponse>builder()
                .content(content)
                .pageNumber(bookmarks.getNumber()).pageSize(bookmarks.getSize())
                .totalElements(bookmarks.getTotalElements())
                .totalPages(bookmarks.getTotalPages()).last(bookmarks.isLast())
                .build();
    }

    // ── PRIVATE HELPERS ────────────────────────────────────
    private UserProfileResponse buildProfile(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        long totalPosts   = postRepository.countByAuthorIdAndIsDeletedFalse(user.getId());

        // FIX: Use dedicated count query — previously called answerRepository.findAll()
        //      which loaded EVERY answer in the database into memory just to count
        //      the ones belonging to this user. Now a single COUNT SQL query.
        long totalAnswers  = answerRepository.countByAuthorIdAndIsDeletedFalse(user.getId());
        long verified      = answerRepository.countVerifiedByAuthorId(user.getId());
        long warnings      = warningRepository.countByUserId(user.getId());

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .profileImage(user.getProfileImage())
                .bio(user.getBio())
                .reputationPoints(user.getReputationPoints())
                .roles(roles)
                .isActive(user.getIsActive())
                .isBanned(user.getIsBanned())
                .emailVerified(user.getEmailVerified())
                .totalPosts(totalPosts)
                .totalAnswers(totalAnswers)
                .totalVerifiedAnswers(verified)
                .warningCount(warnings)
                .reputationBadge(getBadge(user.getReputationPoints()))
                .createdAt(user.getCreatedAt())
                .build();
    }

    private String getBadge(int rep) {
        if (rep >= 1000) return "MASTER";
        if (rep >= 500)  return "EXPERT";
        if (rep >= 100)  return "CONTRIBUTOR";
        return "NEWCOMER";
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));
    }
}
