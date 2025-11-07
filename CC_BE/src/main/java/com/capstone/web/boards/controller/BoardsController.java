package com.capstone.web.boards.controller;

import com.capstone.web.boards.dto.BoardSummaryResponse;
import com.capstone.web.boards.service.BoardsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardsController {

    private final BoardsService boardsService;

    @GetMapping
    public ResponseEntity<List<BoardSummaryResponse>> getBoards() {
        return ResponseEntity.ok(boardsService.getAllBoards());
    }
}

