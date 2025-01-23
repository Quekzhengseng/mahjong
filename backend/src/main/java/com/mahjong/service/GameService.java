package com.mahjong.service;

import com.mahjong.model.Tile;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

@Service
public class GameService {
    private final List<Tile> fullSet;
    private final List<Tile> currentHand;
    private final List<List<Tile>> submittedHand;
    private final List<Tile> specialHand;
    private boolean checkDiscard = true;
    
    private static final List<String> NORMAL_TILESETS = Arrays.asList("Bamboo", "Character", "Circle");
    private static final List<String> SPECIAL_TILESETS = Arrays.asList(
        "Red", "Green", "White", "Flower", "Season", "North", "South", "East", "West"
    );
    
    @Autowired
    private HandChecker handChecker;

    public GameService() {
        this.fullSet = new ArrayList<>();
        this.currentHand = new ArrayList<>();
        this.specialHand = new ArrayList<>();
        this.submittedHand = new ArrayList<>();
        this.checkDiscard = true;
        initializeAllTiles();
    }

    private void initializeAllTiles() {
        // Initialize normal numbered tiles
        for (String suit : NORMAL_TILESETS) {
            for (int number = 1; number <= 9; number++) {
                for (int copies = 0; copies < 4; copies++) {
                    fullSet.add(new Tile(number, suit));
                }
            }
        }

        // Initialize special tiles
        for (String suit : SPECIAL_TILESETS) {
            for (int number = 1; number <= 4; number++) {
                fullSet.add(new Tile(number, suit));
            }
        }

        // Initial draw
        for (int i = 0; i < 13; i++) {
            drawTile();
        }
    }

    public Tile drawTile() {
        Random rand = new Random();
        int randInt = rand.nextInt(fullSet.size());
        Tile tile = fullSet.remove(randInt);

        if (Arrays.asList("Flower", "Season").contains(tile.getSuit())) {
            specialHand.add(tile);
            return drawTile();
        } else {
            currentHand.add(tile);
            checkDiscard = false;
            return tile;
        }
    }

    public void discard(int tileIndex) {
        if (tileIndex >= 0 && tileIndex < currentHand.size()) {
            currentHand.remove(tileIndex);
            checkDiscard = true;
        } else {
            throw new IllegalArgumentException("Invalid tile index");
        }
    }

    public boolean returnDiscard() {
        return this.checkDiscard;
    }

    public boolean addCombiSet(List<Integer> tileIndices) {
        if (tileIndices.size() != 3) {
            return false;
        }

        List<Tile> combiSet = new ArrayList<>();

        for (int index : tileIndices) {
            if (index >= currentHand.size()) {
                return false;
            }
            combiSet.add(currentHand.get(index));
        }

        if (handChecker.checkChi(combiSet) || handChecker.checkPung(combiSet)) {
            submittedHand.add(combiSet);
            tileIndices.stream()
                .sorted(Collections.reverseOrder())
                .forEach(i -> currentHand.remove((int) i));
            return true;
        }

        return false;
    }

    public void removeCombiset(int Index) {
        //remove certain combisets and put them back into main hand
        List<Tile> combiset = submittedHand.remove(Index);
        for (Tile tile : combiset) {
            currentHand.add(tile);
        }
    }

    public void sortHand() {
        Map<String, Integer> suitPriority = new HashMap<>();
        suitPriority.put("Character", 1);
        suitPriority.put("Circle", 2);
        suitPriority.put("Bamboo", 3);
        suitPriority.put("Red", 4);
        suitPriority.put("Green", 5);
        suitPriority.put("White", 6);
        suitPriority.put("North", 7);
        suitPriority.put("South", 8);
        suitPriority.put("West", 9);
        suitPriority.put("East", 10);

        currentHand.sort((tile1, tile2) -> {
            int suitCompare = suitPriority.get(tile1.getSuit())
                .compareTo(suitPriority.get(tile2.getSuit()));
            return suitCompare != 0 ? suitCompare : 
                Integer.compare(tile1.getTileNumber(), tile2.getTileNumber());
        });
    }

    // Getters
    public List<Tile> getCurrentHand() {
        return new ArrayList<>(currentHand);
    }

    public List<List<Tile>> getSubmittedHand() {
        return new ArrayList<>(submittedHand);
    }

    public List<Tile> getSpecialHand() {
        return new ArrayList<>(specialHand);
    }

    public boolean isCheckDiscard() {
        return checkDiscard;
    }
}
