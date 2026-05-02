package com.yams.controller;

import com.yams.model.Game;
import com.yams.model.Turn;
import com.yams.service.TurnService;
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
    public Turn roll(@PathVariable Long id) {
        return turnService.rollDice(id);
    }

    @PostMapping("/{id}/turns/reroll")
    public Turn reroll(@PathVariable Long id, @RequestBody RerollRequest req) {
        return turnService.rerollDice(id, req.indices());
    }

    @PostMapping("/{id}/turns/end")
    public Game end(@PathVariable Long id) {
        return turnService.endTurn(id);
    }

    public static record RerollRequest(int[] indices) {}
}
