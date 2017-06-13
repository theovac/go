
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by theovac on 1/5/2017.
 *
 * Simulates a Go player and determines the next move based on the state of the game board. The next move is
 * determined using the alpha-beta pruning algorithm.
 */
public class GoAI {
    protected Controller controller;
    protected static int colorID;
    protected static int opponentColorID;
    protected int depth;
    private GoRules rules;

    public GoAI(Controller controller, int colorID, int depth) {
        this.controller = controller;
        this.colorID = colorID;
        if (colorID == 1) {
            this.opponentColorID = 2;
        } else { this.opponentColorID = 1; }
        this.depth = depth;
        rules = new GoRules();
    }

    /* Alpha-beta search tree node structure. */
    private abstract static class Node {
        protected double value;
        protected GoRules.BoardPosition move = null; // The stone placement this Node represents.
        private int[][] gameState = null;
        private Node parent = null;
        private List<Node> children = null;

        public Node() {
        }

        public void setValue(double value) { this.value = value; }

        public abstract void setMove(GoRules.BoardPosition move);

        public void setGameState(int[][] gameState) {this.gameState = gameState; }

        public void setParent(Node parent) { this.parent = parent; }

        public void setChildren(List<Node> children) {
            this.children = children;
        }

        public double getValue() { return this.value; }

        public GoRules.BoardPosition getMove() { return this.move; }

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

        public void setMove(GoRules.BoardPosition move) {
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

        public void setMove(GoRules.BoardPosition move) {
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
                    if (rules.isValidMove(new GoRules.BoardPosition(i, j), this.colorID, currentNode.getGameState())) {
                        Node childNode;
                        if (currentNode.getClass().getName().equals("GoAI$Maximizer")) {
                            childNode = new Minimizer();
                        } else {
                            childNode = new Maximizer();
                        }
                        childNode.setParent(currentNode);
                        childNode.setMove(new GoRules.BoardPosition(i, j));
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
        GoRules.CheckCaptureResult result;
        GoRules.GetConnectedResult connectedResult;
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
                    result = rules.checkCapture(new GoRules.BoardPosition(i, j), current.getGameState());
                    if ((!result.getStoneGroup().isEmpty()) && (result.getLibertyCount() == 0)) {
                        for (GoRules.BoardPosition captured : result.getStoneGroup()) {
                            current.getGameState()[captured.getRow()][captured.getCol()] = 0;
                            score += 100;
                        }
                    }
                    connectedResult = rules.getConnected(new GoRules.BoardPosition(i, j), current.getGameState());
                    if (connectedResult.getLibertyCount() != 0) {
                        score = score - connectedResult.getLibertyCount() * 5;
                    }
                } else if (current.gameState[i][j] == colorID) {
                    connectedResult = rules.getConnected(new GoRules.BoardPosition(i, j), current.getGameState());
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

    public GoRules.BoardPosition getMove() {
        Node searchTree = this.makeTree();
        double value = searchTree.alphabeta(this.depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        System.out.println("Max potential value: " + value);
        for (Node child : searchTree.getChildren()) {
            System.out.println(searchTree.getValue());
            System.out.println(child.getChildren().get(0).getValue());
            if (value == child.value) {
                return new GoRules.BoardPosition(child.getMove().getRow(), child.getMove().getCol());
            }
        }
        return null;
    }
}
