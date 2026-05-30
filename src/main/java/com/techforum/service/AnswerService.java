package com.techforum.service;
import com.techforum.dto.request.CreateAnswerRequest;
import com.techforum.dto.request.UpdateAnswerRequest;
import com.techforum.dto.response.AnswerResponse;
import java.util.List;
public interface AnswerService {
    AnswerResponse createAnswer(Long postId, CreateAnswerRequest request, String username);
    AnswerResponse updateAnswer(Long answerId, UpdateAnswerRequest request, String username);
    void deleteAnswer(Long answerId, String username);
    List<AnswerResponse> getAnswersByPost(Long postId, String username);
    AnswerResponse voteAnswer(Long answerId, String voteType, String username);
    AnswerResponse verifyAnswer(Long answerId, String moderatorUsername);
    AnswerResponse unverifyAnswer(Long answerId, String moderatorUsername);
}
