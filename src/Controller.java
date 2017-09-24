import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Arrays;
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
        Index playerTurn;
        List<Integer> score;
        controller.gameState = ui.getGameState();
        ui.setGameState(controller.gameState);
        rules.stoneCapture(controller.gameState);
        ui.setGameState(controller.gameState);

        while(true) {
            playerTurn = ui.getTurn(1);
            controller.gameState = ui.getGameState();
            rules.stoneCapture(controller.gameState);
            ui.setGameState(controller.gameState);

            MonteCarlo mcts = new MonteCarlo(controller.gameState, 2);
            MonteCarlo.Move aiTurn = mcts.getTurn();
            if (aiTurn == null && playerTurn == null) {
                score  = mcts.scoreBoard(controller.gameState);
                if (score.get(0) >= score.get(1)+7) { // Give white an advantage of 7 points because it plays second.
                    System.out.println("Black won!");
                    System.out.println(score.get(0) + " to " + score.get(1));
                } else {
                    System.out.println("White won!");
                    System.out.println(score.get(0) + " to " + score.get(1));
                }
                break;
            }
            controller.gameState[aiTurn.pos.getRow()][aiTurn.pos.getCol()] = 2;
            rules.stoneCapture(controller.gameState);
            ui.setGameState(controller.gameState);
        }
    }
}
