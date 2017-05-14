
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by theovac on 1/5/2017.
 *
 * Simulates a Go player and determines the next move based on the state of the game board. The next move is
 * determined using the alpha-beta pruning algorithm.
 */
public class GoAI {
    protected GoController controller;
    protected static int colorID;
    protected static int opponentColorID;
    protected int depth;

    public GoAI(GoController controller, int colorID, int depth) {
        this.controller = controller;
        this.colorID = colorID;
        if (colorID == 1) {
            this.opponentColorID = 2;
        } else { this.opponentColorID = 1; }
        this.depth = depth;
    }

    /* Alpha-beta search tree node structure. */
    private abstract static class Node {
        protected double value;
        protected BoardPosition move = null; // The stone placement this Node represents.
        private int[][] gameState = null;
        private Node parent = null;
        private List<Node> children = null;

        public Node() {
        }

        public void setValue(double value) { this.value = value; }

        public abstract void setMove(BoardPosition move);

        public void setGameState(int[][] gameState) {this.gameState = gameState; }

        public void setParent(Node parent) { this.parent = parent; }

        public void setChildren(List<Node> children) {
            this.children = children;
        }

        public double getValue() { return this.value; }

        public BoardPosition getMove() { return this.move; }

        public int[][] getGameState() { return this.gameState; }

        public Node getParent() {
            return this.parent;
        }

        public List<Node> getChildren() {
            return this.children;
        }

        public int getDepth() {
            Node currentNode = this;
            int depth = 0;

            while(currentNode.getParent() != null) {
                currentNode = currentNode.getParent();
                depth += 1;
            }

            return depth;
        }

        protected abstract double alphabeta(int depth, double a, double b); //Behaviour depends on type of Node.
    }

    private class Maximizer extends Node{

        public Maximizer() {
            super();
            super.value = Double.NEGATIVE_INFINITY;
        }

        public void setMove(BoardPosition move) {
            this.move = move;
            int[][] gameState = this.getParent().getGameState().clone();
            int[][] nextGameState = new int[gameState.length][gameState.length];
            for (int i = 0; i < this.getParent().getGameState().length; i++) {
                for (int j = 0; j < this.getParent().getGameState().length; j++) {
                    nextGameState[i][j] = gameState[i][j];
                }
            }
            nextGameState[move.getRow()][move.getCol()] = colorID;
            this.setGameState(nextGameState);
            evaluate(this);
        }

        public double alphabeta(int depth, double a, double b) {
            if (depth == 0 || this.getChildren().size() == 0) {
                /* Return evaluation function value. */
                return this.value;
            }
            for (Node child : this.getChildren()) {
                this.value = Math.max(this.value, child.alphabeta(depth-1, a, b));
                a = Math.max(this.value, a);
                if (b <= a) break;
            }
            return this.value;
        }
    }

    private class Minimizer extends Node {

        public Minimizer() {
            super();
            super.value = Double.POSITIVE_INFINITY;
        }

        public void setMove(BoardPosition move) {
            this.move = move;
            int[][] gameState = this.getParent().getGameState().clone();
            int[][] nextGameState = new int[gameState.length][gameState.length];
            for (int i = 0; i < this.getParent().getGameState().length; i++) {
                for (int j = 0; j < this.getParent().getGameState().length; j++) {
                    nextGameState[i][j] = gameState[i][j];
                }
            }
            nextGameState[move.getRow()][move.getCol()] = opponentColorID;
            this.setGameState(nextGameState);
            evaluate(this);
        }

        public double alphabeta(int depth, double a, double b) {
            if (depth == 0 || this.getChildren().size() == 0) {
                // Return evaluation function value.
                return this.value;
            }
            for (Node child : this.getChildren()) {
                this.value = Math.min(this.value, child.alphabeta(depth - 1, a, b));
                System.out.println("DEBUG: " + this.value);
                a = Math.min(this.value, a);
                if (b <= a) break;
            }
            return this.value;
        }
    }

    private final class GetConnectedResult {
        private final int libertyCount;
        private final List<BoardPosition> connectedStones;

        public GetConnectedResult(int libertyCount, List<BoardPosition> connectedStones) {
            this.libertyCount = libertyCount;
            this.connectedStones = connectedStones;
        }

        public int getLibertyCount() { return this.libertyCount; }

        public List<BoardPosition> getConnectedStones() { return this.connectedStones; }
    }

    /* Finds connected stones of the same color. */
    private GetConnectedResult getConnected(BoardPosition pos, int[][] gameState) {
        List<BoardPosition> connectedPositions = new ArrayList<BoardPosition>() {
            {
                add(new BoardPosition(pos.getRow()+1, pos.getCol()));
                add(new BoardPosition(pos.getRow(), pos.getCol()+1));
                add(new BoardPosition(pos.getRow()-1, pos.getCol()));
                add(new BoardPosition(pos.getRow(), pos.getCol()-1));
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

    private final class CheckCaptureResult {
        private final int libertyCount;
        private final Set<BoardPosition> stoneGroup;

        public CheckCaptureResult(int libertyCount, Set<BoardPosition> stoneGroup) {
            this.libertyCount = libertyCount;
            this.stoneGroup = stoneGroup;
        }

        public int getLibertyCount() { return this.libertyCount; }

        public Set<BoardPosition> getStoneGroup() { return this.stoneGroup; }
    }

    private class BoardPosition {
        private int row, col;

        public BoardPosition(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() { return this.row; }

        public int getCol() { return this.col; }
    }

    private boolean findBoardPosition(Set<BoardPosition> positionList, BoardPosition position) {
        for (BoardPosition el : positionList) {
            if (el.getRow() == position.getRow() && el.getCol() == position.getCol()) return true;
        }
        return false;
    }

    /* Performs BFS search to find liberties. If no liberties are found the stone group is captured. */
    private CheckCaptureResult checkCapture(BoardPosition root, int[][] gameState) {
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
    private boolean isValidMove(BoardPosition move, int colorID, int[][] gameState) {
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

    /* Construct the root node of the alpha-bet search tree by setting the parent and children for each node. */
    private Node makeTree() {
        int[][] gameState = controller.gameState;
        LinkedList<Node> searchQueue = new LinkedList<>();
        Node rootNode = new Maximizer();
        rootNode.setGameState(gameState);
        searchQueue.add(rootNode); // Root node of the tree.

        while (!searchQueue.isEmpty()) {
            Node currentNode = searchQueue.removeFirst();
            if (currentNode.getDepth() >= this.depth) { break; }
            List<Node> children = new ArrayList<>();
            for (int i = 0; i < currentNode.getGameState().length; i++) {
                for (int j = 0; j < currentNode.getGameState().length; j++) {
                    if (isValidMove(new BoardPosition(i, j), this.colorID, currentNode.getGameState())) {
                        Node childNode;
                        if (currentNode.getClass().getName().equals("GoAI$Maximizer")) {
                            childNode = new Minimizer();
                        } else {
                            childNode = new Maximizer();
                        }
                        childNode.setParent(currentNode);
                        childNode.setMove(new BoardPosition(i, j));
                        children.add(childNode);
                        searchQueue.add(childNode);
                        System.out.println(childNode.getDepth());
                    }
                }
            }
            currentNode.setChildren(children);
        }
        return rootNode;
    }

    /* Capture stones as a result of the move this node represents and calculate score for each node. */
    protected int evaluate(Node current) {
        CheckCaptureResult result;
        GetConnectedResult connectedResult;
        int score = 0;
        int colorID, opponentColorID;

        if (this.getClass().getName() == "Maximizer") {
            colorID = this.colorID;
            opponentColorID = this.opponentColorID;
        } else {
            /* Minimizer plays as the opponent so we swap the colors. */
            colorID = this.opponentColorID;
            opponentColorID = this.colorID;
        }

        for (int i = 0; i < current.getGameState().length; i++) {
            for (int j = 0; j < current.getGameState().length; j++) {
                /* Add one point to score for every captured opponent stone.
                * Add half a point to the score for every liberty.
                * Substract half a point for every opponent liberty.
                */
                if (current.gameState[i][j] == opponentColorID) {
                    result = checkCapture(new BoardPosition(i, j), current.getGameState());
                    if ((!result.getStoneGroup().isEmpty()) && (result.getLibertyCount() == 0)) {
                        for (BoardPosition captured : result.getStoneGroup()) {
                            current.getGameState()[captured.getRow()][captured.getCol()] = 0;
                            score += 100;
                        }
                    }
                    connectedResult = getConnected(new BoardPosition(i, j), current.getGameState());
                    if (connectedResult.getLibertyCount() != 0) {
                        score = score - connectedResult.getLibertyCount() * 5;
                    }
                } else if (current.gameState[i][j] == colorID) {
                    connectedResult = getConnected(new BoardPosition(i, j), current.getGameState());
                    if (connectedResult.getLibertyCount() != 0) {
                        score = score + connectedResult.getLibertyCount() * 2;
                    }
                }
            }
        }

        if (current.getDepth() >= this.depth) {
            current.value = score;
        }
        return score;
    }

    public Index getMove() {
        Node searchTree = this.makeTree();
        double value = searchTree.alphabeta(this.depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        System.out.println("Max potential value: " + value);
        for (Node child : searchTree.getChildren()) {
            System.out.println(searchTree.getValue());
            System.out.println(child.getChildren().get(0).getValue());
            if (value == child.value) {
                return new Index(child.getMove().getRow(), child.getMove().getCol());
            }
        }
        return null;
    }
}
