package com.green.spring_board.controller;

import com.green.spring_board.dto.ApiResponse;
import com.green.spring_board.dto.CommentCreateRequest;
import com.green.spring_board.dto.CommentResponse;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class CommentController {
    private final CommentService commentService;

    // 1. 특정 게시글에 댓글 등록
    @PostMapping("/board/{boardId}/comment")
    public ResponseEntity<ApiResponse<Void>> createComment(
            @PathVariable int boardId,
            @Valid @RequestBody CommentCreateRequest commentCreateRequest,
            HttpServletRequest httpServletRequest
    ) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        int userId = (int) session.getAttribute("userId");

        commentService.createComment(commentCreateRequest, userId, boardId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 2. 특정 게시글의 댓글 목록 조회 (getComments -> readComments 로 변경)
    @GetMapping("/board/{boardId}/comment")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable int boardId
    ) {
        List<CommentResponse> comments = commentService.readComments(boardId);
        return ResponseEntity.ok(ApiResponse.ok(comments));
    }

    // 3. 댓글 수정 (CommentCreateRequest 타입 및 (request, id, userId) 순서로 변경)
    @PatchMapping("/comment/{id}")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @PathVariable int id,
            @Valid @RequestBody CommentCreateRequest commentCreateRequest,
            HttpServletRequest httpServletRequest
    ) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        int userId = (int) session.getAttribute("userId");

        commentService.updateComment(commentCreateRequest, id, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 4. 댓글 삭제
    @DeleteMapping("/comment/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable int id,
            HttpServletRequest httpServletRequest
    ) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        int userId = (int) session.getAttribute("userId");

        commentService.deleteComment(id, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}