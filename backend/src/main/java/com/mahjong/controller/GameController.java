// src/main/java/com/mahjong/controller/GameController.java
package com.mahjong.controller;

import com.mahjong.model.Tile;
import com.mahjong.service.GameService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;



@RestController
@RequestMapping("/api/game")
public class GameController {
    @Autowired
    private GameService gameService;

    @PostMapping("/start")
    public void startGame() {
        gameService.startGame();
    }

    @PostMapping("/reset")
    public void resetGame() {
        gameService.resetGame();
    }

    @PostMapping("/draw")
    public Tile drawTile() {
        return gameService.drawTile();
    }

    @PostMapping("/discard/{tileIndex}")
    public void discardTile(@PathVariable int tileIndex) {
        gameService.discard(tileIndex);
    }

    @GetMapping("/checkdiscard")
    public boolean returnDiscard () {
        return gameService.returnDiscard();
    }
    
    @PostMapping("/combi-set")
    public boolean addCombiSet(@RequestBody List<Integer> tileIndices) {
        return gameService.addCombiSet(tileIndices);
    }

    @PostMapping("/combi-set/remove/{index}")  
    public List<List<Tile>> removeCombiSet(@PathVariable int index) {
        gameService.removeCombiset(index);
        return getSubmittedHand();
    }

    @GetMapping("/hand")
    public List<Tile> getCurrentHand() {
        return gameService.getCurrentHand();
    }

    @GetMapping("/special-hand")
    public List<Tile> getSpecialHand() {
        return gameService.getSpecialHand();
    }

    @GetMapping("/submitted-hand")
    public List<List<Tile>> getSubmittedHand() {
        return gameService.getSubmittedHand();
    }

    @PostMapping("/sort")
    public List<Tile> sortHand() {
        gameService.sortHand();
        return gameService.getCurrentHand();
    }

    @RequestMapping(value = "/health", method = RequestMethod.HEAD)
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok().build();
    }
}