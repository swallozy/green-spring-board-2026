package com.green.spring_board.service;

import com.green.spring_board.dto.CommentCreateRequest;
import com.green.spring_board.dto.CommentResponse;
import com.green.spring_board.dto.CommentUpdateRequest;
import com.green.spring_board.entity.Board;
import com.green.spring_board.entity.Comment;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.AuthorizationFailureException;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.repository.BoardRepository;
import com.green.spring_board.repository.CommentRepository;
import com.green.spring_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public int createComment(int boardId, CommentCreateRequest request, int userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthenticatedException("로그인이 필요합니다."));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setBoard(board);
        comment.setUser(user);

        return commentRepository.save(comment).getId();
    }

    public List<CommentResponse> getComments(int boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }

        List<Comment> comments = commentRepository.findAllByBoardIdOrderByCreatedDatetimeAsc(boardId);
        List<CommentResponse> responses = new ArrayList<>();

        for (Comment comment : comments) {
            responses.add(new CommentResponse(
                    comment.getId(),
                    comment.getContent(),
                    comment.getUser().getId(),
                    comment.getUser().getNickname(),
                    comment.getCreatedDatetime(),
                    comment.getUpdatedDatetime()
            ));
        }
        return responses;
    }

    public void updateComment(int commentId, CommentUpdateRequest request, int userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다."));

        if (comment.getUser().getId() != userId) {
            throw new AuthorizationFailureException("댓글 수정 권한이 없습니다.");
        }

        comment.setContent(request.getContent());
        commentRepository.save(comment);
    }

    public void deleteComment(int commentId, int userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다."));

        if (comment.getUser().getId() != userId) {
            throw new AuthorizationFailureException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.deleteById(commentId);
    }
}