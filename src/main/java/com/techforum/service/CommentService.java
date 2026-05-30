package com.techforum.service;
import com.techforum.dto.request.CreateCommentRequest;
import com.techforum.dto.request.UpdateCommentRequest;
import com.techforum.dto.response.CommentResponse;
import java.util.List;
public interface CommentService {
    CommentResponse addCommentToPost(Long postId, CreateCommentRequest request, String username);
    CommentResponse addCommentToAnswer(Long answerId, CreateCommentRequest request, String username);
    CommentResponse updateComment(Long commentId, UpdateCommentRequest request, String username);
    void deleteComment(Long commentId, String username);
    List<CommentResponse> getCommentsByPost(Long postId);
    List<CommentResponse> getCommentsByAnswer(Long answerId);
    List<CommentResponse> getReplies(Long parentCommentId);
}
