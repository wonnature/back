package com.wonkwang.wonnature.service;

import com.wonkwang.wonnature.domain.Board;
import com.wonkwang.wonnature.dto.BoardDTO;
import com.wonkwang.wonnature.repository.BoardRepository;
import com.wonkwang.wonnature.repository.BoardRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

//    @PostConstruct
//    public void init() {
//
//        List<Board> all = boardRepository.findAll();
//        if (all.isEmpty()) {
//            boardRepository.save(new Board(1L,"연혁 내용을 입력해주세요."));
//        }
//    }
    public BoardDTO getBoard(String pathname) {

        Board findBoard = boardRepository.findByPathName(pathname).orElse(null);
        if (findBoard == null) {
            return null;
        }
        return new BoardDTO(findBoard);
    }

    @Transactional
    public void updateBoard(BoardDTO boardDTO) {
        // 경로 이름으로 게시판을 찾는다.
        Optional<Board> optionalBoard = boardRepository.findByPathName(boardDTO.getPathName());
        System.out.println("boardDTO = " + boardDTO.getPathName());

        // 게시판이 존재하지 않으면 새로 생성한다.
        if (optionalBoard.isEmpty()) {
            Board board = new Board(boardDTO);
            boardRepository.save(board);
            return;
        }

        // 게시판이 존재하면 내용을 업데이트한다.
        Board findBoard = optionalBoard.get();
        findBoard.updateBoard(boardDTO);
    }
}
