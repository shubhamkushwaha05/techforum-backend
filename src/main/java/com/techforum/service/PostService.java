package com.techforum.service;
import com.techforum.dto.request.CreatePostRequest;
import com.techforum.dto.request.UpdatePostRequest;
import com.techforum.dto.response.PagedResponse;
import com.techforum.dto.response.PostResponse;
public interface PostService {
    PostResponse createPost(CreatePostRequest request, String username);
    PostResponse getPostById(Long postId, String username);
    PostResponse updatePost(Long postId, UpdatePostRequest request, String username);
    void deletePost(Long postId, String username);
    PagedResponse<PostResponse> getAllPosts(int page, int size, String sortBy);
    PagedResponse<PostResponse> searchPosts(String keyword, String tag, String author, String status, int page, int size);
    PagedResponse<PostResponse> getPostsByUser(Long userId, int page, int size);
    PostResponse votePost(Long postId, String voteType, String username);
    PostResponse bookmarkPost(Long postId, String username);
    PagedResponse<PostResponse> getBookmarkedPosts(String username, int page, int size);
    PagedResponse<PostResponse> getTrendingPosts(int page, int size);
}
