import com.sun.xml.internal.fastinfoset.algorithm.BooleanEncodingAlgorithm;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
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
        ui.initUI();
        GoRules rules = new GoRules();
        GoAI ai = new GoAI(controller, 2, 2);
        GoRules.BoardPosition playerTurn;
        List<Integer> score;
        controller.gameState = ui.getGameState();
        ui.setGameState(controller.gameState);
        rules.stoneCapture(controller.gameState);

        ui.setGameState(controller.gameState);
        List<GoRules.BoardPosition> capturedWhite = new ArrayList<>();
        List<GoRules.BoardPosition> capturedBlack = new ArrayList<>();

        MonteCarlo.Move aiTurn = null;
        while(true) {
            playerTurn = ui.getTurn(1);
            boolean possibleKo = (capturedBlack.size() == 1);
            GoRules.BoardPosition komove = new GoRules.BoardPosition(-1, -1);
            if (possibleKo && aiTurn != null &&
                    GoRules.checkCapture(aiTurn.pos, controller.gameState).getLibertyCount() == 1) {
                komove = GoRules.getConnected(aiTurn.pos, controller.gameState).getLibertyPositions().get(0);
            }
            if (playerTurn != null) {
                if (!(playerTurn.getRow() == komove.getRow() && playerTurn.getCol() == komove.getCol()) &&
                        GoRules.isValidMove(new GoRules.BoardPosition(playerTurn.getRow(),
                        playerTurn.getCol()), 1, controller.gameState)) {
                    controller.gameState[playerTurn.getRow()][playerTurn.getCol()] = 1;
                    capturedWhite = rules.captureWhite(controller.gameState);
                    ui.setGameState(controller.gameState);
                } else {
                    controller.gameState[playerTurn.getRow()][playerTurn.getCol()] = 0;
                    ui.setGameState(controller.gameState);
                    continue;
                }
            }

            MonteCarlo mcts = new MonteCarlo(controller.gameState, 2);
            GoRules.BoardPosition lastBlackMove = null;
            if (playerTurn != null) {
                lastBlackMove = new GoRules.BoardPosition(playerTurn.getRow(), playerTurn.getCol());
            }
            aiTurn = mcts.getTurn(capturedWhite.size() == 1, lastBlackMove);
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
            capturedBlack = rules.captureBlack(controller.gameState);
            ui.setGameState(controller.gameState);
            score = mcts.scoreBoard(controller.gameState);
            System.out.println("Territory: Black(" + score.get(0) + ") - White(" + score.get(1) + ")");
        }
    }
}
