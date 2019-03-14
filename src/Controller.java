import com.sun.xml.internal.fastinfoset.algorithm.BooleanEncodingAlgorithm;
import java.util.ArrayList;
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
        ui.init();
        GoRules rules = new GoRules();
        GoRules.BoardPosition playerMove;
        List<Integer> score;

        List<GoRules.BoardPosition> capturedWhite = new ArrayList<>();
        List<GoRules.BoardPosition> capturedBlack = new ArrayList<>();
        ui.getGameState()[4][4] = 1;
        ui.getGameState()[3][4] = 2;
        ui.getGameState()[4][5] = 2;
        ui.getGameState()[4][3] = 2;
        ui.printGameState();
        ui.drawState();

        MonteCarlo.Move aiMove;
        while(true) {
            System.out.println("Waiting for player turn");
            playerMove = ui.getTurn(1);
            List<GoRules.BoardPosition> capturedStones = rules.capture(ui.getGameState(), 2); // Capture white.
            ui.drawState();

            System.out.println("Waiting for A.I. turn");
            MonteCarlo mcts = new MonteCarlo(ui.getGameState(), 2);
            aiMove = mcts.getTurn(capturedWhite.size() == 1, playerMove);

            // If both players pass score the board.
            if (aiMove == null && playerMove == null) {
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

            ui.getGameState()[aiMove.pos.getRow()][aiMove.pos.getCol()] = 2;
            capturedBlack = rules.capture(ui.getGameState(), 1);
            ui.printGameState();
            ui.drawState();

            score = mcts.scoreBoard(ui.getGameState());
            System.out.println("Territory: Black(" + score.get(0) + ") - White(" + score.get(1) + ")");
        }
    }
}
