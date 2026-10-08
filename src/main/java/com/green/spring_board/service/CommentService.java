package com.green.spring_board.service;

import com.green.spring_board.dto.CommentCreateRequest;
import com.green.spring_board.dto.CommentResponse;
import com.green.spring_board.dto.CommentUpdateRequest;
import com.green.spring_board.entity.Board;
import com.green.spring_board.entity.Comment;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.AuthorizationFailureException;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.repository.BoardRepository;
import com.green.spring_board.repository.CommentRepository;
import com.green.spring_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // 댓글 등록
    public void createComment(
            CommentCreateRequest commentCreateRequest,
            int userId,
            int boardId
    ) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Board> boardOptional = boardRepository.findById(boardId);

        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        if (boardOptional.isEmpty()) {
            throw new ResourceNotFoundException("Board not found");
        }

        User user = userOptional.get();
        Board board = boardOptional.get();

        Comment comment = new Comment();
        comment.setContent(commentCreateRequest.getContent());
        comment.setUser(user);
        comment.setBoard(board);
        commentRepository.save(comment);
    }

    // 특정 게시글의 댓글 목록 조회
    public List<CommentResponse> getComments(int boardId) {
        Optional<Board> boardOptional = boardRepository.findById(boardId);
        if (boardOptional.isEmpty()) {
            throw new ResourceNotFoundException("Board not found");
        }

        List<Comment> comments = commentRepository.findAllByBoardIdOrderByCreatedDatetimeAsc(boardId);
        List<CommentResponse> responses = new ArrayList<>();

        for (Comment comment : comments) {
            responses.add(
                    new CommentResponse(
                            comment.getId(),
                            comment.getContent(),
                            comment.getUser().getId(),
                            comment.getUser().getNickname(),
                            comment.getCreatedDatetime(),
                            comment.getUpdatedDatetime()
                    )
            );
        }
        return responses;
    }

    // 댓글 수정
    public void updateComment(int commentId, CommentUpdateRequest commentUpdateRequest, int userId) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if (commentOptional.isEmpty()) {
            throw new ResourceNotFoundException("Comment not found");
        }

        Comment comment = commentOptional.get();

        if (comment.getUser().getId() != userId) {
            throw new AuthorizationFailureException("권한이 없습니다.");
        }

        if (commentUpdateRequest.getContent() != null && !commentUpdateRequest.getContent().isBlank()) {
            comment.setContent(commentUpdateRequest.getContent());
        }

        commentRepository.save(comment);
    }

    // 댓글 삭제
    public void deleteComment(int commentId, int userId) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if (commentOptional.isEmpty()) {
            throw new ResourceNotFoundException("Comment not found");
        }

        Comment comment = commentOptional.get();

        if (comment.getUser().getId() != userId) {
            throw new AuthorizationFailureException("권한이 없습니다.");
        }

        commentRepository.deleteById(commentId);
    }
}