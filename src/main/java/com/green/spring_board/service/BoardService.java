package com.green.spring_board.service;

import com.green.spring_board.dto.BoardResponse;
import com.green.spring_board.dto.BoardUpdateRequest;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UnauthenticatedExecption;
import com.green.spring_board.exceptions.UserRequestException;
import com.green.spring_board.dto.BoardCreateRequest;
import com.green.spring_board.repository.BoardRepository;
import com.green.spring_board.entity.Board;
import com.green.spring_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor

public class BoardService {
    private BoardRepository boardRepository;
    private UserRepository userRepository;

    // 전체 조회
    public List<BoardResponse> getAllBoards() {
        List<Board> boards = boardRepository.findAll();
        List<BoardResponse> boardResponses = new ArrayList<>();

        for (Board board : boards) {
            boardResponses.add(
                    new BoardResponse(
                            board.getId(),
                            board.getTitle(),
                            board.getContent(),
                            board.getHits(),
                            board.getUser().getId(),
                            board.getUser().getNickname(),
                            board.getCreatedDatetime(),
                            board.getUpdatedDatetime()
                    )
            );
        }
        return boardResponses;
    }

    // 상세 조회
    public BoardResponse getBoard(int id) {
        Optional<Board> optionalboard = boardRepository.findById(id);
        if(optionalboard.isEmpty()) {
            // 요청한 게시글을 찾지 못한 경우
            throw new ResourceNotFoundException("요청한 게시글을 찾지 못했습니다.");
        }

        Board board = optionalboard.get();
        board.setHits(board.getHits() + 1);
        boardRepository.save(board);

        return new BoardResponse(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                board.getHits(),
                board.getUser().getId(),
                board.getUser().getNickname(),
                board.getCreatedDatetime(),
                board.getUpdatedDatetime()
        );
    }

    public int createBoard(BoardCreateRequest boardCreateRequest, Integer userId) {
        if(boardCreateRequest.getTitle() == null || boardCreateRequest.getTitle().isBlank()) {
            throw new UserRequestException("잘못된 입력값 입니다.");
        }
        if(boardCreateRequest.getContent() == null || boardCreateRequest.getContent().isBlank()) {
            throw new UserRequestException("잘못된 입력값 입니다.");
        }

        // userId 유효성 체크 (해당 userId의 유저가 정상적으로 존재하는지)
        // TODO :: 이후 삭제/탈퇴 유저에 대한 검증도 추가 필요
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()) {
            throw new UnauthenticatedExecption("로그인한 사용자를 찾을 수 없습니다.");
        }

        Board board = new Board();
        board.setTitle(boardCreateRequest.getTitle());
        board.setContent(boardCreateRequest.getContent());
        board.setUser(user.get());

        Board saveBoard = boardRepository.save(board);
        return saveBoard.getId();
    }

    public void updateBoard(int id, BoardUpdateRequest boardUpdateRequest) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }

        Board board = optionalBoard.get();

        if (boardUpdateRequest.getTitle() == null || boardUpdateRequest.getTitle().isBlank()) {

        }
        if (boardUpdateRequest.getContent() == null || boardUpdateRequest.getContent().isBlank()) {

        }
        board.setTitle(boardUpdateRequest.getTitle());
        board.setContent(boardUpdateRequest.getContent());

        boardRepository.save(board);
    }

    public void deleteBoard(int id) {
        boolean isExist = boardRepository.existsById(id);
        if (!isExist) {
            throw new ResourceNotFoundException("해당 게시글을 찾을 수 없습니다. id");
        }
        boardRepository.deleteById(id);
    }
}