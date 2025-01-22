package com.mahjong.model;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class Tile {
    private int tileNumber;
    private String suit;
    
    // Add getters if not using Lombok
    public int getTileNumber() {
        return this.tileNumber;
    }
    
    public String getSuit() {
        return this.suit;
    }
}
