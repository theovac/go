
import java.util.List;

/**
 * Created by theovac on 1/5/2017.
 *
 * Simulates a Go player and determines the next move based on the state of the game board. The next move is
 * determined using the alpha-beta pruning algorithm.
 */
public class GoAI {
     protected static GoController controller;

    public GoAI(GoController controller) {
        this.controller = controller;
    }

    private abstract static class Node {
        protected int x, y; // The stone placement this Node represents.
        protected double value = Double.NEGATIVE_INFINITY;
        protected Node parent;
        protected List<Node> children;

        public Node(Node parent, List<Node> children) {
            this.parent = parent;
            this.children = children;
        }

        public double getValue() {
            return this.value;
        }

        public Node getParent() {
            return this.parent;
        }

        public List<Node> getChildren() {
            return this.children;
        }

        public abstract double alphabeta(int depth, double a, double b); //Behaviour depends on type of Node.
    }

    private static class Maximizer extends Node{
        public Maximizer(Node parent, List<Node> children) {
            super(parent, children);
        }

        public double alphabeta(int depth, double a, double b) {
            if (depth == 0 || this.getChildren().size() == 0) {
                //TODO: Return evaluation function value.
                //return evaluate()
            }
            for (Node child : this.getChildren()) {
                this.value = Math.max(this.value, alphabeta(depth-1, a, b));
                a = Math.max(this.value, a);
                if (b <= a) break;
            }
            return this.value;
        }
    }
}
