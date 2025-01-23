"use client";
import { useState, useEffect } from "react";
import axios from "axios";

const CurrentHand = () => {
  const [hand, setHand] = useState([]);
  const [combiSets, setCombiSets] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedTiles, setSelectedTiles] = useState([]);
  const [checkdiscard, setDiscard] = useState();

  const start = async () => {
    try {
      axios.post("https://mahjong-5ztb.onrender.com/api/game/start");
      await getHand();
      await getCombiSets();
    } catch (error) {
      console.error("Error starting", error);
    }
  };

  const reset = async () => {
    try {
      axios.post("https://mahjong-5ztb.onrender.com/api/game/reset");
      await getHand();
      await getCombiSets();
    } catch (error) {
      console.error("Error reseting", error);
    }
  };

  const getHand = async () => {
    try {
      const response = await axios.get(
        "https://mahjong-5ztb.onrender.com/api/game/hand"
      );
      setHand(response.data);
      setIsLoading(false);
    } catch (error) {
      console.error("Error getting hand:", error);
      setIsLoading(false);
    }
  };

  const getCombiSets = async () => {
    try {
      const response = await axios.get(
        "https://mahjong-5ztb.onrender.com/api/game/submitted-hand"
      );
      setCombiSets(response.data);
    } catch (error) {
      console.error("Error getting combi sets:", error);
    }
  };

  const sortHand = async () => {
    try {
      const response = await axios.post(
        "https://mahjong-5ztb.onrender.com/api/game/sort"
      );
      setHand(response.data);
      console.log(checkdiscard);
    } catch (error) {
      console.error("Error sorting hand:", error);
    }
  };

  const drawTile = async () => {
    try {
      await axios.post("https://mahjong-5ztb.onrender.com/api/game/draw");

      const response = await checkDiscard();
      setDiscard(response.data);
      console.log(response.data);

      if (response.data) {
        alert("You must discard a tile before drawing!");
        return;
      }

      await getHand();
    } catch (error) {
      console.error("Error drawing tile:", error);
    }
  };

  const discardTile = async (index) => {
    try {
      await axios.post(
        `https://mahjong-5ztb.onrender.com/api/game/discard/${index}`
      );

      const response = await checkDiscard();
      setDiscard(response.data);
      console.log(!response.data);

      if (!response.data) {
        alert("You must draw a tile before discarding!");
        return;
      }

      await getHand();
      setSelectedTiles([]);
    } catch (error) {
      console.error("Error discarding tile:", error);
    }
  };

  const checkDiscard = async () => {
    try {
      return axios.get(
        "https://mahjong-5ztb.onrender.com/api/game/checkdiscard"
      );
    } catch (error) {
      console.error("Error retrieving discard boolean", error);
    }
  };

  const submitCombiSet = async () => {
    if (selectedTiles.length !== 3) {
      alert("Please select exactly 3 tiles for a combination set");
      return;
    }

    try {
      await axios.post(
        "https://mahjong-5ztb.onrender.com/api/game/combi-set",
        selectedTiles
      );
      await getHand();
      await getCombiSets();
      setSelectedTiles([]);
    } catch (error) {
      console.error("Error submitting combi set:", error);
      alert("Invalid combination set");
    }
  };

  const removeCombiSet = async (index) => {
    try {
      await axios.post(
        `https://mahjong-5ztb.onrender.com/api/game/combi-set/remove/${index}`
      );
      await getHand();
      await getCombiSets();
    } catch (error) {
      console.error("Error removing combi set:", error);
    }
  };

  const toggleTileSelection = (index) => {
    if (selectedTiles.includes(index)) {
      setSelectedTiles(selectedTiles.filter((i) => i !== index));
    } else {
      if (selectedTiles.length < 3) {
        setSelectedTiles([...selectedTiles, index]);
      }
    }
  };

  useEffect(() => {
    getHand();
    getCombiSets();
    const checkInitialState = async () => {
      const response = await checkDiscard();
      console.log("Initial checkDiscard state:", response.data);
      setDiscard(response.data);
    };
    checkInitialState();
  }, []);

  if (isLoading) return <div>Loading...</div>;

  return (
    <div className="p-4">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold">Current Hand</h2>
        <div className="space-x-2">
          <button
            onClick={start}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            Start Game
          </button>

          <button
            onClick={reset}
            className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
          >
            Reset Game
          </button>
          <button
            onClick={sortHand}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            Sort Hand
          </button>
          <button
            onClick={drawTile}
            disabled={!checkdiscard}
            className={`px-4 py-2 ${
              checkdiscard
                ? "bg-green-500 hover:bg-green-600 text-white" // Green when CAN draw (checkdiscard is true)
                : "bg-gray-300 text-gray-500 cursor-not-allowed"
            }`}
          >
            Draw Tile
          </button>

          <button
            onClick={() =>
              selectedTiles.length === 1 && discardTile(selectedTiles[0])
            }
            disabled={checkdiscard || selectedTiles.length !== 1} // Disabled when checkdiscard is true OR no tile selected
            className={`px-4 py-2 rounded ${
              !checkdiscard && selectedTiles.length === 1
                ? "bg-red-500 hover:bg-red-600 text-white" // Red when CAN discard (checkdiscard is false)
                : "bg-gray-300 text-gray-500 cursor-not-allowed"
            }`}
          >
            Discard Selected
          </button>
          <button
            onClick={submitCombiSet}
            className={`px-4 py-2 rounded ${
              selectedTiles.length === 3
                ? "bg-purple-500 hover:bg-purple-600 text-white"
                : "bg-gray-300 text-gray-500 cursor-not-allowed"
            }`}
            disabled={selectedTiles.length !== 3}
          >
            Submit Combi Set
          </button>
        </div>
      </div>

      <div className="mb-8">
        <h3 className="text-lg font-semibold mb-2">Your Hand</h3>
        <div className="flex flex-wrap gap-2">
          {hand &&
            hand.map((tile, index) => (
              <div
                key={index}
                className={`p-4 border rounded bg-white shadow cursor-pointer transition-all 
                                ${
                                  selectedTiles.includes(index)
                                    ? "border-blue-500 scale-110"
                                    : "hover:border-gray-400"
                                }`}
                onClick={() => toggleTileSelection(index)}
              >
                {tile.suit} {tile.tileNumber}
              </div>
            ))}
        </div>
      </div>

      <div className="mt-8">
        <h3 className="text-lg font-semibold mb-2">Submitted Combinations</h3>
        <div className="space-y-4">
          {combiSets.map((set, setIndex) => (
            <div
              key={setIndex}
              className="flex items-center gap-4 p-4 bg-gray-50 rounded"
            >
              <div className="flex gap-2">
                {set.map((tile, tileIndex) => (
                  <div
                    key={tileIndex}
                    className="p-4 border rounded bg-white shadow"
                  >
                    {tile.suit} {tile.tileNumber}
                  </div>
                ))}
              </div>
              <button
                onClick={() => removeCombiSet(setIndex)}
                className="px-3 py-1 text-sm bg-red-500 text-white rounded hover:bg-red-600"
              >
                Remove
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default CurrentHand;
