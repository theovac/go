import javax.swing.tree.TreeNode;
import java.util.List;

/**
 * Created by theovac on 23/5/2017.
 */
public class Controller {
    int [][] gameState;
    public Controller() {
    }

    public static void main(String args[]) {
        Controller controller = new Controller();
        GoUI ui = new GoUI(9);
        GoRules rules = new GoRules();
        GoAI ai = new GoAI(controller, 2, 2);
        controller.gameState = ui.getGameState();
        /*controller.gameState[0][0] = 1;
        controller.gameState[0][1] = 2;
        controller.gameState[0][2] = 0;
        controller.gameState[1][0] = 2;
        controller.gameState[1][1] = 0;
        controller.gameState[1][2] = 0;
        controller.gameState[2][0] = 1;
        controller.gameState[2][1] = 1;
        controller.gameState[2][2] = 2;
        controller.gameState[4][4] = 1;
        controller.gameState[3][4] = 2;
        controller.gameState[5][4] = 1;
        controller.gameState[5][5] = 2;
        controller.gameState[6][4] = 2;
        controller.gameState[5][3] = 2;
        controller.gameState[4][3] = 2;*/

        ui.setGameState(controller.gameState);
        rules.stoneCapture(controller.gameState);
        ui.setGameState(controller.gameState);

        MonteCarlo mcts = new MonteCarlo(1);
        /*if (!moves.isEmpty()) {
            for (MonteCarlo.Move move : moves) {
                if (move != null) {
                    System.out.println("Move: " + move.pos.getRow() + ", " + move.pos.getCol() + " - " + move.priority);
                } else System.out.println("pass");
                controller.gameState[move.pos.getRow()][move.pos.getCol()] = 2;
                rules.stoneCapture(controller.gameState);
                ui.setGameState(controller.gameState);
            }
        }*/
        while(true) {
            ui.getTurn(1);
            rules.stoneCapture(controller.gameState);
            ui.setGameState(controller.gameState);
            Node node = new Node(controller.gameState, 2);
            List<MonteCarlo.Move> moves = mcts.generate_move(node, controller.gameState);
            GoRules.BoardPosition move = moves.get(0).pos;
            controller.gameState[move.getRow()][move.getCol()] = 2;
            rules.stoneCapture(controller.gameState);
            ui.setGameState(controller.gameState);
        }
    }
}
