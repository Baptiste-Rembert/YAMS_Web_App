package com.yams.controller;

import com.yams.model.Game;
import com.yams.model.Turn;
import com.yams.service.TurnService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class TurnController {

    private final TurnService turnService;

    public TurnController(TurnService turnService) {
        this.turnService = turnService;
    }

    @PostMapping("/{id}/turns/roll")
    public Turn roll(@PathVariable Long id, HttpSession session) {
        return turnService.rollDice(id, session);
    }

    @PostMapping("/{id}/turns/reroll")
    public Turn reroll(@PathVariable Long id, @RequestBody RerollRequest req, HttpSession session) {
        return turnService.rerollDice(id, req.indices(), session);
    }

    @PostMapping("/{id}/turns/end")
    public Game end(@PathVariable Long id, HttpSession session) {
        return turnService.endTurn(id, session);
    }

    public static record RerollRequest(int[] indices) {}
}
