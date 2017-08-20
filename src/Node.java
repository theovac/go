import java.util.ArrayList;

public class Node {
    int toPlayColor;
    Node parent;
    ArrayList<Node> children;
    int[][] state;
    int wins, losses;

    public Node(int[][] state, int toPlayColor) {
        this.toPlayColor = toPlayColor;
        this.parent = null;
        this.children = null;
        this.state = state;
        this.wins = 0;
        this.losses = 0;
    }

}
