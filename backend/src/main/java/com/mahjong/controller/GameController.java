// src/main/java/com/mahjong/controller/GameController.java
package com.mahjong.controller;

import com.mahjong.model.Tile;
import com.mahjong.service.GameService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/game")
public class GameController {
    @Autowired
    private GameService gameService;
    
    //Checks whether the room is already taken
    @PostMapping("/create/{roomId}")
    public ResponseEntity<?> create(@PathVariable String roomId) {
        try {
            gameService.startGame(roomId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Failed to create room: " + e.getMessage());
        }
    }

    //Initialises the game with all tiles
    @PostMapping("/start/{roomId}")
    public void startGame(@PathVariable String roomId) {
        gameService.startGame(roomId);
    }

    //Resets the room
    @PostMapping("/reset/{roomId}")
    public void resetGame(@PathVariable String roomId) {
        gameService.resetGame(roomId);
    }

    //Draw tile for a specific player
    @PostMapping("/draw/{roomId}/{playerIndex}")
    public ResponseEntity<?> drawTiles(@PathVariable String roomId, @PathVariable int playerIndex) {
        try {
            gameService.drawTile(roomId, playerIndex);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error drawing tiles: " + e.getMessage());
        }
    }

    //Checks whether it is the players turn or not
    @GetMapping("/draw/{roomId}/{playerIndex}")
    public Boolean isPlayerTurn(@PathVariable String roomId, @PathVariable int playerIndex) {
        try {
            return gameService.isPlayerTurn(roomId, playerIndex);
        } catch (Exception e) {
            throw new RuntimeException("Error checking if it's player's turn: " + e.getMessage());
        }
    }
    
    //Discard the tile that is selected
    @PostMapping("/discard/{roomId}/{playerIndex}/{tileIndex}")
    public void discardTile(@PathVariable String roomId, @PathVariable int playerIndex, @PathVariable int tileIndex) {
        try {
            gameService.discard(roomId, playerIndex, tileIndex);
        } catch (Exception e) {
            throw new RuntimeException("Error discarding tile: " + e.getMessage());
        }
    }

    //Get whether the player has discard a tile or not
    @GetMapping("/checkdiscard/{room}/{playerIndex}")
    public boolean checkDiscard (@PathVariable String roomId, @PathVariable int playerIndex) {
        try {
        return gameService.checkDiscard(roomId, playerIndex);
        } catch (Exception e) {
            throw new RuntimeException("Error checking Discard" + e.getMessage());
        }
    }
    
    //Addition of a combiset
    @PostMapping("/combi-set/{roomid}/{playerIndex}")
    public boolean addCombiSet(@PathVariable String roomId, @PathVariable int playerIndex, @RequestBody List<Integer> tileIndices) {
        return gameService.addCombiSet(roomId, playerIndex, tileIndices);
    }

    //Removal of a combiset
    @PostMapping("/combi-set/remove/{index}")  
    public void removeCombiSet(@PathVariable int index) {
        gameService.removeCombiset(index);
    }

    //Get player current playing hand
    @GetMapping("/hand/{roomId}/{playerIndex}")
    public ResponseEntity<?> currentHand(@PathVariable String roomId, @PathVariable int playerIndex) {
        try {
            List<Tile> hand = gameService.getCurrentHand(roomId, playerIndex);
            return ResponseEntity.ok(hand);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error getting tiles: " + e.getMessage());
        }
    }

    //Get player current special hand
    @GetMapping("/special-hand/{roomId}/{playerIndex}")
    public ResponseEntity<?> getSpecialHand(@PathVariable String roomId, @PathVariable int playerIndex) {
        try {
            List<Tile> hand = gameService.getSpecialHand(roomId, playerIndex);
            return ResponseEntity.ok(hand);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error getting tiles: " + e.getMessage());
        }
    }

    //Sort players current hand
    @PostMapping("/sort/{roomId}/{playerIndex}")
    public ResponseEntity<?> sortHand(@PathVariable String roomId, @PathVariable int playerIndex) {
        try {
            gameService.sortHand(roomId, playerIndex);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error sorting hand: " + e.getMessage());
        }
    }

    //For render server
    @RequestMapping(value = "/health", method = RequestMethod.HEAD)
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok().build();
    }


}