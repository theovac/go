import sun.reflect.generics.tree.Tree;

import java.util.*;
import java.util.function.BinaryOperator;

public class MonteCarlo {
    private int[][] rootState;
    private Node rootNode;
    private static int toPlayColor, lastPlayColor;
    private static int pAtari = 50;
    private static int pPattern = 50;
    /* Move generation policy patterns. X is the player color, O is the opponent's color, . is empty position,
    W is empty or opponent color, ? means don't care and # means out of board.
    First 11 patterns are isGo patterns. Patterns 12(thin cut) and 13(magari) are cut patterns.
    Patterns 14-16 are atekomi patterns, 17 is a side chase pattern, 18 is a side cut block pattern,
    19 is a side connection block pattern and 20 is side cut pattern.
     */
    private List<String> patterns =
            new ArrayList<String>(Arrays.asList("XOX...???", "XO....?.?", "XO?X..?.?", "?O?X.XWWW",
                                                "X.?O.?###", "?X?W.O###", "XOO...?.?", "?XO?.?###",
                                                "?OX?..###", "?OX?.X###", "?OXX.O###", "XO?O.????",
                                                "???..X.XO", "????.O?O?", "?O?O.????", "?O??.O???",
                                                "X.?O.?###", "OX?X.O###", "?X?W.O###", "?OXX.O###"));
    public MonteCarlo(int[][] rootState, int toPlayColor) {
        this.rootState = rootState;
        this.rootNode = new Node(rootState, toPlayColor);
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
        for (int i = 0; i < node.getState().length; i++) {
            for (int j = 0; j < node.getState().length; j++) {
               if (node.getState()[i][j] != node.getToPlayColor() && node.getState()[i][j] != 0 &&
                       !GoRules.findBoardPosition(checked, new GoRules.BoardPosition(i, j))) {
                   // Check if opponent stone group containing the stone on i, j is in atari.
                   GoRules.CheckCaptureResult captureResult =
                           GoRules.checkCapture(new GoRules.BoardPosition(i, j), node.getState());
                   checked.addAll(captureResult.getStoneGroup());
                   if (captureResult.getLibertyCount() == 1) {
                       // Find the position where if a stone is placed, opponent stones will be captured.
                       for (GoRules.BoardPosition stone : captureResult.getStoneGroup()) {
                           GoRules.GetConnectedResult connectedResult = GoRules.getConnected(stone, node.getState());
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
        int[][] paddedState = new int[node.getState().length+1][node.getState().length];
        Arrays.fill(paddedState[paddedState.length-1], 3);
        int stateRow = 0;
        int stateCol = 0;

        for (int i = 0; i < paddedState.length - 1; i++) {
            for (int j = 0; j < paddedState[0].length; j++) {
                paddedState[i][j] = node.getState()[stateRow][stateCol];
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
                    if (matchPattern(blockString, node.getToPlayColor())) {
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
        for (int i = 0; i < node.getState().length; i++) {
            for (int j = 0; j < node.getState().length; j++) {
                // Add a random move if it is a valid one.
                if(GoRules.isValidMove(new GoRules.BoardPosition(i, j), node.getToPlayColor(), node.getState())) {
                    GoRules.GetConnectedResult randomResult =
                            GoRules.getConnected(new GoRules.BoardPosition(i, j), node.getState());
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
            int maxMoveCount = 10;
            int moveCounter = 0;
            List<GoRules.BoardPosition> checkedMoves = new ArrayList<>();
            if (randomMoves.size() < maxMoveCount) {
                for (int i=0; i<randomMoves.size(); i++) {
                    moves.add(new Move(randomMoves.get(i), 4));
                }
            } else {
                // Play random moves prioritizing moves that are close to opponent stones.
                for (int i = 0; i < randomMoves.size(); i++) {
                    for (GoRules.BoardPosition layerOneStone : GoRules.getAdjacent(randomMoves.get(i), node.getState())) {
                        if (layerOneStone.getRow() >= 0 &&
                                layerOneStone.getRow() < node.getState().length &&
                                layerOneStone.getCol() >= 0 &&
                                layerOneStone.getCol() < node.getState().length &&
                                node.getState()[layerOneStone.getRow()][layerOneStone.getCol()] != node.getToPlayColor() &&
                                node.getState()[layerOneStone.getRow()][layerOneStone.getCol()] != 0) {
                            if (!GoRules.findBoardPosition(checkedMoves, randomMoves.get(i))) {
                                checkedMoves.add(randomMoves.get(i));
                                moves.add(new Move(randomMoves.get(i), 4));
                                moveCounter++;
                            }
                        } else {
                            for (GoRules.BoardPosition layerTwoStone : GoRules.getAdjacent(layerOneStone, node.getState())) {
                                if (layerTwoStone.getRow() >= 0 &&
                                        layerTwoStone.getRow() < node.getState().length &&
                                        layerTwoStone.getCol() >= 0 &&
                                        layerTwoStone.getCol() < node.getState().length &&
                                        node.getState()[layerTwoStone.getRow()][layerTwoStone.getCol()] != node.getToPlayColor() &&
                                        node.getState()[layerTwoStone.getRow()][layerTwoStone.getCol()] != 0) {
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
        if (node.getToPlayColor() == 1) {
            lastPlayColor = 2;
        } else lastPlayColor = 1;
        Node currentNode = node;
        int[][] currentState = new int[node.getState().length][node.getState().length];
        for (int i = 0; i < node.getState().length; i++) {
            for (int j = 0; j < node.getState().length; j++) {
                currentState[i][j] = node.getState()[i][j];
            }
        }
        List<Integer> score;
        GoRules rules = new GoRules();
        List<Move> moves;

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
        while (!(toPlayMove == null && lastPlayMove == null)) {
            moves = generate_moves(new Node(currentState, node.getToPlayColor()));
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
                currentState[toPlayMove.pos.getRow()][toPlayMove.pos.getCol()] = node.getToPlayColor();
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
        }

        score = scoreBoard(currentState);
        if (node.getToPlayColor() == 1) {
            return (score.get(0) > score.get(1));
        } else return (score.get(0) < score.get(1));
    }

    /* Fill an empty area which includes a specific position, with the number 3, which differentiates this empty
    group from the others in the board. This allows us to work on this group only.
     */
    private List<GoRules.BoardPosition> markEmptySpace(int[][] state, GoRules.BoardPosition pos) {
        LinkedList<GoRules.BoardPosition> fifo = new LinkedList<>();
        List<GoRules.BoardPosition> visited = new ArrayList<>();

        fifo.add(pos);
        state[pos.getRow()][pos.getCol()] = 3;

        while (!fifo.isEmpty()) {
            GoRules.BoardPosition currentPos = fifo.removeFirst();
            List<GoRules.BoardPosition> adjacentStones = GoRules.getAdjacent(currentPos, state);
            for (GoRules.BoardPosition stone : adjacentStones) {
                if (stone.getRow() < 0 || stone.getRow() >= state.length ||
                        stone.getCol() < 0 || stone.getCol() >= state.length) {
                    continue;
                }
                if (state[stone.getRow()][stone.getCol()] == 0 && !GoRules.findBoardPosition(visited, pos)) {
                    visited.add(stone);
                    fifo.addFirst(stone);
                    state[stone.getRow()][stone.getCol()] = 3;
                }
            }
        }
        return visited;
    }

    /* For a gives list of Board positions, find if any of the positions is adjacent to a stone of a given color. */
    private boolean touches(int[][] state, List<GoRules.BoardPosition> stoneGroup, int color) {
        for (GoRules.BoardPosition stone : stoneGroup) {
            List<GoRules.BoardPosition> adjacentStones = GoRules.getAdjacent(stone, state);
            for (GoRules.BoardPosition adjacentStone : adjacentStones) {
                if (adjacentStone.getRow() < 0 || adjacentStone.getRow() >= state.length ||
                        adjacentStone.getCol() < 0 || adjacentStone.getCol() >= state.length) {
                    continue;
                }
                if (state[adjacentStone.getRow()][adjacentStone.getCol()] == color) return true;
            }
        }
        return false;
    }

    // Change the color of a group of stones.
    private void fill(int[][] state, List<GoRules.BoardPosition> stoneGroup, int color) {
       for(GoRules.BoardPosition stone : stoneGroup) {
           state[stone.getRow()][stone.getCol()] = color;
       }
    }

    private int[][] getTerritoryMap(int[][] state) {
        int[][] territoryMap = new int[state.length][state.length];
        List<GoRules.BoardPosition> emptySpace;
        boolean touchesBlack;
        boolean touchesWhite;
        boolean foundEmptySpace = true;
        // Copy state array.
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state.length; j++) {
                territoryMap[i][j] = state[i][j];
            }
        }

        while(foundEmptySpace) {
            foundEmptySpace = false;
            for (int i = 0; i < territoryMap.length; i++) {
                for (int j = 0; j < territoryMap.length; j++) {
                    if (territoryMap[i][j] == 0) {
                        foundEmptySpace = true;
                        emptySpace = markEmptySpace(territoryMap, new GoRules.BoardPosition(i, j));
                        touchesBlack = touches(state, emptySpace, 1);
                        touchesWhite = touches(state, emptySpace, 2);
                        if (touchesBlack && !touchesWhite) {
                            fill(territoryMap, emptySpace, 1);
                        } else if (touchesWhite && !touchesBlack) {
                            fill(territoryMap, emptySpace, 2);
                        }
                    }
                }
            }
        }
        return territoryMap;
    }

    private List<Integer> scoreBoard(int[][] state) {
        int blackScore = 0;
        int whiteScore = 0;
        int[][] territoryMap = getTerritoryMap(state);
        for (int i = 0; i < territoryMap.length; i++) {
            for (int j = 0; j < territoryMap.length; j++) {
                if (territoryMap[i][j] == 0) {
                    System.out.println("failed");
                }
                if (territoryMap[i][j] == 1) {
                    blackScore++;
                } else if (territoryMap[i][j] == 2) {
                    whiteScore++;
                }
            }
        }

        return Arrays.asList(blackScore, whiteScore);
    }

    /* Select the node where the tree will be expanded, based on a tree descend policy. */
    private Node treeDescend() {
        Node selectedNode = this.rootNode;
        Node bestChild;

        while(selectedNode.getChildren().size() > 0) {
            bestChild = selectedNode.getChildren().get(0);
            for (Node child : selectedNode.getChildren()) {
                if (child.getWinrate() > bestChild.getWinrate()) {
                    bestChild = child;
                }
            }
            selectedNode = bestChild;
        }
        return selectedNode;
    }

    /* Move up the tree updating the information of every node encountered. */
    private void treeUpdate(Node child) {
        Node currentNode = child;
        while (currentNode.getParent() != null) {
            if (child.getWinrate() == 1) {
                currentNode.getParent().addWin();
            } else currentNode.getParent().addLoss();
            currentNode = currentNode.getParent();
        }
    }

    /* Expand the tree by adding to a node, child nodes each one of which has one of the states that can be
    reached through the moves returned by the move generator.
     */
    private void treeExpand(Node selectedNode) {
        List<Move> moves = generate_moves(selectedNode); // Get available moves.
        for (Move move : moves) {
            int[][] childState = new int[selectedNode.getState().length][selectedNode.getState().length];
            Node child;
            // Copy parent's state.
            for (int i=0; i<selectedNode.getState().length; i++) {
                for (int j=0; j<selectedNode.getState().length; j++) {
                    childState[i][j] = selectedNode.getState()[i][j];
                }
            }
            // Play a move to get the child's state.
            if (selectedNode.getToPlayColor() == 1) {
                childState[move.pos.getRow()][move.pos.getCol()] = 1;
                GoRules.stoneCapture(childState);
                child = new Node(childState, 2);
            } else {
                childState[move.pos.getRow()][move.pos.getCol()] = 2;
                GoRules.stoneCapture(childState);
                child = new Node(childState, 1);
            }
            // Add new child node to the selected node.
            child.setMove(move);
            child.setParent(selectedNode);
            selectedNode.addChild(child);
        }

        // Run Monte Carlo simulation for every child and update the tree depending on the result.
        for (Node child : selectedNode.getChildren()) {
            if (simulatePlayout(child)) { // Child's color won
                child.addWin();
            } else child.addLoss(); // Child's color lost.

            // Move up the tree updating the information of every node encountered.
            treeUpdate(child);
        }
    }

    /* Decide the next move. */
    public Move getTurn() {
        Node selectedNode;
        Node bestNode;
        long startTime = System.currentTimeMillis();
        // Stop expanding the MC tree after certain time.
        while ((System.currentTimeMillis()-startTime) < 5000) {
            selectedNode = treeDescend();
            treeExpand(selectedNode);
        }
        bestNode = this.rootNode.getChildren().get(0);
        for (Node child : this.rootNode.getChildren()) {
            if (child.getSimulationCount() > bestNode.getSimulationCount()) {
                bestNode = child;
            }
        }
        System.out.println("Computer played move with priority " + bestNode.getMove().priority);
        System.out.print("Winrate: " + bestNode.getWinrate() + " out of ");
        for (Node child : rootNode.getChildren()) {
            System.out.println("(" + child.getMove().pos.getRow() + ", " +
                    child.getMove().pos.getCol() +  ") " + child.getWins() + "/" +
                    child.getSimulationCount() + "[" + child.getWinrate() + "] ");
        }

        // Calculate tree depth.
        Node leafNode = treeDescend();
        Node currentNode = leafNode;
        int depth = 0;
        while (currentNode.getParent() != null) {
            depth++;
            currentNode = currentNode.getParent();
        }
        System.out.println("Tree depth: " + depth);
        System.out.println();
        return bestNode.getMove();
    }
}
