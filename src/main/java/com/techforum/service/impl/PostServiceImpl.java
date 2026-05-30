package com.techforum.service.impl;

import com.techforum.config.PostMapper;
import com.techforum.dto.request.CreatePostRequest;
import com.techforum.dto.request.UpdatePostRequest;
import com.techforum.dto.response.PagedResponse;
import com.techforum.dto.response.PostResponse;
import com.techforum.entity.*;
import com.techforum.enums.PostStatus;
import com.techforum.enums.VoteType;
import com.techforum.exception.BadRequestException;
import com.techforum.exception.ResourceNotFoundException;
import com.techforum.repository.*;
import com.techforum.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    @Autowired private PostRepository     postRepository;
    @Autowired private UserRepository     userRepository;
    @Autowired private TagRepository      tagRepository;
    @Autowired private VoteRepository     voteRepository;
    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private PostMapper         postMapper;

    // ── CREATE ─────────────────────────────────────────────
    @Override
    @Transactional
    public PostResponse createPost(CreatePostRequest request, String username) {
        User author = getUser(username);
        Set<Tag> tags = resolveTags(request.getTags());

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(author)
                .tags(tags)
                .status(PostStatus.OPEN)
                .voteCount(0).viewCount(0).answerCount(0)
                .isPinned(false).hasVerifiedAnswer(false)
                .isDeleted(false).moderationStatus("APPROVED")
                .build();

        Post saved = postRepository.save(post);
        tags.forEach(tag -> {
            tag.setUsageCount(tag.getUsageCount() + 1);
            tagRepository.save(tag);
        });
        return postMapper.toPostResponse(saved);
    }

    // ── GET BY ID ──────────────────────────────────────────
    @Override
    @Transactional
    public PostResponse getPostById(Long postId, String username) {
        Post post = getActivePost(postId);
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        PostResponse response = postMapper.toPostResponse(post);
        if (username != null) {
            userRepository.findByUsername(username).ifPresent(user -> {
                response.setIsBookmarked(
                        bookmarkRepository.existsByUserIdAndPostId(
                                user.getId(), postId));
                voteRepository.findByUserIdAndPostId(user.getId(), postId)
                        .ifPresent(v -> response.setUserVote(v.getVoteType().name()));
            });
        }
        return response;
    }

    // ── UPDATE ─────────────────────────────────────────────
    @Override
    @Transactional
    public PostResponse updatePost(Long postId, UpdatePostRequest request,
                                   String username) {
        Post post = getActivePost(postId);
        User user = getUser(username);

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"));
        if (!post.getAuthor().getId().equals(user.getId()) && !isAdmin) {
            throw new BadRequestException(
                    "You are not allowed to edit this post");
        }

        post.getTags().forEach(tag -> {
            tag.setUsageCount(Math.max(0, tag.getUsageCount() - 1));
            tagRepository.save(tag);
        });
        Set<Tag> newTags = resolveTags(request.getTags());
        newTags.forEach(tag -> {
            tag.setUsageCount(tag.getUsageCount() + 1);
            tagRepository.save(tag);
        });

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setTags(newTags);
        return postMapper.toPostResponse(postRepository.save(post));
    }

    // ── DELETE ─────────────────────────────────────────────
    @Override
    @Transactional
    public void deletePost(Long postId, String username) {
        Post post = getActivePost(postId);
        User user = getUser(username);

        boolean isAdminOrMod = user.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN")
                        || r.getName().name().equals("ROLE_MODERATOR"));
        if (!post.getAuthor().getId().equals(user.getId()) && !isAdminOrMod) {
            throw new BadRequestException(
                    "You are not allowed to delete this post");
        }
        post.setIsDeleted(true);
        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
    }

    // ── GET ALL ────────────────────────────────────────────
    @Override
    public PagedResponse<PostResponse> getAllPosts(int page, int size,
                                                   String sortBy) {
        Page<Post> posts;

        // FIX: Previously the "votes" and "views" cases created a PageRequest
        //      with a Sort, then passed it to
        //      findByIsDeletedFalseOrderByIsPinnedDescCreatedAtDesc() whose
        //      method-name sort takes priority — the Sort param was ignored.
        //      Now each case calls the correct (or a proper @Query) method.
        switch (sortBy) {
            case "votes" -> {
                // Fix 4: Use findByIsDeletedFalse(Pageable) — method-name OrderBy
                // conflicts with Pageable sort; this method has no fixed sort.
                Pageable p = PageRequest.of(page, size,
                        Sort.by(Sort.Direction.DESC, "voteCount")
                            .and(Sort.by(Sort.Direction.DESC, "createdAt")));
                posts = postRepository.findByIsDeletedFalse(p);
            }
            case "views" -> {
                Pageable p = PageRequest.of(page, size,
                        Sort.by(Sort.Direction.DESC, "viewCount")
                            .and(Sort.by(Sort.Direction.DESC, "createdAt")));
                posts = postRepository.findByIsDeletedFalse(p);
            }
            case "trending" -> posts = postRepository.findTrending(
                    LocalDateTime.now().minusDays(7),
                    PageRequest.of(page, size));
            default -> posts = postRepository
                    .findByIsDeletedFalseOrderByIsPinnedDescCreatedAtDesc(
                            PageRequest.of(page, size));
        }
        return buildPagedResponse(posts);
    }

    // ── SEARCH ─────────────────────────────────────────────
    @Override
    public PagedResponse<PostResponse> searchPosts(String keyword, String tag,
                                                    String author, String status,
                                                    int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts;

        if (tag != null && !tag.isEmpty()) {
            posts = postRepository.findByTagName(tag, pageable);
        } else if (author != null && !author.isEmpty()) {
            posts = postRepository.findByAuthorUsername(author, pageable);
        } else if (status != null && !status.isEmpty()) {
            // FIX: Validate status string before parsing
            PostStatus postStatus;
            try {
                postStatus = PostStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                        "Invalid status. Must be OPEN, CLOSED, SOLVED, or DELETED");
            }
            posts = postRepository.findByStatusAndIsDeletedFalse(postStatus, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            posts = postRepository.searchByKeyword(keyword, pageable);
        } else {
            posts = postRepository
                    .findByIsDeletedFalseOrderByIsPinnedDescCreatedAtDesc(pageable);
        }
        return buildPagedResponse(posts);
    }

    // ── POSTS BY USER ──────────────────────────────────────
    @Override
    public PagedResponse<PostResponse> getPostsByUser(Long userId,
                                                       int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return buildPagedResponse(
                postRepository.findByAuthorIdAndIsDeletedFalse(userId, pageable));
    }

    // ── VOTE ───────────────────────────────────────────────
    @Override
    @Transactional
    public PostResponse votePost(Long postId, String voteTypeStr,
                                 String username) {
        Post post = getActivePost(postId);
        User user = getUser(username);

        if (post.getAuthor().getId().equals(user.getId())) {
            throw new BadRequestException(
                    "You cannot vote on your own post");
        }

        // FIX: Validate voteType string — previously an invalid value threw
        //      an uncaught IllegalArgumentException → 500 response.
        VoteType voteType;
        try {
            voteType = VoteType.valueOf(voteTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Invalid vote type. Must be UPVOTE or DOWNVOTE");
        }

        Optional<Vote> existingVote =
                voteRepository.findByUserIdAndPostId(user.getId(), postId);

        boolean toggled = false;

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if (vote.getVoteType() == voteType) {
                voteRepository.delete(vote);
                int adj = voteType == VoteType.UPVOTE ? -1 : 1;
                post.setVoteCount(post.getVoteCount() + adj);
                toggled = true;
            } else {
                vote.setVoteType(voteType);
                voteRepository.save(vote);
                int adj = voteType == VoteType.UPVOTE ? 2 : -2;
                post.setVoteCount(post.getVoteCount() + adj);
            }
        } else {
            voteRepository.save(Vote.builder()
                    .user(user).post(post).voteType(voteType).build());
            int adj = voteType == VoteType.UPVOTE ? 1 : -1;
            post.setVoteCount(post.getVoteCount() + adj);

            User author   = post.getAuthor();
            int repChange = voteType == VoteType.UPVOTE ? 10 : -2;
            author.setReputationPoints(
                    Math.max(0, author.getReputationPoints() + repChange));
            userRepository.save(author);
        }

        PostResponse response = postMapper.toPostResponse(postRepository.save(post));

        // FIX: Set userVote on response so the frontend knows the new vote state.
        //      Previously votePost() never set userVote — the client had to
        //      refetch the post to learn whether its vote was recorded.
        //      Also: when toggled off, userVote must be null.
        if (!toggled) {
            response.setUserVote(voteType.name());
        }
        // toggled == true → userVote stays null (vote was removed)

        return response;
    }

    // ── BOOKMARK ───────────────────────────────────────────
    @Override
    @Transactional
    public PostResponse bookmarkPost(Long postId, String username) {
        Post post = getActivePost(postId);
        User user = getUser(username);

        Optional<Bookmark> existing =
                bookmarkRepository.findByUserIdAndPostId(user.getId(), postId);
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
        } else {
            bookmarkRepository.save(
                    Bookmark.builder().user(user).post(post).build());
        }

        PostResponse response = postMapper.toPostResponse(post);
        response.setIsBookmarked(existing.isEmpty());

        // FIX: Also carry the user's current vote on bookmark toggle responses
        //      so the frontend doesn't lose the vote state on UI refresh.
        voteRepository.findByUserIdAndPostId(user.getId(), postId)
                .ifPresent(v -> response.setUserVote(v.getVoteType().name()));

        return response;
    }

    // ── BOOKMARKED POSTS ───────────────────────────────────
    @Override
    public PagedResponse<PostResponse> getBookmarkedPosts(String username,
                                                           int page, int size) {
        User user = getUser(username);
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        Page<Bookmark> bookmarks =
                bookmarkRepository.findByUserId(user.getId(), pageable);

        List<PostResponse> responses = bookmarks.getContent().stream()
                .map(b -> {
                    PostResponse r = postMapper.toPostResponse(b.getPost());
                    r.setIsBookmarked(true);
                    return r;
                })
                .collect(Collectors.toList());

        return PagedResponse.<PostResponse>builder()
                .content(responses)
                .pageNumber(bookmarks.getNumber()).pageSize(bookmarks.getSize())
                .totalElements(bookmarks.getTotalElements())
                .totalPages(bookmarks.getTotalPages()).last(bookmarks.isLast())
                .build();
    }

    // ── TRENDING ───────────────────────────────────────────
    @Override
    public PagedResponse<PostResponse> getTrendingPosts(int page, int size) {
        return buildPagedResponse(postRepository.findTrending(
                LocalDateTime.now().minusDays(7),
                PageRequest.of(page, size)));
    }

    // ── PRIVATE HELPERS ────────────────────────────────────
    private Post getActivePost(Long postId) {
        return postRepository.findById(postId)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + postId));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));
    }

    private Set<Tag> resolveTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return new HashSet<>();
        return tagNames.stream()
                .map(name -> tagRepository.findByName(name.toLowerCase())
                        .orElseGet(() -> tagRepository.save(
                                Tag.builder().name(name.toLowerCase())
                                        .usageCount(0).build())))
                .collect(Collectors.toSet());
    }

    private PagedResponse<PostResponse> buildPagedResponse(Page<Post> page) {
        List<PostResponse> content = page.getContent().stream()
                .map(postMapper::toPostResponse)
                .collect(Collectors.toList());
        return PagedResponse.<PostResponse>builder()
                .content(content)
                .pageNumber(page.getNumber()).pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages()).last(page.isLast())
                .build();
    }
}
