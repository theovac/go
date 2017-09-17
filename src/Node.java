import java.util.ArrayList;
import java.util.List;

public class Node {
    private MonteCarlo.Move move;
    private int toPlayColor;
    private Node parent;
    private List<Node> children = new ArrayList<>();
    private int[][] state;
    private double winrate;
    private double wins;
    private double n_simulations;

    public Node(int[][] state, int toPlayColor) {
        this.move = null; // The move that led to the state of this node.
        this.toPlayColor = toPlayColor; // The color id of the player that is going to play after this node is reached.
        this.parent = null;
        this.state = state;
        this.winrate = 0;
        this.wins = 0;
        this.n_simulations = 0;
    }

    public void setMove(MonteCarlo.Move move) {
        this.move = move;
    }

    public MonteCarlo.Move getMove() { return this.move; }

    public int getToPlayColor() { return this.toPlayColor; }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getParent() { return this.parent; }

    public void addChild(Node child) {
        this.children.add(child);
    }

    public List<Node> getChildren() { return this.children; }

    public int[][] getState() { return this.state; }

    public void addWin() {
        this.wins++;
        this.n_simulations++;
        this.winrate = this.wins/this.n_simulations;
    }

    public void addLoss() {
        this.n_simulations++;
        this.winrate = this.wins/this.n_simulations;
    }

    public double getWinrate() { return winrate; }

    public double getWins() { return this.wins; }

    public double getSimulationCount() { return this.n_simulations; }
}
