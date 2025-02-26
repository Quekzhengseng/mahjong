package com.mahjong.service;

import com.mahjong.model.Tile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.WriteResult;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.WriteResult;

@Service
public class GameService {
    private final List<Tile> fullSet;
    private final List<Tile> discardPile;
    private List<Tile> currentHand;
    private List<List<Tile>> submittedHand;
    private boolean checkDiscard;
    private List<Tile> specialHand;

    private static final List<String> NORMAL_TILESETS = Arrays.asList("Bamboo", "Character", "Circle");
    private static final List<String> SPECIAL_TILESETS = Arrays.asList(
        "Red", "Green", "White", "Flower", "Season", "North", "South", "East", "West"
    );
    
    private final Firestore db;

    @Autowired
    private HandChecker handChecker;

    
    public GameService(List<Tile> currentHand, List<List<Tile>> submittedHand, List<Tile> specialHand) {
        this.fullSet = new ArrayList<>();
        this.discardPile = new ArrayList<>();
        this.db = FirestoreClient.getFirestore();
        this.specialHand = specialHand;
        this.currentHand = currentHand;
        this.submittedHand = submittedHand;
    }

    // TO-DO:
    // Need to create check for win, create get combiset
    // Fix remove and add combiset
    // Addition of logic between drawing and changing of states



    public void startGame(String roomId) {
        DocumentReference roomRef = db.collection("rooms").document(roomId);
        
        try {
            ApiFuture<DocumentSnapshot> future = roomRef.get();
            DocumentSnapshot document = future.get();
            
            // Check if game has already started
            Boolean isGameStarted = document.getBoolean("gameStarted");
            if (Boolean.TRUE.equals(isGameStarted)) {
                throw new IllegalStateException("Game already started for room: " + roomId);
            }

            // Initialize and distribute tiles
            initializeAllTiles(roomId);

            // Prepare game start update
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("gameStarted", true);
            updateData.put("playerIndex", 0);

            // Update room document
            roomRef.update(updateData).get();

        } catch (Exception e) {
            throw new RuntimeException("Error starting game for room: " + roomId, e);
        }
    }

    public void resetGame(String roomId) {
        DocumentReference roomRef = db.collection("rooms").document(roomId);
        
        try {
            // Prepare reset data
            Map<String, Object> resetData = new HashMap<>();
            resetData.put("fullSet", new ArrayList<>());
            resetData.put("discardPile", new ArrayList<>());
            resetData.put("gameStarted", false);
            resetData.put("playerIndex", 0);
            resetData.put("playerHands", new HashMap<>());
            resetData.put("playerSpecialHands", new HashMap<>());

            // Update room document
            roomRef.update(resetData).get();

        } catch (Exception e) {
            throw new RuntimeException("Error resetting game for room: " + roomId, e);
        }
    }

    private void initializeAllTiles(String roomId) throws Exception {
        // Clear the fullSet before initializing
        this.fullSet.clear();

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

        // Shuffle the full set
        Collections.shuffle(fullSet);


        // Save tiles to Firestore
        try {
            // Create a document for the room's tiles
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("fullSet", fullSet);
            
            // Save to Firestore with roomId as the document ID
            db.collection("rooms").document(roomId).update(updateData)
                .get(); // Wait for the operation to complete

        } catch (Exception e) {
            // Log the error
            System.err.println("Error saving tiles to Firestore: " + e.getMessage());
            throw new RuntimeException("Failed to save tiles to Firestore", e);
        }

        for (int i = 0; i < 4; i++) {
            StartTile("test", i);
        }
    }

    public void StartTile(String roomId, int currentPlayerIndex) throws Exception {
        DocumentReference roomRef = db.collection("rooms").document(roomId);
        ApiFuture<DocumentSnapshot> future = roomRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            // Retrieve fullSet and cast correctly
            List<Map<String, Object>> fullSetData = (List<Map<String, Object>>) document.get("fullSet");
            
            // Convert Firestore map data back to Tile objects
            List<Tile> fullSet = fullSetData.stream()
                .map(tileData -> new Tile(
                    ((Long) tileData.get("tileNumber")).intValue(), 
                    (String) tileData.get("suit")
                ))
                .collect(Collectors.toList());

            // Retrieve existing player hands or create if not exists
            Map<String, Object> playerHands = (Map<String, Object>) document.get("playerHands");
            if (playerHands == null) {
                playerHands = new HashMap<>();
            }

            // Retrieve existing player special hands or create if not exists
            Map<String, Object> playerSpecialHands = (Map<String, Object>) document.get("playerSpecialHands");
            if (playerSpecialHands == null) {
                playerSpecialHands = new HashMap<>();
            }

            // Get or create current player's hand
            List<Map<String, Object>> currentPlayerHand = new ArrayList<>();
            List<Map<String, Object>> currentSpecialHand = new ArrayList<>();

            // Draw a random tile
            for (int i = 0; i < 14; i++) {
                Random rand = new Random();
                int randInt = rand.nextInt(fullSet.size());
                Tile drawnTile = fullSet.remove(randInt);

                if (Arrays.asList("Flower", "Season").contains(drawnTile.getSuit())) {
                    Map<String, Object> tileMap = new HashMap<>();
                    tileMap.put("tileNumber", drawnTile.getTileNumber());
                    tileMap.put("suit", drawnTile.getSuit());
                    currentSpecialHand.add(tileMap);
                    i--;
                } else {
                    // Add the drawn tile to the player's hand
                    Map<String, Object> tileMap = new HashMap<>();
                    tileMap.put("tileNumber", drawnTile.getTileNumber());
                    tileMap.put("suit", drawnTile.getSuit());
                    currentPlayerHand.add(tileMap);
                }
            }

            // Update player hands
            playerHands.put(String.valueOf(currentPlayerIndex), currentPlayerHand);

            // Update player special hands
            playerSpecialHands.put(String.valueOf(currentPlayerIndex), currentSpecialHand);

            // Prepare update data
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("fullSet", fullSet);
            updateData.put("playerHands", playerHands);
            updateData.put("playerSpecialHands", playerSpecialHands);
            
            // Save to Firestore with roomId as the document ID
            db.collection("rooms").document(roomId).update(updateData)
                .get(); // Wait for the operation to complete
        }
    }

    public void drawTile(String roomId, int currentPlayerIndex) throws Exception {
        DocumentReference roomRef = db.collection("rooms").document(roomId);
        ApiFuture<DocumentSnapshot> future = roomRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            List<Map<String, Object>> fullSetData = (List<Map<String, Object>>) document.get("fullSet");
            
            // Convert Firestore map data back to Tile objects
            List<Tile> fullSet = fullSetData.stream()
                .map(tileData -> new Tile(
                    ((Long) tileData.get("tileNumber")).intValue(), 
                    (String) tileData.get("suit")
                ))
                .collect(Collectors.toList());

            // Retrieve player hands
            Map<String, Object> playerHands = (Map<String, Object>) document.get("playerHands");
            if (playerHands == null) {
                playerHands = new HashMap<>();
            }

            // Retrieve player special hands
            Map<String, Object> playerSpecialHands = (Map<String, Object>) document.get("playerSpecialHands");
            if (playerSpecialHands == null) {
                playerSpecialHands = new HashMap<>();
            }

            // Get or create current player's hand and special hand
            List<Map<String, Object>> currentPlayerHand = 
                (List<Map<String, Object>>) playerHands.getOrDefault(
                    String.valueOf(currentPlayerIndex), 
                    new ArrayList<>()
                );

            List<Map<String, Object>> currentPlayerSpecialHand = 
                (List<Map<String, Object>>) playerSpecialHands.getOrDefault(
                    String.valueOf(currentPlayerIndex), 
                    new ArrayList<>()
                );

            // Draw tiles until a non-special tile is drawn
            Tile drawnTile = null;
            while (drawnTile == null && !fullSet.isEmpty()) {
                Random rand = new Random();
                int randInt = rand.nextInt(fullSet.size());
                Tile potentialTile = fullSet.remove(randInt);

                // Check if it's a special tile
                if (Arrays.asList("Flower", "Season").contains(potentialTile.getSuit())) {
                    // Add to special hand
                    Map<String, Object> specialTileMap = new HashMap<>();
                    specialTileMap.put("tileNumber", potentialTile.getTileNumber());
                    specialTileMap.put("suit", potentialTile.getSuit());
                    currentPlayerSpecialHand.add(specialTileMap);
                } else {
                    // Regular tile found
                    drawnTile = potentialTile;
                }
            }

            // If a non-special tile was drawn
            if (drawnTile != null) {
                // Prepare tile map for Firestore
                Map<String, Object> tileMap = new HashMap<>();
                tileMap.put("tileNumber", drawnTile.getTileNumber());
                tileMap.put("suit", drawnTile.getSuit());
                currentPlayerHand.add(tileMap);

                // Update player hands
                playerHands.put(String.valueOf(currentPlayerIndex), currentPlayerHand);
                playerSpecialHands.put(String.valueOf(currentPlayerIndex), currentPlayerSpecialHand);

                // Prepare update data
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("fullSet", fullSet.stream()
                    .map(tile -> {
                        Map<String, Object> tilemap = new HashMap<>();
                        tilemap.put("tileNumber", tile.getTileNumber());
                        tilemap.put("suit", tile.getSuit());
                        return tilemap;
                    })
                    .collect(Collectors.toList()));
                updateData.put("playerHands", playerHands);
                updateData.put("playerSpecialHands", playerSpecialHands);
                
                // Save to Firestore with roomId as the document ID
                db.collection("rooms").document(roomId).update(updateData)
                    .get(); // Wait for the operation to complete
            }
        }
    }

    
    public List<Tile> getCurrentHand(String roomId, int playerIndex) throws Exception {
        DocumentReference roomRef = db.collection("rooms").document(roomId);
        ApiFuture<DocumentSnapshot> future = roomRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            // Retrieve player hands
            Map<String, Object> playerHands = (Map<String, Object>) document.get("playerHands");
            
            if (playerHands != null) {
                // Get the specific player's hand
                List<Map<String, Object>> playerHandData = 
                    (List<Map<String, Object>>) playerHands.get(String.valueOf(playerIndex));
                
                if (playerHandData != null) {
                    // Convert Firestore map data back to Tile objects
                    return playerHandData.stream()
                        .map(tileData -> new Tile(
                            ((Long) tileData.get("tileNumber")).intValue(), 
                            (String) tileData.get("suit")
                        ))
                        .collect(Collectors.toList());
                }
            }
        }
        
        return new ArrayList<>(); // Return empty list if no hand found
    }

    public List<Tile> getSpecialHand(String roomId, int playerIndex) throws Exception {
        DocumentReference roomRef = db.collection("rooms").document(roomId);
        ApiFuture<DocumentSnapshot> future = roomRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            // Retrieve player hands
            Map<String, Object> playerHands = (Map<String, Object>) document.get("playerSpecialHands");
            
            if (playerHands != null) {
                // Get the specific player's hand
                List<Map<String, Object>> playerHandData = 
                    (List<Map<String, Object>>) playerHands.get(String.valueOf(playerIndex));
                
                if (playerHandData != null) {
                    // Convert Firestore map data back to Tile objects
                    return playerHandData.stream()
                        .map(tileData -> new Tile(
                            ((Long) tileData.get("tileNumber")).intValue(), 
                            (String) tileData.get("suit")
                        ))
                        .collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>(); // Return empty list if no hand found
    }

    public void sortHand(String roomId, int playerIndex) throws Exception {
        DocumentReference roomRef = db.collection("rooms").document(roomId);
        ApiFuture<DocumentSnapshot> future = roomRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            // Retrieve player hands
            Map<String, Object> playerHands = (Map<String, Object>) document.get("playerHands");
            
            if (playerHands != null) {
                // Get the specific player's hand
                List<Map<String, Object>> playerHandData = 
                    (List<Map<String, Object>>) playerHands.get(String.valueOf(playerIndex));
                
                if (playerHandData != null) {
                    // Convert Firestore map data back to Tile objects
                    List<Tile> playerHand = playerHandData.stream()
                        .map(tileData -> new Tile(
                            ((Long) tileData.get("tileNumber")).intValue(), 
                            (String) tileData.get("suit")
                        ))
                        .collect(Collectors.toList());

                    // Sort the hand
                    sortTiles(playerHand);

                    // Update the player's hand in Firestore
                    Map<String, Object> updateData = new HashMap<>();
                    List<Map<String, Object>> sortedHandMaps = playerHand.stream()
                        .map(tile -> {
                            Map<String, Object> tileMap = new HashMap<>();
                            tileMap.put("tileNumber", tile.getTileNumber());
                            tileMap.put("suit", tile.getSuit());
                            return tileMap;
                        })
                        .collect(Collectors.toList());

                    playerHands.put(String.valueOf(playerIndex), sortedHandMaps);
                    
                    // Prepare update data
                    updateData.put("playerHands", playerHands);
                    
                    // Save to Firestore
                    roomRef.update(updateData).get();
                }
            }
        }
    }

    private void sortTiles(List<Tile> hand) {
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

        hand.sort((tile1, tile2) -> {
            int suitCompare = suitPriority.get(tile1.getSuit())
                .compareTo(suitPriority.get(tile2.getSuit()));
            return suitCompare != 0 ? suitCompare : 
                Integer.compare(tile1.getTileNumber(), tile2.getTileNumber());
        });
    }


    public boolean isPlayerTurn(String roomId, int playerIndex) throws Exception {
        DocumentReference roomRef = db.collection("rooms").document(roomId);
        ApiFuture<DocumentSnapshot> future = roomRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            // Retrieve player hands
            Integer currentIndex = ((Long) document.get("playerIndex")).intValue();
            
            if (currentIndex == playerIndex) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public boolean addCombiSet(String roomId, int playerIndex, List<Integer> tileIndices) {
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

    public void getCombiset() {

    }

    public void discard(String roomId, int playerIndex, int tileIndex) throws Exception {
        DocumentReference roomRef = db.collection("rooms").document(roomId);
        ApiFuture<DocumentSnapshot> future = roomRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            // Retrieve player hands
            Map<String, Object> playerHands = (Map<String, Object>) document.get("playerHands");
            
            if (playerHands != null) {
                // Get the specific player's hand
                List<Map<String, Object>> playerHandData = 
                    (List<Map<String, Object>>) playerHands.get(String.valueOf(playerIndex));
                
                if (playerHandData != null && tileIndex >= 0 && tileIndex < playerHandData.size()) {
                    // Remove the tile at the specified index
                    Map<String, Object> discardedTileData = playerHandData.remove(tileIndex);
                    
                    // Convert discarded tile back to Tile object
                    Tile discardedTile = new Tile(
                        ((Long) discardedTileData.get("tileNumber")).intValue(), 
                        (String) discardedTileData.get("suit")
                    );

                    // Update player's hand in Firestore
                    Map<String, Object> updateData = new HashMap<>();
                    playerHands.put(String.valueOf(playerIndex), playerHandData);
                    updateData.put("playerHands", playerHands);

                    // Add tile to discard pile in Firestore
                    List<Map<String, Object>> discardPile = 
                        (List<Map<String, Object>>) document.get("discardPile");
                    
                    if (discardPile == null) {
                        discardPile = new ArrayList<>();
                    }

                    Map<String, Object> discardTileMap = new HashMap<>();
                    discardTileMap.put("tileNumber", discardedTile.getTileNumber());
                    discardTileMap.put("suit", discardedTile.getSuit());
                    discardPile.add(discardTileMap);

                    updateData.put("discardPile", discardPile);

                    // Update the room document
                    roomRef.update(updateData).get();

                    // Set discard flag
                    this.checkDiscard = true;
                } else {
                    throw new IllegalArgumentException("Invalid tile index");
                }
            }
        }
    }

    public boolean checkDiscard(String roomId, int playerIndex) throws Exception {
        DocumentReference roomRef = db.collection("rooms").document(roomId);
        ApiFuture<DocumentSnapshot> future = roomRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Map<String, Boolean> playerDiscard = (Map<String, Boolean>) document.get("playerDiscard");
            boolean discard = playerDiscard.get(String.valueOf(playerIndex));
            return discard;
        }
        return false;
    };
}
