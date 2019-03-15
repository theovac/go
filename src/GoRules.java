import java.util.*;

/**
 * Created by theovac on 13/6/2017.
 */
public class GoRules {

    public GoRules() {
    }

    public static final class GetConnectedResult {
        private final int libertyCount;
        private final List<BoardPosition> connectedStones;
        private final List<BoardPosition> libertyPositions;

        public GetConnectedResult(int libertyCount, List<BoardPosition> libertyPositions, List<BoardPosition> connectedStones) {
            this.libertyCount = libertyCount;
            this.connectedStones = connectedStones;
            this.libertyPositions = libertyPositions;
        }

        public int getLibertyCount() {
            return this.libertyCount;
        }

        public List<BoardPosition> getConnectedStones() {
            return this.connectedStones;
        }

        public List<BoardPosition> getLibertyPositions() { return this.libertyPositions; }
    }

    /* Finds connected stones of the same color. */
    public static GetConnectedResult getConnected(BoardPosition pos, int[][] gameState) {
        List<BoardPosition> connectedPositions = new ArrayList<BoardPosition>() {
            {
                add(new BoardPosition(pos.getRow() + 1, pos.getCol()));
                add(new BoardPosition(pos.getRow(), pos.getCol() + 1));
                add(new BoardPosition(pos.getRow() - 1, pos.getCol()));
                add(new BoardPosition(pos.getRow(), pos.getCol() - 1));
            }
        };
        List<BoardPosition> connectedStones = new ArrayList<BoardPosition>();
        List<BoardPosition> libertyPositions = new ArrayList<BoardPosition>();
        int libertyCount = 0;

        for (BoardPosition connPos : connectedPositions) {
            if (connPos.getRow() < gameState.length &&
                    connPos.getCol() < gameState.length &&
                    connPos.getRow() >= 0 && connPos.getCol() >= 0) {
                if (gameState[pos.getRow()][pos.getCol()] != 0 &&
                        gameState[connPos.getRow()][connPos.getCol()] == gameState[pos.getRow()][pos.getCol()]) {
                    connectedStones.add(connPos);
                } else if (gameState[connPos.getRow()][connPos.getCol()] == 0) {
                    libertyCount += 1;
                    libertyPositions.add(new BoardPosition(connPos.getRow(), connPos.getCol()));
                }
            }
        }
        return new GetConnectedResult(libertyCount, libertyPositions, connectedStones);
    }

    public static List<BoardPosition> getAdjacent(BoardPosition pos, int[][] gameState) {
        List<BoardPosition> adjacentStones = new ArrayList<>();
        List<BoardPosition> adjacentPositions = new ArrayList<BoardPosition>() {
            {
                add(new BoardPosition(pos.getRow() + 1, pos.getCol()));
                add(new BoardPosition(pos.getRow(), pos.getCol() + 1));
                add(new BoardPosition(pos.getRow() - 1, pos.getCol()));
                add(new BoardPosition(pos.getRow(), pos.getCol() - 1));
            }
        };

        for (BoardPosition aPos : adjacentPositions) {
            adjacentStones.add(new BoardPosition(aPos.getRow(), aPos.getCol()));
        }

        return adjacentPositions;
    }

    public static final class CheckCaptureResult {
        private int libertyCount;
        private List<BoardPosition> stoneGroup;
        private BoardPosition atariPos;

        public CheckCaptureResult(int libertyCount, List<BoardPosition> stoneGroup, BoardPosition atariPos) {
            this.libertyCount = libertyCount;
            this.stoneGroup = stoneGroup;
            this.atariPos = atariPos;
        }

        public int getLibertyCount() {
            return this.libertyCount;
        }

        public List<BoardPosition> getStoneGroup() {
            return this.stoneGroup;
        }

        public BoardPosition getAtariPos() { return this.atariPos; }
    }

    public static final class BoardPosition {
        private int row, col;

        public BoardPosition(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return this.row;
        }

        public void setRow(int row) { this.row = row;};

        public int getCol() {
            return this.col;
        }

        public void setCol(int col) { this.col= col;};
    }

    public static boolean findBoardPosition(List<BoardPosition> positionList, BoardPosition position) {
        for (BoardPosition el : positionList) {
            if (el.getRow() == position.getRow() && el.getCol() == position.getCol()) return true;
        }
        return false;
    }

    /* Performs BFS search to find liberties. If no liberties are found the stone group is captured. */
    public static CheckCaptureResult checkCapture(BoardPosition root, int[][] gameState) {
        int libertyCount = 0;
        BoardPosition atariPos = null;
        LinkedList<BoardPosition> bfQueue = new LinkedList<>();
        HashSet<String> visited = new HashSet<>();
        List<BoardPosition> stoneGroup = new ArrayList<>();
        BoardPosition currPos;
        bfQueue.add(root);

        while (!bfQueue.isEmpty()) {
            currPos = bfQueue.removeFirst();
            visited.add(""+ currPos.getRow() + currPos.getCol());
            stoneGroup.add(currPos);
            GetConnectedResult connStones = getConnected(currPos, gameState);
            if (connStones.getLibertyCount() == 1) {
                atariPos = connStones.getLibertyPositions().get(0);
            }
            libertyCount = libertyCount + connStones.getLibertyCount();
            for (BoardPosition connected : getConnected(currPos, gameState).getConnectedStones()) {
                if (!visited.contains(""+connected.getRow()+connected.getCol())) {
                    bfQueue.add(connected);
                }
            }
        }

        if (libertyCount != 1) {
            return new CheckCaptureResult(libertyCount, stoneGroup, null);
        } else return new CheckCaptureResult(libertyCount, stoneGroup, atariPos);
    }

    /* Checks if a move is valid based on Go rules. */
    public static boolean isValidMove(BoardPosition move, int colorID, int[][] gameState) {
        /* The position where the player wants to place a stone is empty. */
        if (gameState[move.getRow()][move.getCol()] != 0) return false;

        int[][] nextGameState = new int[gameState.length][gameState.length];
        for (int i = 0; i < gameState.length; i++) {
            for (int j = 0; j < gameState.length; j++) {
                nextGameState[i][j] = gameState[i][j];
            }
        }
        nextGameState[move.getRow()][move.getCol()] = colorID;

        /* The move does not cause current player's stones to be captured. Allow move if it leads to
        opponent stone capture before player stone capture.
        */
        List<BoardPosition> adjacentStones = getAdjacent(move, nextGameState);
        if (checkCapture(move, nextGameState).getLibertyCount() == 0) {
            for (BoardPosition stone : adjacentStones) {
                if (stone.getRow() >= 0 &&
                        stone.getRow() < nextGameState.length &&
                        stone.getCol() >=0 &&
                        stone.getCol() < nextGameState.length &&
                        nextGameState[stone.getRow()][stone.getCol()] != 0 &&
                        nextGameState[stone.getRow()][stone.getCol()] != colorID &&
                        (checkCapture(stone, nextGameState).getLibertyCount() == 0)) {
                    return true;
                }
            }
            return false;
        }

        /* The move does not fill an eye. */
        for (BoardPosition stone : getAdjacent(move, gameState)) {
            if (stone.getRow() >= 0 &&
                    stone.getRow() < gameState.length &&
                    stone.getCol() >= 0 &&
                    stone.getCol() < gameState.length &&
                    gameState[stone.getRow()][stone.getCol()] != colorID) return true;
        }

        return false;
    }

    public static List<BoardPosition> stoneCapture(int[][] gameState) {
        List<BoardPosition> capturedStones = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (gameState[i][j] != 0) {
                    CheckCaptureResult result = checkCapture(new BoardPosition(i, j), gameState);
                    if (result.getLibertyCount() == 0) {
                        capturedStones = result.getStoneGroup();
                        for (BoardPosition captured : result.getStoneGroup()) {
                            gameState[captured.getRow()][captured.getCol()] = 0;
                        }
                    }
                }
            }
        }
        return capturedStones;
    }

    public static List<BoardPosition> capture (int[][] gameState, int playerId) {
        List<BoardPosition> capturedStones = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (gameState[i][j] == playerId) {
                    CheckCaptureResult result = checkCapture(new BoardPosition(i, j), gameState);
                    if (result.getLibertyCount() == 0) {
                        capturedStones = result.getStoneGroup();
                        for (BoardPosition captured : result.getStoneGroup()) {
                            gameState[captured.getRow()][captured.getCol()] = 0;
                        }
                    }
                }
            }
        }
        return capturedStones;
    }

    /* Calculate the territory points of a player. */
    public int getTerritory(int[][] gameState, int colorID) {
        int upInd = 0;
        int downInd = 0;
        int rightInd = 0;
        int leftInd = 0;
        int territory = 0;
        String direction = "up";
        boolean playerSurrounds = false;
        List<Integer> surroundingStones = new ArrayList<>();

        for (int i = 0; i < gameState.length; i++) {
            for (int j = 0; j < gameState.length; j++) {
                // For every stone get the stones that surround it.
                switch (direction) {
                    case "up":
                        if (gameState[upInd][j] != 0) {
                            surroundingStones.add(gameState[upInd][j]);
                            direction = "down";
                            break;
                        }
                        if (upInd > 0) {
                            upInd -= 1;
                        } else break;
                    case "down":
                        if (gameState[downInd][j] != 0) {
                            surroundingStones.add(gameState[downInd][j]);
                            direction = "right";
                            break;
                        }
                        if (downInd < gameState.length) {
                            downInd += 1;
                        } else break;
                    case "right":
                        if (gameState[i][rightInd] != 0) {
                            surroundingStones.add(gameState[rightInd][j]);
                            direction = "left";
                            break;
                        }
                        if (rightInd < gameState.length) {
                            rightInd += 1;
                        } else break;
                    case "left":
                        if (gameState[i][leftInd] != 0) {
                            surroundingStones.add(gameState[leftInd][j]);
                            break;
                        }
                        if (leftInd > 0) {
                            leftInd -= 1;
                        } else break;
                    default:
                        break;
                }

                // If a stone is surrounded only by stones with given colorID add one territory point.
                for (int id : surroundingStones) {
                    if (id == colorID) playerSurrounds = true;
                    else if (id != 0 && id != colorID) {
                        playerSurrounds = false;
                        break;
                    }
                }
                if (playerSurrounds) {
                    territory += 1;
                }
            }
        }
        return territory;
    }
}
