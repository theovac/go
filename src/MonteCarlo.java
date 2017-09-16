import sun.reflect.generics.tree.Tree;

import java.util.*;
import java.util.function.BinaryOperator;

public class MonteCarlo {
    private static int toPlayColor, lastPlayColor;
    private static int pAtari = 50;
    private static int pPattern = 50;
    private List<String> patterns =
            new ArrayList<String>(Arrays.asList("XOX...???", "XO....?.?", "XO?X..?.?", "?O?X.XWWW",
                                                "X.?O.?###", "?X?W.O###", "XOO...?.?", "?XO?.?###",
                                                "?OX?..###", "?OX?.X###", "?OXX.O###"));
    public MonteCarlo(int toPlayColor) {
        this.toPlayColor = toPlayColor;
        if (toPlayColor == 1) {
           this.lastPlayColor = 2;
        } else this.lastPlayColor = 1;
    }

    public class Move {
        GoRules.BoardPosition pos;
        int priority;

        public Move(GoRules.BoardPosition pos, int priority) {
            this.pos = pos;
            this.priority = priority;
        }
    }

    public List<Move> generate_moves(Node node) {
        Random randGen = new Random();
        List<GoRules.BoardPosition> checked = new ArrayList<>();
        List<Move> moves = new ArrayList<>();
        List<GoRules.BoardPosition> skippedMoves = new ArrayList<>();
        // Generate moves that capture enemy stones.
        for (int i = 0; i < node.state.length; i++) {
            for (int j = 0; j < node.state.length; j++) {
               if (node.state[i][j] != node.toPlayColor && node.state[i][j] != 0 &&
                       !GoRules.findBoardPosition(checked, new GoRules.BoardPosition(i, j))) {
                   // Check if opponent stone group containing the stone on i, j is in atari.
                   GoRules.CheckCaptureResult captureResult =
                           GoRules.checkCapture(new GoRules.BoardPosition(i, j), node.state);
                   checked.addAll(captureResult.getStoneGroup());
                   if (captureResult.getLibertyCount() == 1) {
                       // Find the position where if a stone is placed, opponent stones will be captured.
                       for (GoRules.BoardPosition stone : captureResult.getStoneGroup()) {
                           GoRules.GetConnectedResult connectedResult = GoRules.getConnected(stone, node.state);
                           if (connectedResult.getLibertyCount() == 1) {
                               // Play move with pAtari chance
                               if (randGen.nextInt(100) > pAtari) {
                                   moves.add(new Move(connectedResult.getLibertyPositions().get(0), 1));
                               } else {
                                   skippedMoves.add(connectedResult.getLibertyPositions().get(0));
                               }
                           }
                       }
                   }
               }
            }
        }
        checked.clear();
        // Add an extra line to the array, that represents off board positions. This is needed for pattern matching.
        int[][] paddedState = new int[node.state.length+1][node.state.length];
        Arrays.fill(paddedState[paddedState.length-1], 3);
        int stateRow = 0;
        int stateCol = 0;

        for (int i = 0; i < paddedState.length - 1; i++) {
            for (int j = 0; j < paddedState[0].length; j++) {
                paddedState[i][j] = node.state[stateRow][stateCol];
                stateCol++;
            }
            stateCol = 0;
            stateRow++;
        }

        // Play move if the 3x3 block around it matches an urgency pattern.
        for (int i = 1; i < paddedState.length-1; i++) {
            for (int j = 1; j < paddedState[0].length-1; j++) {
                // The 3x3 block in string form.
                if (paddedState[i][j] == 0) {
                    String blockString = "" + paddedState[i - 1][j - 1] + paddedState[i - 1][j] + paddedState[i - 1][j + 1] +
                            paddedState[i][j - 1] + paddedState[i][j] + paddedState[i][j + 1] + paddedState[i + 1][j - 1] +
                            paddedState[i + 1][j] + paddedState[i + 1][j + 1];
                    if (matchPattern(blockString, node.toPlayColor)) {
                        if (randGen.nextInt(100) > pPattern) {
                            moves.add(new Move(new GoRules.BoardPosition(i, j), 2));
                        } else {
                            skippedMoves.add(new GoRules.BoardPosition(i, j));
                        }
                    }
                }
            }
        }

        // Play previously skipped moves.
        for (GoRules.BoardPosition skippedMove : skippedMoves) {
            moves.add(new Move(skippedMove, 3));
        }

        // Make a list of all the possible random moves.
        List<GoRules.BoardPosition> randomMoves = new ArrayList<>();
        for (int i = 0; i < node.state.length; i++) {
            for (int j = 0; j < node.state.length; j++) {
                // Add a random move if it is a valid one.
                if(GoRules.isValidMove(new GoRules.BoardPosition(i, j), node.toPlayColor, node.state)) {
                    GoRules.GetConnectedResult randomResult =
                            GoRules.getConnected(new GoRules.BoardPosition(i, j), node.state);
                    // If the move has only one liberty add a move in the position of the liberty instead.
                    if (randomResult.getLibertyCount() == 1) {
                        randomMoves.add(randomResult.getLibertyPositions().get(0));
                    } else {
                        randomMoves.add(new GoRules.BoardPosition(i, j));
                    }
                }
            }
        }
        // Play some of the random moves at random.
        if (!randomMoves.isEmpty()) {
            Collections.shuffle(randomMoves); // Shuffle so that moves at the top left of the board are not prioritized.
            int maxMoveCount = 5;
            int moveCounter = 0;
            List<GoRules.BoardPosition> checkedMoves = new ArrayList<>();
            if (randomMoves.size() < maxMoveCount) {
                for (int i=0; i<randomMoves.size(); i++) {
                    moves.add(new Move(randomMoves.get(i), 4));
                }
            } else {
                // Play random moves prioritizing moves that are close to opponent stones.
                for (int i = 0; i < randomMoves.size(); i++) {
                    for (GoRules.BoardPosition layerOneStone : GoRules.getAdjacent(randomMoves.get(i), node.state)) {
                        if (layerOneStone.getRow() >= 0 &&
                                layerOneStone.getRow() < node.state.length &&
                                layerOneStone.getCol() >= 0 &&
                                layerOneStone.getCol() < node.state.length &&
                                node.state[layerOneStone.getRow()][layerOneStone.getCol()] != node.toPlayColor &&
                                node.state[layerOneStone.getRow()][layerOneStone.getCol()] != 0) {
                            if (!GoRules.findBoardPosition(checkedMoves, randomMoves.get(i))) {
                                checkedMoves.add(randomMoves.get(i));
                                moves.add(new Move(randomMoves.get(i), 4));
                                moveCounter++;
                            }
                        } else {
                            for (GoRules.BoardPosition layerTwoStone : GoRules.getAdjacent(layerOneStone, node.state)) {
                                if (layerTwoStone.getRow() >= 0 &&
                                        layerTwoStone.getRow() < node.state.length &&
                                        layerTwoStone.getCol() >= 0 &&
                                        layerTwoStone.getCol() < node.state.length &&
                                        node.state[layerTwoStone.getRow()][layerTwoStone.getCol()] != node.toPlayColor &&
                                        node.state[layerTwoStone.getRow()][layerTwoStone.getCol()] != 0) {
                                    if (!GoRules.findBoardPosition(checkedMoves, randomMoves.get(i))) {
                                        checkedMoves.add(randomMoves.get(i));
                                        moves.add(new Move(randomMoves.get(i), 4));
                                        moveCounter++;
                                    }
                                }
                            }
                        }
                    }
                    if (moveCounter >= maxMoveCount) break;
                }
                // If there are not enough moves close to opponent stones, pick the remaining moves at random.
                if (moveCounter < maxMoveCount) {
                    for (int i=0; i<(maxMoveCount-moveCounter); i++) {
                        moves.add(new Move(randomMoves.get(randGen.nextInt(randomMoves.size() - 1)), 4));
                    }
                }
            }
        }

        return moves;
    }

    private boolean matchPattern(String blockString, int toPlayColor) {
        boolean isMatch;
        boolean foundMatch = false;
        if (toPlayColor == 1) {
            lastPlayColor = 2;
        } else lastPlayColor = 1;
        for (String pattern : this.patterns) {
            isMatch = true;
            // Translate pattern for the current player.
            pattern = pattern.replaceAll("X", Integer.toString(toPlayColor));
            pattern = pattern.replaceAll("O", Integer.toString(lastPlayColor));
            pattern = pattern.replaceAll("#", Integer.toString(3));
            pattern = pattern.replaceAll("\\.", Integer.toString(0));

            for (int i = 0; i < blockString.length(); i++) {
                if (blockString.charAt(i) != pattern.charAt(i)) {
                    if (pattern.charAt(i) == '?' && blockString.charAt(i) != '3') {
                        continue;
                    } else if (pattern.charAt(i) == 'W' &&
                            (blockString.charAt(i) == (char)(48+lastPlayColor) || blockString.charAt(i) == '0')) {
                        continue;
                    }
                    isMatch = false;
                }
            }
            if (isMatch == true) {
                foundMatch = true;
                break;
            }
        }
        return foundMatch;
    }

    /*  Simulate a game starting from a node's game state by playing the highest priority move for each player,
    until both players pass. If at the end of the simulation the stones that have this node's color have a
    better score, then return true.
    */
    public boolean simulatePlayout(Node node) {
        Move lastPlayMove = new Move(null, -1);
        Move toPlayMove = new Move(null, -1);
        int lastPlayColor;
        if (node.toPlayColor == 1) {
            lastPlayColor = 2;
        } else lastPlayColor = 1;
        Node currentNode = node;
        int[][] currentState = new int[node.state.length][node.state.length];
        for (int i = 0; i < node.state.length; i++) {
            for (int j = 0; j < node.state.length; j++) {
                currentState[i][j] = node.state[i][j];
            }
        }
        List<Integer> score;
        GoRules rules = new GoRules();
        List<Move> moves;

        int n_moves = 0;
        /* If a players captures only one opponent stone, then the opponent can't play in the
           position of that stone. koMove is that position.
         */
        GoRules.BoardPosition koMove = new GoRules.BoardPosition(-1, -1);
        List<GoRules.BoardPosition> toPlayCaptured = new ArrayList<>();
        List<GoRules.BoardPosition> lastPlayCaptured = new ArrayList<>();
        LinkedList<GoRules.BoardPosition> recentMoves = new LinkedList<GoRules.BoardPosition>();
        for (int i=0; i<10; i++) {
            recentMoves.add(new GoRules.BoardPosition(-1, -1));
        }
        while (!(toPlayMove == null || lastPlayMove == null)) {
            moves = generate_moves(new Node(currentState, node.toPlayColor));
            if (moves.size() > 0) {
                toPlayMove = moves.get(0);
                // Avoid move if move is ko.
                if (toPlayMove.pos.getRow() == koMove.getRow() &&
                        toPlayMove.pos.getCol() == koMove.getCol()) {
                    if (moves.size() > 1) {
                        toPlayMove = moves.get(1);
                    } else toPlayMove = null;
                }
                // If the same move has been played recently, that move does not lead to better score so pass instead.
                if (toPlayMove != null && rules.findBoardPosition(recentMoves, toPlayMove.pos)) {
                    toPlayMove = null;
                }
                recentMoves.removeLast();
                if (toPlayMove == null) {
                    recentMoves.addFirst(new GoRules.BoardPosition(-1, -1));
                } else recentMoves.addFirst(toPlayMove.pos);
            } else toPlayMove = null; // No available moves.
            if (toPlayMove != null) {
                currentState[toPlayMove.pos.getRow()][toPlayMove.pos.getCol()] = node.toPlayColor;
            }
            lastPlayCaptured = rules.stoneCapture(currentState);
            if (lastPlayCaptured.size() == 1) {
                koMove = lastPlayCaptured.get(0);
            }

            moves = generate_moves(new Node(currentState, lastPlayColor));
            if (moves.size() > 0) {
                lastPlayMove = moves.get(0);
                if (lastPlayMove.pos.getRow() == koMove.getRow() &&
                        lastPlayMove.pos.getCol() == koMove.getCol()) {
                    if (moves.size() > 1) {
                        lastPlayMove = moves.get(1);
                    } else lastPlayMove = null;
                }

                if (lastPlayMove!= null && rules.findBoardPosition(recentMoves, lastPlayMove.pos)) {
                    lastPlayMove = null;
                }
                recentMoves.removeLast();
                if (lastPlayMove == null) {
                    recentMoves.addFirst(new GoRules.BoardPosition(-1, -1));
                } else recentMoves.addFirst(lastPlayMove.pos);
            } else lastPlayMove = null;
            if (lastPlayMove != null) {
                currentState[lastPlayMove.pos.getRow()][lastPlayMove.pos.getCol()] = lastPlayColor;
            }
            toPlayCaptured = rules.stoneCapture(currentState);
            if (toPlayCaptured.size() == 1) {
                koMove = toPlayCaptured.get(0);
            }

            n_moves++;
        }

        score = scoreBoard(currentState);
        if (node.toPlayColor == 1) {
            return (score.get(0) < score.get(1));
        } else return (score.get(0) > score.get(1));
    }

    private List<Integer> scoreBoard(int[][] state) {
        int blackScore = 0;
        int whiteScore = 0;
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state.length; j++) {
                if (state[i][j] == 1) {
                    blackScore++;
                } else if (state[i][j] == 2) {
                    whiteScore++;
                }
            }
        }

        return Arrays.asList(blackScore, whiteScore);
    }

    /* Select the node where the tree will be expanded, based on a tree descend policy. */
    private void treeDescend() {

    }

    /* Expand the tree by adding to a node, child nodes each one of which has one of the states that can be
    reached through the moves returned by the move generator.
     */
    private void treeExpand() {

    }

    /* Move up the tree updating, in every node encountered, the maximum (for current player nodes) or minimum
    (for the opponent nodes) score that can be reached in the subtree that has this node as root.
     */
    private void treeUpdate() {

    }

    /* Decide the next move. */
    public Move getTurn() {

    }


}
