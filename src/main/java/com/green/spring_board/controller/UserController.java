package com.green.spring_board.controller;

import com.green.spring_board.dto.*;
import com.green.spring_board.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        userService.signup(signupRequest);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest httpServletRequest
    ){
        int userId = userService.login(loginRequest);
        HttpSession session = httpServletRequest.getSession();
        httpServletRequest.changeSessionId();
        session.setAttribute("userId", userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request
    ){
        HttpSession session = request.getSession(false);

        session.invalidate();
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyInfoResponse>> getCurrentUser(
            HttpServletRequest httpServletRequest
    ){
        // 1. 이 사람의 세션을 가져옴
        HttpSession session = httpServletRequest.getSession(false);

        // 2. 세션에서 유저 아이디 뽑아옴
        int userId = (int) session.getAttribute("userId");
        MyInfoResponse response = userService.getUserInfo(userId);

        return ResponseEntity.ok().body(ApiResponse.ok(response));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(
            HttpServletRequest request,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest

    ){
        HttpSession session = request.getSession(false);

        int userId = (int) session.getAttribute("userId");
        userService.updateUserInfo(userId, userUpdateRequest);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 유저 탈퇴 기능
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            HttpServletRequest request
    ){
        HttpSession session = request.getSession(false);

        int userId = (int) session.getAttribute("userId");

        // 1. DB 삭제
        userService.deleteUser(userId);
        // 2. 세션 비활성화
        session.invalidate();

        return ResponseEntity.ok(ApiResponse.ok());
    }
}