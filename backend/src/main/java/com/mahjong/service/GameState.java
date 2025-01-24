package com.mahjong.model;

import java.util.List;

public class GameState {
    private List<Tile> specialHand;
    private List<Tile> currentHand;
    private List<List<Tile>> submittedHand;

    public GameState(List<Tile> currentHand, List<List<Tile>> submittedHand, List<Tile> specialHand) {
        this.type = type;
        this.currentHand = currentHand;
        this.submittedHand = submittedHand;
    }

    public List<Tile> getCurrentHand() {
        return currentHand;
    }

    public void setCurrentHand(List<Tile> currentHand) {
        this.currentHand = currentHand;
    }

    public List<List<Tile>> getSubmittedHand() {
        return submittedHand;
    }

    public void setSubmittedHand(List<List<Tile>> submittedHand) {
        this.submittedHand = submittedHand;
    }

    public List<Tile> getSpecialHand() {
        return specialHand;
    }

    public void setSpecialHand(List<Tile> specialHand) {
        this.specialHand = specialHand;
    }
}