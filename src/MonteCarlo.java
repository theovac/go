import java.util.*;
import java.util.List;

public class MonteCarlo {
    private int[][] gameState;
    private Node rootNode;
    private List<Node> tree = new ArrayList<>();
    private static int nextMoveColor, prevMoveColor;
    private static int pAtari = 100;
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

    public MonteCarlo(int[][] gameState, int toPlayColor) {
        this.gameState = gameState;
        this.rootNode = new Node(gameState, toPlayColor);
        this.tree.add(this.rootNode);
        this.nextMoveColor = toPlayColor;
        if (toPlayColor == 1) {
           this.prevMoveColor = 2;
        } else this.prevMoveColor = 1;
    }

    public class Move {
        GoRules.BoardPosition pos;
        int priority;

        public Move(GoRules.BoardPosition pos, int priority) {
            this.pos = pos;
            this.priority = priority;
        }
    }

    private GoRules.BoardPosition getAtariPos(int[][] gameState, int i, int j, int toPlaycolor) {
        HashSet<String> checked = new HashSet<>();
        if (gameState[i][j] != toPlaycolor && gameState[i][j] != 0 &&
                !checked.contains(i+""+j)) {
            // Check if opponent stone group containing the stone on i, j is in atari.
            GoRules.CheckCaptureResult captureResult =
                    GoRules.checkCapture(new GoRules.BoardPosition(i, j), gameState);
            for (GoRules.BoardPosition stone : captureResult.getStoneGroup()) {
                checked.add(stone.getRow()+""+stone.getCol());
            }
            if (captureResult.getAtariPos() != null) {
                return captureResult.getAtariPos();
            }
        }
        return null;
    }

    private GoRules.BoardPosition getPatternMatchPos(int[][] gameState, int i, int j, int toPlayColor) {
        String blockString = "" + gameState[i - 1][j - 1] + gameState[i - 1][j] + gameState[i - 1][j + 1] +
                gameState[i][j - 1] + gameState[i][j] + gameState[i][j + 1] + gameState[i + 1][j - 1] +
                gameState[i + 1][j] + gameState[i + 1][j + 1];
        if (matchPattern(blockString, toPlayColor)) {
            return new GoRules.BoardPosition(i, j);
        }
        return null;
    }

    private void addClosestToOpponent(List<GoRules.BoardPosition> randomMoves, List<Move> moves, int[][] gameState, int toPlayColor) {
        Random randGen = new Random();
        // Play some of the random moves at random.
        if (!randomMoves.isEmpty()) {
            Collections.shuffle(randomMoves); // Shuffle so that moves at the top left of the board are not prioritized.
            int maxMoveCount = 10;
            int moveCounter = 0;
            HashSet<GoRules.BoardPosition> checkedMoves = new HashSet<>();
            if (randomMoves.size() < maxMoveCount) {
                for (int i=0; i<randomMoves.size(); i++) {
                    moves.add(new Move(randomMoves.get(i), 4));
                }
            } else {
                // Play random moves prioritizing moves that are close to opponent stones.
                for (int i = 0; i < randomMoves.size(); i++) {
                    for (GoRules.BoardPosition layerOneStone : GoRules.getAdjacent(randomMoves.get(i), gameState)) {
                        if (layerOneStone.getRow() >= 0 &&
                                layerOneStone.getRow() < gameState.length &&
                                layerOneStone.getCol() >= 0 &&
                                layerOneStone.getCol() < gameState.length &&
                                gameState[layerOneStone.getRow()][layerOneStone.getCol()] != toPlayColor &&
                                gameState[layerOneStone.getRow()][layerOneStone.getCol()] != 0) {
                            if (!checkedMoves.contains(randomMoves.get(i))) {
                                checkedMoves.add(randomMoves.get(i));
                                moves.add(new Move(randomMoves.get(i), 4));
                                moveCounter++;
                            }
                        } else {
                            for (GoRules.BoardPosition layerTwoStone : GoRules.getAdjacent(layerOneStone, gameState)) {
                                if (layerTwoStone.getRow() >= 0 &&
                                        layerTwoStone.getRow() < gameState.length &&
                                        layerTwoStone.getCol() >= 0 &&
                                        layerTwoStone.getCol() < gameState.length &&
                                        gameState[layerTwoStone.getRow()][layerTwoStone.getCol()] != toPlayColor &&
                                        gameState[layerTwoStone.getRow()][layerTwoStone.getCol()] != 0) {
                                    if (!checkedMoves.contains(randomMoves.get(i))) {
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
    }

    public List<Move> generate_moves(Node node) {
        Random randGen = new Random();
        List<Move> moves = new ArrayList<>();
        List<GoRules.BoardPosition> skippedMoves = new ArrayList<>();
        List<GoRules.BoardPosition> randomMoves = new ArrayList<>();

        // Add an extra line to the array, that represents off board positions. This is needed for pattern matching.
        int[][] paddedState = new int[node.getState().length+1][node.getState().length];
        Arrays.fill(paddedState[paddedState.length-1], 3);
        for (int i = 0; i < paddedState.length - 1; i++) {
            for (int j = 0; j < paddedState.length - 1; j++) {
                paddedState[i][j] = node.getState()[i][j];
            }
        }

        for (int i = 1; i < paddedState.length-1; i++) {
            for (int j = 1; j < paddedState[0].length-1; j++) {
                // Generate moves that capture enemy stones.
                GoRules.BoardPosition atariPos = getAtariPos(node.getState(), i, j, node.getToPlayColor());
                if (atariPos != null) {
                    if (randGen.nextInt(100) <= pAtari) {
                        moves.add(new Move(atariPos, 1));
                    } else skippedMoves.add(atariPos);
                }

                if (!GoRules.isValidMove(new GoRules.BoardPosition(i, j), node.getToPlayColor(), node.getState())) continue;
                // Play move if the 3x3 block around it matches an urgency pattern.
                GoRules.BoardPosition patternMatchPos = getPatternMatchPos(paddedState, i, j, node.getToPlayColor());
                if (patternMatchPos != null) {
                    if (randGen.nextInt(100) <= pPattern) {
                        moves.add(new Move(patternMatchPos, 2));
                    } else {
                        skippedMoves.add(new GoRules.BoardPosition(i, j));
                    }
                }

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

        // Play previously skipped moves.
        for (GoRules.BoardPosition skippedMove : skippedMoves) {
            moves.add(new Move(skippedMove, 3));
        }

        addClosestToOpponent(randomMoves, moves, node.getState(), node.getToPlayColor());

        return moves;
    }

    private boolean matchPattern(String blockString, int toPlayColor) {
        boolean isMatch;
        boolean foundMatch = false;
        int oppColor;
        if (toPlayColor == 1) {
            oppColor = 2;
        } else {
            oppColor = 1;
        }

        for (String pattern : patterns) {
            isMatch = true;
            pattern = pattern.replaceAll("X", Integer.toString(toPlayColor));
            pattern = pattern.replaceAll("O", Integer.toString(oppColor));
            pattern = pattern.replaceAll("#", Integer.toString(3));
            pattern = pattern.replaceAll("\\.", Integer.toString(0));

            for (int i = 0; i < blockString.length(); i++) {
                if (blockString.charAt(i) != pattern.charAt(i)) {
                    if (pattern.charAt(i) == '?' && blockString.charAt(i) != '3') {
                        continue;
                    } else if (pattern.charAt(i) == 'W' &&
                            (blockString.charAt(i) == (char)(48+oppColor) || blockString.charAt(i) == '0')) {
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

        int prevMoveColor;
        if (node.getToPlayColor() == 1) {
            prevMoveColor = 2;
        } else prevMoveColor = 1;
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

            moves = generate_moves(new Node(currentState, prevMoveColor));
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
                currentState[lastPlayMove.pos.getRow()][lastPlayMove.pos.getCol()] = prevMoveColor;
            }
            toPlayCaptured = rules.stoneCapture(currentState);
            if (toPlayCaptured.size() == 1) {
                koMove = toPlayCaptured.get(0);
            }
        }

        score = scoreBoard(currentState);
        if (node.getToPlayColor() == 1) {
            return (score.get(0) < score.get(1));
        } else return (score.get(0) >= score.get(1));
    }

    /* Fill an empty area which includes a specific position, with the number 3, which differentiates this empty
    group from the others in the board. This allows us to work on this group only.
     */
    private List<GoRules.BoardPosition> markEmptySpace(int[][] state, GoRules.BoardPosition pos) {
        LinkedList<GoRules.BoardPosition> fifo = new LinkedList<>();
        List<GoRules.BoardPosition> visited = new ArrayList<>();

        fifo.add(pos);
        visited.add(pos);
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

    public List<Integer> scoreBoard(int[][] state) {
        int blackScore = 0;
        int whiteScore = 0;
        int[][] territoryMap = getTerritoryMap(state);
        for (int i = 0; i < territoryMap.length; i++) {
            for (int j = 0; j < territoryMap.length; j++) {
                if (territoryMap[i][j] == 3) {
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

    /* Calculate Upper Confidence bound 1. */
    private double ucb(Node currentNode) {
        double c = Math.sqrt(2); // Exploration parameter.
        return (currentNode.getWinrate() +
                c*Math.sqrt(Math.log(currentNode.getParent().getSimulationCount())/currentNode.getSimulationCount()));
    }

    /* Select the node where the tree will be expanded, based on a tree descend policy. */
    private Node treeDescend() {
        Node selectedNode = this.rootNode;
        Node bestChild;
        double maxucb, childucb;

        while(selectedNode.getChildren().size() > 0) {
            // Descend picking the node with the maximum UCB in each step.
            bestChild = selectedNode.getChildren().get(0);
            maxucb = ucb(bestChild);
            for (Node child : selectedNode.getChildren()) {
                childucb = ucb(child);
                if (childucb > maxucb) {
                    maxucb = childucb;
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
            if (child.getWinrate() == 1 && currentNode.getParent().getToPlayColor() == child.getToPlayColor()) {
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
            tree.add(child);
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

    private int getDepth(Node node) {
        int depth = 0;
        Node currentNode = node;
        while (currentNode.getParent() != null) {
            depth++;
            currentNode = currentNode.getParent();
        }
        return depth;
    }

    /* Decide the next move. */
    public Move getMove(boolean possibleKo, GoRules.BoardPosition opponentMove) {
        Node selectedNode;
        Node bestNode;
        long startTime = System.currentTimeMillis();

        GoRules.BoardPosition koMove = null;
        if (possibleKo  && opponentMove != null && GoRules.checkCapture(opponentMove, gameState).getLibertyCount() == 1) {
            koMove = GoRules.getConnected(opponentMove, gameState).getLibertyPositions().get(0);
        }

        // Stop expanding the MC tree after certain time.
        int expansion = 0;
        while ((System.currentTimeMillis()-startTime) < 20000) {
            selectedNode = treeDescend();
            treeExpand(selectedNode);
            expansion++;
        }
        System.out.println("Expansion: " + expansion);

        if (this.rootNode.getChildren().isEmpty()) return null; // No moves available.
        bestNode = null;
        for (Node child : this.rootNode.getChildren()) {
            if (koMove != null && child.getMove().pos.getRow() == koMove.getRow() &&
                    child.getMove().pos.getCol() == koMove.getCol()) {
                // A move that would result to KO is not valid.
                continue;
            } else if (child.getMove().priority == 1) {
                // Always play atari moves.
                return child.getMove();
            } else if (bestNode == null || child.getSimulationCount() > bestNode.getSimulationCount()) {
                // Prioritise the move with the most simulations.
                bestNode = child;
            } else if (bestNode == null || child.getWinrate() > bestNode.getWinrate()) {
                // Pick the move that leads to best win rate.
                bestNode = child;
            }
        }

        System.out.println("Computer played move with priority " + bestNode.getMove().priority);
        System.out.println("Chose move at (" + bestNode.getMove().pos.getRow() + ", " +
                    bestNode.getMove().pos.getCol() +  ") Winrate:" + bestNode.getWins() + "/" +
                    bestNode.getSimulationCount() + " UCB:" + ucb(bestNode) + " ");
        for (Node child : rootNode.getChildren()) {
            System.out.println("Position: (" + child.getMove().pos.getRow() + ", " +
                    child.getMove().pos.getCol() +  ") Winrate:" + child.getWins() + "/" +
                    child.getSimulationCount() + " UCB:" + ucb(child) + " ");
        }

        int maxDepth = 0;
        for (Node treeNode : tree) {
            if (getDepth(treeNode) > maxDepth) {
                maxDepth = getDepth(treeNode);
            }
        }

        System.out.println("Tree depth: " + maxDepth);
        System.out.println();
        return bestNode.getMove();
    }
}
