package com.example.logicservice.controller;

import com.example.logicservice.dto.*;
import com.example.logicservice.service.MoveStrategy;
import com.example.logicservice.service.TrumpStrategy;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logic")
public class LogicController {

    private final TrumpStrategy trumpStrategy;
    private final MoveStrategy moveStrategy;

    public LogicController(TrumpStrategy trumpStrategy, MoveStrategy moveStrategy) {
        this.trumpStrategy = trumpStrategy;
        this.moveStrategy = moveStrategy;
    }

    @PostMapping("/trump")
    public ChooseTrumpResponse chooseTrump(@Valid @RequestBody ChooseTrumpRequest request) {
        return trumpStrategy.chooseTrump(request);
    }

    @PostMapping("/move")
    public ChooseMoveResponse chooseMove(@Valid @RequestBody ChooseMoveRequest request) {
        return moveStrategy.chooseMove(request);
    }
}
