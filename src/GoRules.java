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

        public GetConnectedResult(int libertyCount, List<BoardPosition> connectedStones) {
            this.libertyCount = libertyCount;
            this.connectedStones = connectedStones;
        }

        public int getLibertyCount() {
            return this.libertyCount;
        }

        public List<BoardPosition> getConnectedStones() {
            return this.connectedStones;
        }
    }

    /* Finds connected stones of the same color. */
    public GetConnectedResult getConnected(BoardPosition pos, int[][] gameState) {
        List<BoardPosition> connectedPositions = new ArrayList<BoardPosition>() {
            {
                add(new BoardPosition(pos.getRow() + 1, pos.getCol()));
                add(new BoardPosition(pos.getRow(), pos.getCol() + 1));
                add(new BoardPosition(pos.getRow() - 1, pos.getCol()));
                add(new BoardPosition(pos.getRow(), pos.getCol() - 1));
            }
        };
        List<BoardPosition> connectedStones = new ArrayList<BoardPosition>();
        int libertyCount = 0;

        for (BoardPosition conPos : connectedPositions) {
            if (conPos.getRow() < gameState.length &&
                    conPos.getCol() < gameState.length &&
                    conPos.getRow() >= 0 && conPos.getCol() >= 0) {
                if (gameState[conPos.getRow()][conPos.getCol()] == gameState[pos.getRow()][pos.getCol()]) {
                    connectedStones.add(conPos);
                } else if (gameState[conPos.getRow()][conPos.getCol()] == 0) {
                    libertyCount += 1;
                }
            }
        } /*
        System.out.println("\n" + pos.getRow() + ", " + pos.getCol() + ":");
        for (BoardPosition stone : connectedStones) {
            System.out.print(stone.getRow() + ", " + stone.getCol());
        } */
        return new GetConnectedResult(libertyCount, connectedStones);
    }

    public static final class CheckCaptureResult {
        private final int libertyCount;
        private final Set<BoardPosition> stoneGroup;

        public CheckCaptureResult(int libertyCount, Set<BoardPosition> stoneGroup) {
            this.libertyCount = libertyCount;
            this.stoneGroup = stoneGroup;
        }

        public int getLibertyCount() {
            return this.libertyCount;
        }

        public Set<BoardPosition> getStoneGroup() {
            return this.stoneGroup;
        }
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

        public int getCol() {
            return this.col;
        }
    }

    public boolean findBoardPosition(Set<BoardPosition> positionList, BoardPosition position) {
        for (BoardPosition el : positionList) {
            if (el.getRow() == position.getRow() && el.getCol() == position.getCol()) return true;
        }
        return false;
    }

    /* Performs BFS search to find liberties. If no liberties are found the stone group is captured. */
    public CheckCaptureResult checkCapture(BoardPosition root, int[][] gameState) {
        int libertyCount = 0;
        LinkedList<BoardPosition> fifo = new LinkedList<>();
        Set<BoardPosition> visited = new HashSet<>();
        BoardPosition current;
        int colorID = gameState[root.getRow()][root.getCol()];
        fifo.add(root);

        while (!fifo.isEmpty()) {
            current = fifo.removeFirst();
            visited.add(current);
            libertyCount = libertyCount + getConnected(current, gameState).getLibertyCount();
            for (BoardPosition connected : getConnected(current, gameState).getConnectedStones()) {
                if (!findBoardPosition(visited, connected)) {
                    fifo.add(connected);
                }
            }
        }

        return new CheckCaptureResult(libertyCount, visited);
    }

    /* Checks if a move is valid based on Go rules. */
    public boolean isValidMove(BoardPosition move, int colorID, int[][] gameState) {
        int[][] nextGameState = new int[gameState.length][gameState.length];
        for (int i = 0; i < gameState.length; i++) {
            for (int j = 0; j < gameState.length; j++) {
                nextGameState[i][j] = gameState[i][j];
            }
        }

        /* The position where the player wants to place a stone is empty. */
        if (gameState[move.getRow()][move.getCol()] != 0) return false;

        /* The move does not cause current player's stones to be captured. */
        nextGameState[move.getRow()][move.getCol()] = colorID;
        if (checkCapture(move, nextGameState).getLibertyCount() == 0) return false;

        /* TODO: The move does not lead to the same game state as after the current player's previous turn. */
        return true;
    }

    public void stoneCapture(int[][] gameState) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (gameState[i][j] != 0) {
                    CheckCaptureResult result = checkCapture(new BoardPosition(i, j), gameState);
                    System.out.println(result.getStoneGroup());
                    if (result.getLibertyCount() == 0) {
                        for (BoardPosition captured : result.getStoneGroup()) {
                            System.out.println(captured.getRow() + ", " + captured.getCol());
                            gameState[captured.getRow()][captured.getCol()] = 0;
                        }
                    }
                }
            }
        }
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
