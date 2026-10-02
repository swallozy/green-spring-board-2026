package com.green.spring_board.controller;

import com.green.spring_board.dto.LoginRequest;
import com.green.spring_board.dto.MyInfoResponse;
import com.green.spring_board.dto.SignupRequest;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.ResourceConflictException;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UnauthenticatedExecption;
import com.green.spring_board.exceptions.UserRequestException;
import com.green.spring_board.repository.UserRepository;
import com.green.spring_board.service.UserService;
import jakarta.servlet.ServletSecurityElement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequest signupRequest) {
        try {
            userService.signup(signupRequest);
            return ResponseEntity.ok().build();
        } catch (ResourceConflictException e) {
            return ResponseEntity.status(409).build();
        } catch (UserRequestException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest httpServletRequest) {
        try {
            int userId = userService.login(loginRequest);
            HttpSession session = httpServletRequest.getSession();
            httpServletRequest.changeSessionId();
            session.setAttribute("userId", userId);
            return ResponseEntity.ok().build();

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthenticatedExecption e) {
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<MyInfoResponse> getCurrentUser(
            HttpServletRequest httpServletRequest) {
        // 1. 이 사람의 세션을 가져옴
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }

        // 2. 세션에서 유저 아이디 뽑아옴
        int userId = (int) session.getAttribute("userId");

        try {
            MyInfoResponse response = userService.getUserInfo(userId);
            return ResponseEntity.ok().body(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        // 1. 기존 세션 가져오기 (없으면 새로 만들지 않음)
        HttpSession session = request.getSession(false);

        // 2. 세션이 없거나 세션 내 userId가 없는 경우 401 Unauthorized 반환
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }

        // 3. 세션 무효화 (로그아웃 처리)
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me")
    public ResponseEntity<Void> updateUserInfo(
            HttpServletRequest request,
            @RequestBody MyInfoResponse myInfoResponse)
    {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }

        int userId = (int) session.getAttribute("userId");

        try {
            userService.updateUserInfo(userId, myInfoResponse);
            return ResponseEntity.ok().build(); // 성공 시 200 OK
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build(); // 유저가 없으면 404
        } catch (ResourceConflictException e) {
            return ResponseEntity.status(409).build(); // 이메일 중복이면 409
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // 그 외 에러 500
        }
        /*### 단계별 상세 설명

        1. **세션 검증 (인증 확인)**
            * `request.getSession(false)`로 세션을 가져옵니다.
            * 세션이 없거나 세션 내부의 `userId`가 없다면 로그인하지 않은 사용자이므로 즉시 **401 Unauthorized**를 반환하고 작업을 중단합니다.

        2. 현재 로그인 사용자 조회
            * 세션에서 꺼낸 `userId`로 DB를 조회(`findById`)합니다.
            * DB에 유저가 존재하지 않는 비정상 상태라면 **404 Not Found**를 반환합니다.

        3. 이메일 변경 대상 여부 판단
            * 클라이언트가 보낸 새 이메일이 `null`이거나 공백이면 **이메일은 변경하지 않고 기존 값을 유지**합니다.
            * 클라이언트가 보낸 새 이메일이 **현재 내 이메일과 완전히 동일한 경우**에도 DB 중복 조회를 거치지 않고 그대로 통과시킵니다. (닉네임만 바꾸려고 할 때 내 이메일 때문에 중복 에러가 나는 것을 방지)

        4. 신규 이메일의 DB 중복 여부 확인
            * 새 이메일이 기존 내 이메일과 다를 때만 `userRepository.existsByEmail(newEmail)`을 실행합니다.
            * 이미 다른 유저가 사용 중이라면: 즉시 409 Conflict 예외를 던져 작업을 중단합니다.

        5. 이메일 값 교체
            * 중복 검사를 무사히 통과했다면, 엔티티 객체의 이메일을 새 값으로 교체합니다 (`user.setEmail(newEmail)`).

        6. 닉네임 처리 및 저장 완료
            * 닉네임 역시 `null` 체크를 거쳐 값이 있을 때만 교체합니다.
            * 최종적으로 DB에 반영(`save`)한 뒤 클라이언트에게 **200 OK**를 응답합니다.*/
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }

        int userId = (int) session.getAttribute("userId");

        // 1. DB 삭제
        userService.deleteUser(userId);
        // 2. 세션 비활성화
        session.invalidate();

        return ResponseEntity.noContent().build();
    }
}