package com.green.spring_board.controller;

import com.green.spring_board.dto.ApiResponse;
import com.green.spring_board.dto.CommentCreateRequest;
import com.green.spring_board.dto.CommentResponse;
import com.green.spring_board.dto.CommentUpdateRequest;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@AllArgsConstructor
public class CommentController {
    private final CommentService commentService;

    private int getLoginUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        return (int) session.getAttribute("userId");
    }

    // 1. 특정 게시글에 댓글 등록
    @PostMapping("/api/board/{boardId}/comment")
    public ResponseEntity<ApiResponse<Void>> createComment(
            @PathVariable int boardId,
            @Valid @RequestBody CommentCreateRequest request,
            HttpServletRequest servletRequest
    ) {
        int userId = getLoginUserId(servletRequest);
        int commentId = commentService.createComment(boardId, request, userId);
        return ResponseEntity.created(URI.create("/api/comment/" + commentId)).body(ApiResponse.ok());
    }

    // 2. 특정 게시글의 댓글 목록 조회
    @GetMapping("/api/board/{boardId}/comment")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable int boardId) {
        List<CommentResponse> comments = commentService.getComments(boardId);
        return ResponseEntity.ok(ApiResponse.ok(comments));
    }

    // 3. 댓글 수정
    @PatchMapping("/api/comment/{id}")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @PathVariable int id,
            @Valid @RequestBody CommentUpdateRequest request,
            HttpServletRequest servletRequest
    ) {
        int userId = getLoginUserId(servletRequest);
        commentService.updateComment(id, request, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 4. 댓글 삭제
    @DeleteMapping("/api/comment/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable int id,
            HttpServletRequest servletRequest
    ) {
        int userId = getLoginUserId(servletRequest);
        commentService.deleteComment(id, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}