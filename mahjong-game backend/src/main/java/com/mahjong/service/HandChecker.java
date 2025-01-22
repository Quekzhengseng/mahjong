package com.mahjong.service;

import com.mahjong.model.Tile;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class HandChecker {
    public boolean checkChi(List<Tile> combiSet) {
        if (combiSet.size() != 3) {
            return false;
        }
        
        String firstSuit = combiSet.get(0).getSuit();
        if (!combiSet.stream().allMatch(tile -> tile.getSuit().equals(firstSuit))) {
            return false;
        }
        
        List<Integer> numbers = combiSet.stream()
            .map(Tile::getTileNumber)
            .sorted()
            .toList();
        
        return (numbers.get(1) == numbers.get(0) + 1) && 
               (numbers.get(2) == numbers.get(1) + 1);
    }

    public boolean checkPung(List<Tile> combiSet) {
        if (combiSet.size() != 3) {
            return false;
        }
        
        Tile firstTile = combiSet.get(0);
        return combiSet.stream().allMatch(tile -> 
            tile.getTileNumber() == firstTile.getTileNumber() && 
            tile.getSuit().equals(firstTile.getSuit()));
    }

    public boolean checkEye(List<Tile> combiSet) {
        if (combiSet.size() != 2) {
            return false;
        }

        Tile firstTile = combiSet.get(0);
        return combiSet.stream().allMatch(tile -> 
            tile.getTileNumber() == firstTile.getTileNumber() && 
            tile.getSuit().equals(firstTile.getSuit()));
    }

    public boolean checkWin(List<List<Tile>> submittedHand, List<Tile> currentHand) {
        return submittedHand.size() == 5 && currentHand.isEmpty();
    }
}
