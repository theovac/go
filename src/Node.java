import java.util.List;

public class Node {
    MonteCarlo.Move move;
    int toPlayColor;
    Node parent;
    List<Node> children;
    int[][] state;
    int winrate;
    int wins;
    int losses;

    public Node(int[][] state, int toPlayColor) {
        this.move = null;
        this.toPlayColor = toPlayColor; // The color id of the player that is going to play after this node is reached.
        this.parent = null;
        this.children = null;
        this.state = state;
        this.winrate = 1;
        this.wins = 1;
        this.losses = 1;
    }

    public void setMove(MonteCarlo.Move move) {
        this.move = move;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public void addWin() {
        this.wins++;
        this.winrate = this.wins/this.losses;
    }

    public void addLoss() {
        this.losses++;
        this.winrate = this.wins/this.losses;
    }
}
