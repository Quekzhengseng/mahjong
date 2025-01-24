package com.mahjong.model;

import java.util.List;

public class GameState {
    private String type;
    private List<Tile> currentHand;
    private List<List<Tile>> submittedHand;

    public GameState(String type, List<Tile> currentHand, List<List<Tile>> submittedHand) {
        this.type = type;
        this.currentHand = currentHand;
        this.submittedHand = submittedHand;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}